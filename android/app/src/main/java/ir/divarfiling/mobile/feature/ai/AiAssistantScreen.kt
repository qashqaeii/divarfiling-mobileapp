package ir.divarfiling.mobile.feature.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.feature.crm.ContactPickerSheet
import ir.divarfiling.mobile.feature.crm.ListingPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val phone = PhoneNormalizer.normalize(state.contactPhone)

    if (state.showContactPicker) {
        ContactPickerSheet(
            onDismiss = { viewModel.toggleContactPicker(false) },
            onContactSelected = { contact ->
                viewModel.selectContact(contact.id, contact.fullName, contact.phone)
            },
        )
    }
    if (state.showListingPicker) {
        ListingPickerSheet(
            onDismiss = { viewModel.toggleListingPicker(false) },
            onListingSelected = { listing ->
                viewModel.selectListing(
                    listing.token,
                    listing.title.orEmpty().ifBlank { "آگهی بدون عنوان" },
                )
            },
        )
    }
    val quotaExhausted = state.quota?.let { it.enabled && it.remaining <= 0 } == true
    val aiDisabled = state.quota?.enabled == false
    val pad = aiHorizontalPadding()

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AiAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    item {
                        DfHubPageHeader(
                            title = "دستیار AI",
                            subtitle = "پیش‌نویس و خلاصه در جریان واقعی کار",
                            titleIconRes = DfDecorIcons.Sparkles,
                            onBack = onBack,
                        )
                    }

                    state.contextLabel?.let { label ->
                        item {
                            DfStatusBanner(
                                message = label,
                                tone = DfStatusTone.Info,
                                title = "زمینه آماده",
                                icon = DfIcons.Sparkles,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }

                    when {
                        aiDisabled -> item {
                            DfStatusBanner(
                                message = "AI فعلاً برای این حساب فعال نیست. پیش‌نویس جایگزین همچنان در دسترس است.",
                                tone = DfStatusTone.Warning,
                                title = "AI غیرفعال",
                                icon = DfIcons.Sparkles,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        quotaExhausted -> item {
                            DfStatusBanner(
                                message = "سهمیه امروز تمام شده است. می‌توانید فردا دوباره تلاش کنید یا از نسخه جایگزین استفاده کنید.",
                                tone = DfStatusTone.Warning,
                                title = "سهمیه تمام شده",
                                icon = DfIcons.Sparkles,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }

                    state.quota?.let { quota ->
                        item {
                            AiQuotaHero(
                                planLabel = quota.planLabel ?: "سهمیه AI",
                                remaining = quota.remaining,
                                limit = quota.limit,
                                enabled = quota.enabled,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }

                    item {
                        DfCard(modifier = Modifier.padding(horizontal = pad)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                Text(
                                    "نوع کار",
                                    style = AppTypography.cardTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfThemeColors.textPrimary(),
                                )
                                DfFilterChipRow(
                                    options = listOf(
                                        DfFilterOption(AiMode.Draft, "پیش‌نویس پیام"),
                                        DfFilterOption(AiMode.Summarize, "خلاصه آگهی"),
                                    ),
                                    selected = state.mode,
                                    onSelect = viewModel::onModeChange,
                                )

                                DfGlassTextButton(
                                    text = state.contactLabel.ifBlank { "انتخاب مخاطب" },
                                    onClick = { viewModel.toggleContactPicker(true) },
                                )
                                DfGlassTextButton(
                                    text = state.listingLabel.ifBlank { "انتخاب آگهی / فایل" },
                                    onClick = { viewModel.toggleListingPicker(true) },
                                )

                                if (state.mode == AiMode.Draft) {
                                    Text(
                                        "لحن پیام",
                                        style = AppTypography.cardTitle,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DfThemeColors.textPrimary(),
                                    )
                                    DfFilterChipRow(
                                        options = AiAssistantViewModel.toneOptions.map {
                                            DfFilterOption(it.value, it.label)
                                        },
                                        selected = state.tone,
                                        onSelect = viewModel::onToneChange,
                                    )
                                    DfTextField(
                                        value = state.notes,
                                        onValueChange = viewModel::onNotesChange,
                                        label = "نکات تکمیلی",
                                        helperText = "مثل بودجه، زمان بازدید یا جزئیات خاص",
                                        singleLine = false,
                                        minLines = 3,
                                        enabled = !state.isSubmitting,
                                    )
                                }

                                DfPrimaryButton(
                                    text = when (state.mode) {
                                        AiMode.Draft -> "ساخت پیش‌نویس"
                                        AiMode.Summarize -> "خلاصه‌سازی آگهی"
                                    },
                                    onClick = viewModel::runPrimaryAction,
                                    loading = state.isSubmitting || state.isLoading,
                                    enabled = !state.isSubmitting,
                                )
                                if (state.draftText.isNotBlank() || state.summaryText.isNotBlank()) {
                                    DfSecondaryButton(
                                        text = "تولید مجدد",
                                        onClick = viewModel::regenerate,
                                        enabled = !state.isSubmitting && !state.isLoading,
                                    )
                                }
                            }
                        }
                    }

                    state.draftText.takeIf { it.isNotBlank() }?.let { draft ->
                        item {
                            AiResultCard(
                                title = if (state.draftIsFallback) "پیش‌نویس جایگزین" else "پیش‌نویس آماده ارسال",
                                body = draft,
                                isFallback = state.draftIsFallback,
                                onBodyChange = viewModel::onDraftTextChange,
                                copyLabel = "کپی متن",
                                onCopy = {
                                    copyToClipboard(context, draft)
                                    viewModel.showMessage("متن پیش‌نویس کپی شد")
                                },
                                phone = phone,
                                onWhatsApp = {
                                    DossierShareActions.openWhatsApp(context, draft, phone)
                                },
                                onBale = { DossierShareActions.openBale(context, draft) },
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }

                    state.summaryText.takeIf { it.isNotBlank() }?.let { summary ->
                        item {
                            AiResultCard(
                                title = if (state.summaryIsFallback) "خلاصه جایگزین" else "خلاصه آگهی",
                                body = summary,
                                isFallback = state.summaryIsFallback,
                                onBodyChange = viewModel::onSummaryTextChange,
                                copyLabel = "کپی خلاصه",
                                onCopy = {
                                    copyToClipboard(context, summary)
                                    viewModel.showMessage("خلاصه آگهی کپی شد")
                                },
                                phone = phone,
                                onWhatsApp = {
                                    DossierShareActions.openWhatsApp(context, summary, phone)
                                },
                                onBale = { DossierShareActions.openBale(context, summary) },
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun aiHorizontalPadding() = when {
    LocalConfiguration.current.screenWidthDp < 360 -> 14.dp
    LocalConfiguration.current.screenWidthDp > 600 -> 28.dp
    else -> AppSpacing.screenHorizontal
}

@Composable
private fun AiAmbientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DfColors.Purple.copy(alpha = 0.10f),
                        DfColors.Blue.copy(alpha = 0.05f),
                        DfThemeColors.background(),
                        DfThemeColors.background(),
                    ),
                ),
            ),
    )
}

@Composable
private fun AiQuotaHero(
    planLabel: String,
    remaining: Int,
    limit: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = when {
        !enabled -> DfColors.Amber
        remaining > 0 -> DfColors.Green
        else -> DfColors.Rose
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Hero)
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.16f),
                        DfColors.Purple.copy(alpha = 0.08f),
                        DfThemeColors.surface().copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.Hero)
            .padding(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = DfIcons.Sparkles,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    planLabel,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    if (enabled) {
                        "$remaining از $limit درخواست باقی مانده"
                    } else {
                        "سرویس AI برای این حساب خاموش است"
                    },
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            }
            DfBadge(
                text = if (!enabled) "خاموش" else if (remaining > 0) "فعال" else "تمام",
                color = accent.copy(alpha = 0.14f),
                textColor = accent,
            )
        }
    }
}

@Composable
private fun AiResultCard(
    title: String,
    body: String,
    isFallback: Boolean,
    copyLabel: String,
    onCopy: () -> Unit,
    onBodyChange: (String) -> Unit,
    phone: String,
    onWhatsApp: () -> Unit,
    onBale: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    modifier = Modifier.weight(1f),
                )
                DfBadge(
                    text = if (isFallback) "جایگزین" else "AI",
                    color = if (isFallback) DfColors.AmberLight else DfColors.PurpleLight,
                    textColor = if (isFallback) DfColors.Amber else DfColors.PurpleDark,
                )
            }
            DfTextField(
                value = body,
                onValueChange = onBodyChange,
                label = "متن قابل ویرایش",
                singleLine = false,
                minLines = 4,
            )
            DfSecondaryButton(text = copyLabel, onClick = onCopy)
            if (phone.isNotBlank()) {
                DfPrimaryButton(text = "ارسال واتساپ", onClick = onWhatsApp)
                DfGlassTextButton(text = "ارسال در بله", onClick = onBale)
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("ai_draft", text))
}
