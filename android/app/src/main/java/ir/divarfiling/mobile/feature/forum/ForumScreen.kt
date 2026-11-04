package ir.divarfiling.mobile.feature.forum

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.network.ForumMessageDto
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    onBack: () -> Unit,
    viewModel: ForumViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableLongStateOf(0L) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var didInitialScroll by remember { mutableStateOf(false) }
    var userAtBottom by remember { mutableStateOf(true) }
    var scrollAnchorMessageId by remember { mutableLongStateOf(0L) }
    var scrollAnchorOffset by remember { mutableIntStateOf(0) }
    var wasLoadingOlder by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setScreenActive(true)
                Lifecycle.Event.ON_STOP -> viewModel.setScreenActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setScreenActive(false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    LaunchedEffect(state.snackbar, state.errorBanner) {
        state.snackbar?.let { snackbar.showDfMessage(it); viewModel.clearSnackbar() }
    }

    val chatItems = remember(state.messages, state.selectedMessageId) {
        ForumChatTimeline.build(state.messages, state.selectedMessageId)
    }

    LaunchedEffect(state.access, chatItems.size) {
        if (
            state.access == ForumAccessKind.Ready &&
            chatItems.isNotEmpty() &&
            !didInitialScroll
        ) {
            listState.scrollToItem(chatItems.lastIndex)
            didInitialScroll = true
            userAtBottom = true
            viewModel.setUserAtBottom(true)
        }
    }

    LaunchedEffect(chatItems.lastOrNull()?.stableKey, userAtBottom) {
        if (!didInitialScroll || chatItems.isEmpty()) return@LaunchedEffect
        if (userAtBottom) {
            listState.animateScrollToItem(chatItems.lastIndex)
        }
    }

    LaunchedEffect(state.loadingOlder, chatItems) {
        if (state.loadingOlder && !wasLoadingOlder) {
            val firstIdx = listState.firstVisibleItemIndex
            scrollAnchorMessageId = chatItems
                .drop(firstIdx.coerceAtLeast(0))
                .firstOrNull { it is ForumChatListItem.Message }
                ?.let { (it as ForumChatListItem.Message).message.id }
                ?: 0L
            scrollAnchorOffset = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0
        } else if (!state.loadingOlder && wasLoadingOlder && scrollAnchorMessageId != 0L) {
            val idx = chatItems.indexOfFirst {
                it is ForumChatListItem.Message && it.message.id == scrollAnchorMessageId
            }
            if (idx >= 0) {
                listState.scrollToItem(idx, scrollAnchorOffset)
            }
            scrollAnchorMessageId = 0L
        }
        wasLoadingOlder = state.loadingOlder
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible >= total - 2 && total > 0
        }.distinctUntilChanged().collect { atBottom ->
            userAtBottom = atBottom
            viewModel.setUserAtBottom(atBottom)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, state.nextBeforeId, state.loadingOlder) {
        if (
            listState.firstVisibleItemIndex == 0 &&
            state.nextBeforeId != null &&
            !state.loadingOlder
        ) {
            viewModel.loadOlderMessages()
        }
    }

    LaunchedEffect(listState, chatItems) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect { indices ->
                indices.mapNotNull { idx ->
                    when (val item = chatItems.getOrNull(idx)) {
                        is ForumChatListItem.Message -> item.message.id
                        else -> null
                    }
                }.forEach { viewModel.onMessageVisible(it) }
            }
    }

    state.selectedMessageId?.let { selectedId ->
        messageById(state.messages, selectedId)?.let { msg ->
            ForumMessageContextSheet(
                message = msg,
                canModerate = state.bootstrap?.canModerate == true,
                onDismiss = { viewModel.selectMessage(null) },
                onReply = { viewModel.startReply(msg) },
                onCopy = {
                    copyText(context, msg.body)
                    scope.launch { snackbar.showSnackbar("متن کپی شد") }
                },
                onEdit = { viewModel.startEdit(msg) },
                onDelete = {
                    deleteTarget = msg.id
                    showDeleteConfirm = true
                },
                onReport = { viewModel.openReport(msg.id) },
                onPin = { viewModel.pinMessage(msg.id, !msg.isPinned) },
                onReaction = { viewModel.react(msg.id, it) },
            )
        }
    }

    if (state.showWelcomeSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.dismissWelcome() }) {
            DfSheetScaffold(
                title = "به تالار گفتگو خوش آمدید",
                subtitle = "فضای گفت‌وگوی جامعه فایلینگ دیوار",
                icon = DfIcons.MessageCircle,
                onClose = { viewModel.dismissWelcome() },
                footer = {
                    TextButton(onClick = { viewModel.dismissWelcome() }) {
                        Text("شروع گفتگو")
                    }
                },
            ) {
                Text(
                    "تجربه‌ها و نکات حرفه‌ای را با سایر مشاوران به اشتراک بگذارید. از انتشار اطلاعات شخصی، شماره تماس یا جزئیات محرمانهٔ مشتری خودداری کنید.",
                    style = AppTypography.body,
                    color = DfThemeColors.textSecondary(),
                )
            }
        }
    }

    state.reportMessageId?.let { reportId ->
        val reasons = state.bootstrap?.reportReasons.orEmpty()
        DfModalBottomSheet(onDismissRequest = { viewModel.dismissReport() }) {
            DfSheetScaffold(
                title = "گزارش پیام",
                subtitle = "دلیل را انتخاب کنید",
                icon = DfIcons.MessageCircle,
                onClose = { viewModel.dismissReport() },
            ) {
                reasons.forEach { reason ->
                    TextButton(
                        onClick = { viewModel.report(reportId, reason.id, "") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(reason.label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (state.showProfileSheet) {
        ForumProfileSheet(
            member = state.bootstrap?.member,
            onDismiss = { viewModel.dismissProfileSheet() },
            onSave = viewModel::saveDisplayName,
        )
    }

    if (showDeleteConfirm) {
        DfConfirmBottomSheet(
            title = "حذف پیام",
            message = "این پیام حذف می‌شود. ادامه می‌دهید؟",
            confirmText = "حذف",
            destructive = true,
            onConfirm = {
                viewModel.deleteMessage(deleteTarget)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            ForumTopBar(
                searchOpen = state.searchOpen,
                onBack = onBack,
                onToggleSearch = { viewModel.toggleSearch(!state.searchOpen) },
                onOpenProfile = { viewModel.openProfileSheet() },
            )
        },
        bottomBar = {
            if (state.access == ForumAccessKind.Ready && !state.searchOpen) {
                ForumComposerBar(
                    text = state.composerText,
                    onTextChange = viewModel::onComposerChange,
                    placeholder = "پیام خود را بنویسید…",
                    replyLabel = state.replyTo?.senderLabel,
                    onCancelReply = { viewModel.cancelReply() },
                    editMode = state.editingMessageId != null,
                    onCancelEdit = { viewModel.cancelEdit() },
                    onSend = { viewModel.sendOrSave() },
                    sendEnabled = state.composerText.isNotBlank() && !state.isSending,
                    isSending = state.isSending,
                    pendingFailed = state.pendingSend?.failed == true,
                    onRetry = { viewModel.retryPendingSend() },
                    modifier = Modifier.navigationBarsPadding().imePadding(),
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.access) {
                ForumAccessKind.Loading -> {
                    DfCardListSkeleton(modifier = Modifier.fillMaxSize().padding(AppSpacing.screenHorizontal))
                }
                ForumAccessKind.Disabled, ForumAccessKind.Restricted -> {
                    DfEmptyState(
                        variant = DfEmptyVariant.Error,
                        title = state.accessMessage ?: "دسترسی به تالار ممکن نیست",
                        subtitle = "",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                ForumAccessKind.Ready -> {
                    if (state.searchOpen) {
                        ForumSearchPanel(
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            results = state.searchResults,
                            loading = state.searchLoading,
                            total = state.searchTotal,
                            onLoadMore = { viewModel.loadMoreSearch() },
                        )
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            state.bootstrap?.pinnedNotices?.let { notices ->
                                ForumNoticeStrip(
                                    notices = notices,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
                                )
                            }
                            state.bootstrap?.pinnedMessage?.let { pinned ->
                                ForumPinnedBar(
                                    message = pinned,
                                    expanded = state.pinnedExpanded,
                                    onToggle = { viewModel.togglePinnedExpanded() },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
                                )
                            }
                            state.errorBanner?.let { err ->
                                DfErrorBanner(
                                    message = err,
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                            if (state.messages.isEmpty() && state.pendingSend == null) {
                                DfEmptyState(
                                    variant = DfEmptyVariant.Empty,
                                    title = "هنوز پیامی نیست",
                                    subtitle = "اولین پیام را بفرستید",
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Box(Modifier.weight(1f).fillMaxSize()) {
                                    Column(
                                        Modifier
                                            .fillMaxSize()
                                            .background(DfThemeColors.background()),
                                    ) {
                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = AppSpacing.screenHorizontal,
                                                vertical = AppSpacing.sm,
                                            ),
                                        ) {
                                            if (state.loadingOlder) {
                                                item(key = "forum-loading-older") {
                                                    Box(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .padding(AppSpacing.sm),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(22.dp),
                                                            strokeWidth = 2.dp,
                                                        )
                                                    }
                                                }
                                            }
                                            items(
                                                items = chatItems,
                                                key = { it.stableKey },
                                            ) { item ->
                                                when (item) {
                                                    is ForumChatListItem.DaySeparator -> {
                                                        ForumChatDaySeparator(label = item.label)
                                                    }
                                                    is ForumChatListItem.Message -> {
                                                        ForumMessageRow(
                                                            message = item.message,
                                                            showSenderName = item.showSenderName,
                                                            showAvatar = item.showAvatar,
                                                            isContextTarget = item.isContextTarget,
                                                            onLongPress = { viewModel.selectMessage(item.message.id) },
                                                            onReactionClick = { emoji ->
                                                                viewModel.react(item.message.id, emoji)
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (state.newMessagesBelow > 0) {
                                        ForumNewMessagesPill(
                                            count = state.newMessagesBelow,
                                            onClick = {
                                                viewModel.scrollToLatest()
                                                scope.launch {
                                                    if (chatItems.isNotEmpty()) {
                                                        listState.animateScrollToItem(chatItems.lastIndex)
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = AppSpacing.sm),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumSearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<ForumMessageDto>,
    loading: Boolean,
    total: Int,
    onLoadMore: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(AppSpacing.screenHorizontal)) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("جستجو در پیام‌ها (حداقل ۲ حرف)") },
            singleLine = true,
        )
        when {
            loading -> Text("در حال جستجو…", modifier = Modifier.padding(top = AppSpacing.md))
            query.length >= 2 && results.isEmpty() -> DfEmptyState(
                variant = DfEmptyVariant.NoResults,
                title = "نتیجه‌ای یافت نشد",
                subtitle = "عبارت دیگری امتحان کنید",
                modifier = Modifier.fillMaxSize(),
            )
            else -> LazyColumn {
                items(results, key = { it.id }) { message ->
                    ForumMessageRow(
                        message = message,
                        onLongPress = {},
                        onReactionClick = null,
                    )
                }
                if (results.size < total) {
                    item {
                        TextButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                            Text("صفحه بعد")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForumProfileSheet(
    member: ir.divarfiling.mobile.core.network.ForumMemberDto?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember(member?.displayName) {
        androidx.compose.runtime.mutableStateOf(member?.displayName.orEmpty())
    }
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "پروفایل تالار",
            subtitle = member?.displayLabel.orEmpty(),
            icon = DfIcons.User,
            onClose = onDismiss,
            footer = {
                TextButton(onClick = { onSave(name) }) {
                    Text("ذخیره نام نمایشی")
                }
            },
        ) {
            Text("شناسه عمومی: ${member?.publicId.orEmpty()}", style = AppTypography.labelSmall)
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("نام نمایشی") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.md),
            )
        }
    }
}

private fun messageById(messages: List<ForumMessageDto>, id: Long): ForumMessageDto? =
    messages.firstOrNull { it.id == id }

private fun copyText(context: Context, text: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("forum", text))
}
