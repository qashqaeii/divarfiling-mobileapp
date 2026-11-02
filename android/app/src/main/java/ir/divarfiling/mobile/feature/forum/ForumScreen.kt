package ir.divarfiling.mobile.feature.forum

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.animateScrollToItem
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
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

    DisposableEffect(Unit) {
        viewModel.setScreenActive(true)
        onDispose { viewModel.setScreenActive(false) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    LaunchedEffect(state.snackbar, state.errorBanner) {
        state.snackbar?.let { snackbar.showDfMessage(it); viewModel.clearSnackbar() }
    }

    LaunchedEffect(state.messages.size, state.access) {
        if (state.access == ForumAccessKind.Ready && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex.coerceAtLeast(0))
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            lastVisible >= total - 2 && total > 0
        }.distinctUntilChanged().collect { atBottom ->
            viewModel.setUserAtBottom(atBottom)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.firstVisibleItemIndex == 0 && state.nextBeforeId != null) {
            viewModel.loadOlderMessages()
        }
    }

    LaunchedEffect(listState, state.messages) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
            .distinctUntilChanged()
            .collect { indices ->
                indices.mapNotNull { idx -> state.messages.getOrNull(idx)?.id }
                    .forEach { viewModel.onMessageVisible(it) }
            }
    }

    if (state.showWelcomeSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.dismissWelcome() }) {
            Column(Modifier.padding(AppSpacing.lg)) {
                Text("به تالار گفتگو خوش آمدید", style = AppTypography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "در این فضا با سایر کاربران فایلینگ تجربه‌ها را مبادله کنید. از انتشار اطلاعات شخصی خودداری کنید.",
                    style = AppTypography.bodyMedium,
                    modifier = Modifier.padding(vertical = AppSpacing.md),
                )
                TextButton(onClick = { viewModel.dismissWelcome() }) {
                    Text("متوجه شدم")
                }
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
            TopAppBar(
                title = {
                    Column {
                        Text("تالار گفتگو", style = AppTypography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "جامعه کاربران فایلینگ",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.onSurfaceVariant(),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(DfIcons.ArrowRight, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch(!state.searchOpen) }) {
                        Icon(DfIcons.Search, contentDescription = "جستجو")
                    }
                    IconButton(onClick = { viewModel.openProfileSheet() }) {
                        Icon(DfIcons.User, contentDescription = "پروفایل")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DfScreenContainerColor),
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
                            if (state.newMessagesBelow > 0) {
                                TextButton(
                                    onClick = {
                                        viewModel.scrollToLatest()
                                        scope.launch {
                                            if (state.messages.isNotEmpty()) {
                                                listState.animateScrollToItem(state.messages.lastIndex)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("${state.newMessagesBelow} پیام جدید")
                                }
                            }
                            state.selectedMessageId?.let { selectedId ->
                                messageById(state.messages, selectedId)?.let { msg ->
                                    ForumMessageActionBar(
                                        message = msg,
                                        canModerate = state.bootstrap?.canModerate == true,
                                        onReply = { viewModel.startReply(msg); viewModel.selectMessage(null) },
                                        onReaction = { viewModel.showReactionPicker(msg.id) },
                                        onCopy = { copyText(context, msg.body) },
                                        onEdit = { viewModel.startEdit(msg) },
                                        onDelete = {
                                            deleteTarget = msg.id
                                            showDeleteConfirm = true
                                        },
                                        onReport = { viewModel.openReport(msg.id) },
                                        onPin = {
                                            viewModel.pinMessage(msg.id, !msg.isPinned)
                                        },
                                        onDismiss = { viewModel.selectMessage(null) },
                                    )
                                }
                            }
                            state.showReactionPickerFor?.let { reactionTarget ->
                                ForumReactionPicker(
                                    onPick = { viewModel.react(reactionTarget, it) },
                                    onDismiss = { viewModel.showReactionPicker(null) },
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
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = AppSpacing.screenHorizontal,
                                        vertical = AppSpacing.sm,
                                    ),
                                ) {
                                    items(state.messages, key = { it.id }) { message ->
                                        ForumMessageRow(
                                            message = message,
                                            selected = state.selectedMessageId == message.id,
                                            onLongPress = { viewModel.selectMessage(message.id) },
                                            onClick = { viewModel.selectMessage(message.id) },
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
                        selected = false,
                        onLongPress = {},
                        onClick = {},
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
