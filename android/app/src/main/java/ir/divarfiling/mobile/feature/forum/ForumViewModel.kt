package ir.divarfiling.mobile.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ForumBootstrapDto
import ir.divarfiling.mobile.core.network.ForumMessageDto
import ir.divarfiling.mobile.core.network.ForumProfileRequest
import ir.divarfiling.mobile.core.network.ForumSendMessageRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ForumRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

val FORUM_ALLOWED_REACTIONS = listOf("👍", "❤️", "👏", "😂", "🙏")

enum class ForumAccessKind {
    Loading,
    Ready,
    Disabled,
    Restricted,
}

data class PendingSend(
    val clientMessageId: String,
    val body: String,
    val replyToId: Long?,
    val failed: Boolean = false,
)

data class ForumUiState(
    val access: ForumAccessKind = ForumAccessKind.Loading,
    val accessMessage: String? = null,
    val bootstrap: ForumBootstrapDto? = null,
    val messages: List<ForumMessageDto> = emptyList(),
    val nextBeforeId: Long? = null,
    val loadingOlder: Boolean = false,
    val pendingSend: PendingSend? = null,
    val composerText: String = "",
    val replyTo: ForumMessageDto? = null,
    val editingMessageId: Long? = null,
    val isSending: Boolean = false,
    val snackbar: String? = null,
    val errorBanner: String? = null,
    val newMessagesBelow: Int = 0,
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<ForumMessageDto> = emptyList(),
    val searchLoading: Boolean = false,
    val searchPage: Int = 1,
    val searchTotal: Int = 0,
    val showProfileSheet: Boolean = false,
    val showWelcomeSheet: Boolean = false,
    val reportMessageId: Long? = null,
    val selectedMessageId: Long? = null,
    val showReactionPickerFor: Long? = null,
    val pinnedExpanded: Boolean = false,
)

@HiltViewModel
class ForumViewModel @Inject constructor(
    private val repository: ForumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()

    private val messageMap = linkedMapOf<Long, ForumMessageDto>()
    private var lastSyncSince: String = Instant.now().toString()
    private var pollJob: Job? = null
    private var loadOlderJob: Job? = null
    private var pollBusy = false
    private var pollBackoffUntil: Long = 0L
    private val viewMutex = Mutex()
    private val sendMutex = Mutex()
    private var syncResumeCursor: Long = 0L
    private var syncInProgress = false
    private val pendingViewIds = linkedSetOf<Long>()
    private var viewFlushJob: Job? = null
    private var screenActive = false
    private var userAtBottom = true
    private var searchRequestId = 0

    fun setScreenActive(active: Boolean) {
        if (active) {
            if (screenActive) return
            screenActive = true
            startPolling()
            viewModelScope.launch {
                if (_uiState.value.access == ForumAccessKind.Ready) {
                    pollOnce()
                }
            }
        } else {
            if (!screenActive) return
            screenActive = false
            pollJob?.cancel()
            pollJob = null
            loadOlderJob?.cancel()
        }
    }

    fun setUserAtBottom(atBottom: Boolean) {
        userAtBottom = atBottom
        if (atBottom && _uiState.value.newMessagesBelow > 0) {
            _uiState.update { it.copy(newMessagesBelow = 0) }
            markReadIfNeeded()
        }
    }

    fun scrollToLatest() {
        _uiState.update { it.copy(newMessagesBelow = 0) }
        markReadIfNeeded()
    }

    fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(access = ForumAccessKind.Loading, accessMessage = null) }
            when (val boot = repository.bootstrap()) {
                is ApiResult.Error -> {
                    val disabled = boot.code == "NOT_FOUND" || boot.code == "not_found"
                    _uiState.update {
                        it.copy(
                            access = if (disabled) ForumAccessKind.Disabled else ForumAccessKind.Restricted,
                            accessMessage = boot.message,
                        )
                    }
                    return@launch
                }
                is ApiResult.Success -> {
                    val data = boot.data
                    repository.cachedUnreadCount = data.unread
                    when (val page = repository.loadMessages()) {
                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    access = ForumAccessKind.Restricted,
                                    accessMessage = page.message,
                                )
                            }
                            return@launch
                        }
                        is ApiResult.Success -> {
                            mergeMessages(page.data.messages, replace = true)
                            lastSyncSince = Instant.now().toString()
                            _uiState.update {
                                it.copy(
                                    access = ForumAccessKind.Ready,
                                    bootstrap = data,
                                    nextBeforeId = page.data.nextBeforeId,
                                    showWelcomeSheet = !data.member.welcomeSeen,
                                )
                            }
                            markReadIfNeeded()
                        }
                    }
                }
            }
        }
    }

    fun loadOlderMessages() {
        val before = _uiState.value.nextBeforeId ?: return
        if (_uiState.value.loadingOlder) return
        if (loadOlderJob?.isActive == true) return
        loadOlderJob = viewModelScope.launch {
            _uiState.update { it.copy(loadingOlder = true) }
            try {
                when (val result = repository.loadMessages(beforeId = before)) {
                    is ApiResult.Error -> showError(result.message)
                    is ApiResult.Success -> {
                        val page = result.data
                        if (page.messages.isNotEmpty()) {
                            prependMessages(page.messages)
                        }
                        val next = page.nextBeforeId
                        val stuck = page.messages.isEmpty() && (next == null || next == before)
                        _uiState.update {
                            it.copy(nextBeforeId = if (stuck) null else next)
                        }
                    }
                }
            } finally {
                _uiState.update { it.copy(loadingOlder = false) }
            }
        }
    }

    fun onComposerChange(text: String) {
        val max = _uiState.value.bootstrap?.limits?.maxBody ?: 2000
        _uiState.update { it.copy(composerText = text.take(max)) }
    }

    fun startReply(message: ForumMessageDto) {
        if (message.isDeleted) return
        _uiState.update {
            it.copy(
                replyTo = message,
                editingMessageId = null,
                selectedMessageId = null,
            )
        }
    }

    fun cancelReply() {
        _uiState.update { it.copy(replyTo = null) }
    }

    fun startEdit(message: ForumMessageDto) {
        if (!message.isMine || message.isDeleted) return
        _uiState.update {
            it.copy(
                editingMessageId = message.id,
                composerText = message.body,
                replyTo = null,
                selectedMessageId = null,
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingMessageId = null, composerText = "") }
    }

    fun sendOrSave() {
        val state = _uiState.value
        val text = state.composerText.trim()
        if (text.isBlank() || state.isSending) return
        val editId = state.editingMessageId
        if (editId != null) {
            saveEdit(editId, text)
            return
        }
        val pending = state.pendingSend
        if (pending != null) {
            val sameDraft = text == pending.body.trim() &&
                state.replyTo?.id == pending.replyToId
            if (pending.failed && sameDraft) {
                retryPendingSend()
                return
            }
            showError("ابتدا ارسال ناموفق قبلی را لغو کنید یا «تلاش مجدد» بزنید.")
            return
        }
        sendNew(text, state.replyTo?.id, clientMessageId = null)
    }

    private fun sendNew(body: String, replyToId: Long?, clientMessageId: String?) {
        viewModelScope.launch {
            if (!sendMutex.tryLock()) return@launch
            try {
                if (_uiState.value.isSending) return@launch
                val clientId = clientMessageId ?: UUID.randomUUID().toString()
                _uiState.update {
                    it.copy(
                        isSending = true,
                        pendingSend = PendingSend(clientId, body, replyToId),
                        errorBanner = null,
                    )
                }
                val request = ForumSendMessageRequest(
                    body = body,
                    replyToId = replyToId,
                    clientMessageId = clientId,
                )
                when (val result = repository.sendMessage(request)) {
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                pendingSend = PendingSend(clientId, body, replyToId, failed = true),
                                errorBanner = result.message,
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        mergeMessages(listOf(result.data.message))
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                pendingSend = null,
                                composerText = "",
                                replyTo = null,
                            )
                        }
                        if (userAtBottom) markReadIfNeeded() else bumpNewBelow(1)
                    }
                }
            } finally {
                sendMutex.unlock()
            }
        }
    }

    fun retryPendingSend() {
        val pending = _uiState.value.pendingSend ?: return
        if (_uiState.value.isSending) return
        sendNew(pending.body, pending.replyToId, pending.clientMessageId)
    }

    fun dismissPendingSend() {
        _uiState.update { it.copy(pendingSend = null, errorBanner = null) }
    }

    private fun saveEdit(messageId: Long, body: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            when (val result = repository.editMessage(messageId, body)) {
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSending = false) }
                    showError(result.message)
                }
                is ApiResult.Success -> {
                    mergeMessages(listOf(result.data.message))
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            editingMessageId = null,
                            composerText = "",
                        )
                    }
                }
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            when (val result = repository.deleteMessage(messageId)) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    messageMap[messageId]?.let { existing ->
                        mergeMessages(
                            listOf(
                                existing.copy(body = "", isDeleted = true, reactions = emptyList()),
                            ),
                        )
                    }
                    _uiState.update { it.copy(selectedMessageId = null) }
                    showSnackbar("پیام حذف شد")
                }
            }
        }
    }

    fun react(messageId: Long, emoji: String) {
        viewModelScope.launch {
            val current = messageMap[messageId]?.myReaction.orEmpty()
            val next = if (current == emoji) null else emoji
            when (val result = repository.setReaction(messageId, next)) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    messageMap[messageId]?.let { msg ->
                        mergeMessages(
                            listOf(
                                msg.copy(
                                    reactions = result.data.reactions,
                                    myReaction = next.orEmpty(),
                                ),
                            ),
                        )
                    }
                    _uiState.update { it.copy(showReactionPickerFor = null, selectedMessageId = null) }
                }
            }
        }
    }

    fun report(messageId: Long, reason: String, note: String) {
        viewModelScope.launch {
            when (val result = repository.reportMessage(messageId, reason, note)) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    _uiState.update { it.copy(reportMessageId = null, selectedMessageId = null) }
                    showSnackbar("گزارش ثبت شد")
                }
            }
        }
    }

    fun pinMessage(messageId: Long, pin: Boolean) {
        viewModelScope.launch {
            when (val result = repository.pinMessage(messageId, pin)) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    val pinned = result.data.pinnedMessage
                    _uiState.update { state ->
                        state.copy(
                            bootstrap = state.bootstrap?.copy(pinnedMessage = pinned),
                            selectedMessageId = null,
                        )
                    }
                    showSnackbar(if (pin) "پیام سنجاق شد" else "سنجاق برداشته شد")
                }
            }
        }
    }

    fun toggleSearch(open: Boolean) {
        _uiState.update {
            it.copy(
                searchOpen = open,
                searchQuery = if (open) it.searchQuery else "",
                searchResults = if (open) it.searchResults else emptyList(),
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), searchTotal = 0, searchPage = 1) }
            return
        }
        viewModelScope.launch {
            delay(350)
            if (query != _uiState.value.searchQuery) return@launch
            val reqId = ++searchRequestId
            _uiState.update { it.copy(searchLoading = true) }
            when (val result = repository.search(query.trim(), page = 1)) {
                is ApiResult.Error -> {
                    if (reqId == searchRequestId) {
                        _uiState.update { it.copy(searchLoading = false) }
                        showError(result.message)
                    }
                }
                is ApiResult.Success -> {
                    if (reqId == searchRequestId) {
                        _uiState.update {
                            it.copy(
                                searchLoading = false,
                                searchResults = result.data.messages,
                                searchTotal = result.data.total,
                                searchPage = result.data.page,
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMoreSearch() {
        val q = _uiState.value.searchQuery.trim()
        if (q.length < 2) return
        val nextPage = _uiState.value.searchPage + 1
        viewModelScope.launch {
            when (val result = repository.search(q, nextPage)) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            searchResults = it.searchResults + result.data.messages,
                            searchPage = result.data.page,
                        )
                    }
                }
            }
        }
    }

    fun openProfileSheet() {
        _uiState.update { it.copy(showProfileSheet = true) }
    }

    fun dismissProfileSheet() {
        _uiState.update { it.copy(showProfileSheet = false) }
    }

    fun dismissWelcome() {
        viewModelScope.launch {
            repository.updateProfile(ForumProfileRequest(welcomeAck = true))
            _uiState.update { state ->
                val member = state.bootstrap?.member?.copy(welcomeSeen = true)
                state.copy(
                    showWelcomeSheet = false,
                    bootstrap = state.bootstrap?.copy(member = member ?: state.bootstrap.member),
                )
            }
        }
    }

    fun saveDisplayName(name: String) {
        viewModelScope.launch {
            when (val result = repository.updateProfile(ForumProfileRequest(displayName = name.trim()))) {
                is ApiResult.Error -> showError(result.message)
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val boot = state.bootstrap ?: return@update state
                        val member = boot.member.copy(
                            displayName = result.data.displayName ?: boot.member.displayName,
                            displayLabel = result.data.displayLabel ?: boot.member.displayLabel,
                        )
                        state.copy(
                            bootstrap = boot.copy(member = member),
                            showProfileSheet = false,
                        )
                    }
                    showSnackbar("نام نمایشی ذخیره شد")
                }
            }
        }
    }

    fun onMessageVisible(messageId: Long) {
        viewModelScope.launch {
            viewMutex.withLock {
                pendingViewIds.add(messageId)
            }
            scheduleViewFlush()
        }
    }

    fun selectMessage(id: Long?) {
        _uiState.update { it.copy(selectedMessageId = id) }
    }

    fun showReactionPicker(messageId: Long?) {
        _uiState.update { it.copy(showReactionPickerFor = messageId) }
    }

    fun openReport(messageId: Long) {
        _uiState.update { it.copy(reportMessageId = messageId, selectedMessageId = null) }
    }

    fun dismissReport() {
        _uiState.update { it.copy(reportMessageId = null) }
    }

    fun togglePinnedExpanded() {
        _uiState.update { it.copy(pinnedExpanded = !it.pinnedExpanded) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbar = null, errorBanner = null) }
    }

    private fun scheduleViewFlush() {
        viewFlushJob?.cancel()
        viewFlushJob = viewModelScope.launch {
            delay(800)
            flushViews()
        }
    }

    private suspend fun flushViews() {
        val batch = viewMutex.withLock {
            val ids = pendingViewIds.take(40).toList()
            pendingViewIds.removeAll(ids.toSet())
            ids
        }
        if (batch.isEmpty()) return
        when (val result = repository.recordViews(batch)) {
            is ApiResult.Success -> {
                result.data.views.forEach { (key, count) ->
                    key.toLongOrNull()?.let { id ->
                        messageMap[id]?.let { msg ->
                            mergeMessages(listOf(msg.copy(viewCount = count)))
                        }
                    }
                }
            }
            is ApiResult.Error -> Unit
        }
    }

    private fun markReadIfNeeded() {
        val lastId = messageMap.keys.maxOrNull() ?: return
        if (!userAtBottom) return
        viewModelScope.launch {
            repository.updateRead(lastId)
            _uiState.update { state ->
                state.copy(bootstrap = state.bootstrap?.copy(unread = 0))
            }
        }
    }

    private fun bumpNewBelow(count: Int) {
        _uiState.update { it.copy(newMessagesBelow = it.newMessagesBelow + count) }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive && screenActive) {
                val now = System.currentTimeMillis()
                if (now >= pollBackoffUntil) {
                    pollOnce()
                }
                delay(10_000)
            }
        }
    }

    private suspend fun pollOnce() {
        if (pollBusy || _uiState.value.access != ForumAccessKind.Ready) return
        pollBusy = true
        try {
            fetchNewerMessages()
            syncChanges()
        } finally {
            pollBusy = false
        }
    }

    private suspend fun fetchNewerMessages() {
        var afterId = messageMap.keys.maxOrNull() ?: return
        var loops = 0
        var added = 0
        while (loops < 15) {
            loops++
            when (val page = repository.loadMessages(afterId = afterId)) {
                is ApiResult.Error -> {
                    if (page.code?.startsWith("rate_") == true) {
                        pollBackoffUntil = System.currentTimeMillis() + 30_000
                    }
                    break
                }
                is ApiResult.Success -> {
                    val items = page.data.messages.filter { it.id !in messageMap }
                    if (items.isNotEmpty()) {
                        mergeMessages(items)
                        added += items.size
                        afterId = items.maxOf { it.id }
                    }
                    if (!page.data.hasMoreAfter || page.data.messages.isEmpty()) break
                }
            }
        }
        if (added > 0) {
            if (userAtBottom) markReadIfNeeded() else bumpNewBelow(added)
        }
    }

    private suspend fun syncChanges() {
        if (syncInProgress) return
        val anchor = messageMap.keys.maxOrNull() ?: return
        syncInProgress = true
        val sinceAtCycleStart = lastSyncSince
        var cursor = syncResumeCursor
        var loops = 0
        var advanceCheckpoint = false
        try {
            while (loops < SYNC_MAX_PAGES) {
                loops++
                when (val sync = repository.syncChanges(sinceAtCycleStart, anchor, cursor)) {
                    is ApiResult.Error -> return
                    is ApiResult.Success -> {
                        if (sync.data.messages.isNotEmpty()) {
                            mergeMessages(sync.data.messages)
                        }
                        val hasMore = sync.data.hasMore && sync.data.nextCursor != null
                        if (hasMore) {
                            cursor = sync.data.nextCursor
                            if (loops >= SYNC_MAX_PAGES) {
                                syncResumeCursor = cursor
                                return
                            }
                        } else {
                            syncResumeCursor = 0L
                            advanceCheckpoint = true
                            return
                        }
                    }
                }
            }
        } finally {
            syncInProgress = false
            if (advanceCheckpoint) {
                lastSyncSince = Instant.now().minus(2, ChronoUnit.SECONDS).toString()
            }
        }
    }

    companion object {
        private const val SYNC_MAX_PAGES = 10
    }

    private fun mergeMessages(items: List<ForumMessageDto>, replace: Boolean = false) {
        if (replace) messageMap.clear()
        items.forEach { messageMap[it.id] = it }
        publishMessages()
    }

    private fun prependMessages(items: List<ForumMessageDto>) {
        items.forEach { messageMap[it.id] = it }
        publishMessages()
    }

    private fun publishMessages() {
        val sorted = messageMap.values.sortedBy { it.id }
        _uiState.update { it.copy(messages = sorted) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorBanner = message) }
    }

    private fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbar = message) }
    }

    override fun onCleared() {
        pollJob?.cancel()
        loadOlderJob?.cancel()
        viewFlushJob?.cancel()
        messageMap.clear()
        super.onCleared()
    }
}
