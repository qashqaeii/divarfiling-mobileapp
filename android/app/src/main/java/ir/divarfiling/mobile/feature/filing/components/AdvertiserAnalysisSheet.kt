package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.network.AdvertiserAnalysisDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvertiserAnalysisSheet(
    analysis: AdvertiserAnalysisDto,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmitFeedback: (feedback: String, userCorrection: String) -> Unit,
) {
    val submitted = analysis.feedback?.submitted
    var showCorrection by remember(submitted) {
        mutableStateOf(submitted == "incorrect")
    }
    var correctionDraft by remember(submitted, analysis.feedback?.userCorrection) {
        mutableStateOf(analysis.feedback?.userCorrection.orEmpty())
    }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "تحلیل هوشمند آگهی‌دهنده",
            subtitle = analysis.badgeLabel.takeIf { it.isNotBlank() },
            icon = DfIcons.WandSparkles,
            onClose = onDismiss,
            scrollable = false,
        ) {
            ResultCard(analysis = analysis)

            if (analysis.primaryReasonLabel.isNotBlank()) {
                ReasonPill(label = analysis.primaryReasonLabel)
            }

            val signals = buildList {
                addAll(analysis.primarySignals)
                addAll(analysis.secondarySignals)
                addAll(analysis.ownerSignals)
            }
            if (signals.isNotEmpty()) {
                SignalChips(signals = signals)
            }

            if (analysis.disclaimer.isNotBlank()) {
                Text(
                    text = analysis.disclaimer,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            FeedbackSection(
                submitted = submitted,
                showCorrection = showCorrection,
                correctionDraft = correctionDraft,
                isSubmitting = isSubmitting,
                onCorrect = {
                    showCorrection = false
                    onSubmitFeedback("correct", "")
                },
                onIncorrect = { showCorrection = true },
                onCorrectionSelect = { correctionDraft = it },
                onSubmitCorrection = { onSubmitFeedback("incorrect", correctionDraft) },
            )
        }
    }
}

@Composable
private fun ResultCard(analysis: AdvertiserAnalysisDto) {
    val accent = when (analysis.classification) {
        ListingAdvertiserUtils.SIGNAL_GENUINE_PERSONAL -> DfColors.Green to DfColors.GreenLight
        ListingAdvertiserUtils.SIGNAL_DISGUISED -> DfColors.Amber to DfColors.AmberLight
        ListingAdvertiserUtils.SIGNAL_CONSULTANT -> DfColors.Purple to DfColors.PurpleContainer
        else -> DfColors.TextSecondary to DfColors.SurfaceVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = accent.second,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = analysis.classificationLabelQualified.ifBlank { analysis.classificationLabel },
                    style = AppTypography.bodyDescription,
                    fontWeight = FontWeight.Bold,
                    color = accent.first,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (analysis.confidenceLabel.isNotBlank()) {
                    Text(
                        text = analysis.confidenceLabel,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
            if (analysis.summary.isNotBlank()) {
                Text(
                    text = analysis.summary,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ReasonPill(label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Chip,
        color = DfColors.AmberLight.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, DfColors.Amber.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp)) {
            Text(
                text = "دلیل اصلی",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignalChips(signals: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "نشانه‌ها",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            signals.take(6).forEach { signal ->
                Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant) {
                    Text(
                        text = signal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackSection(
    submitted: String?,
    showCorrection: Boolean,
    correctionDraft: String,
    isSubmitting: Boolean,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onCorrectionSelect: (String) -> Unit,
    onSubmitCorrection: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.35f))
        Text(
            text = "آیا این تشخیص درست بود؟",
            style = AppTypography.labelSmall,
            color = DfColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            FeedbackActionButton(
                text = "درست بود",
                selected = submitted == "correct",
                onClick = onCorrect,
                modifier = Modifier.weight(1f),
            )
            FeedbackActionButton(
                text = "اشتباه بود",
                selected = submitted == "incorrect" || showCorrection,
                onClick = onIncorrect,
                modifier = Modifier.weight(1f),
            )
        }

        if (showCorrection || submitted == "incorrect") {
            Text(
                text = "نوع صحیح آگهی‌دهنده",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CorrectionChip(
                    label = "مالک",
                    value = ListingAdvertiserUtils.SIGNAL_GENUINE_PERSONAL,
                    selected = correctionDraft,
                    onSelect = onCorrectionSelect,
                    modifier = Modifier.weight(1f),
                )
                CorrectionChip(
                    label = "پنهان",
                    value = ListingAdvertiserUtils.SIGNAL_DISGUISED,
                    selected = correctionDraft,
                    onSelect = onCorrectionSelect,
                    modifier = Modifier.weight(1f),
                )
                CorrectionChip(
                    label = "مشاور",
                    value = ListingAdvertiserUtils.SIGNAL_CONSULTANT,
                    selected = correctionDraft,
                    onSelect = onCorrectionSelect,
                    modifier = Modifier.weight(1f),
                )
            }
            DfPrimaryButton(
                text = "ثبت نظر",
                onClick = onSubmitCorrection,
                enabled = correctionDraft.isNotBlank(),
                loading = isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackActionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = if (selected) DfColors.PurpleContainer else DfColors.Surface,
        border = BorderStroke(
            1.dp,
            if (selected) DfColors.Purple.copy(alpha = 0.45f) else DfColors.Outline.copy(alpha = 0.5f),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) DfColors.PurpleDark else DfColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CorrectionChip(
    label: String,
    value: String,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = selected == value
    Surface(
        onClick = { onSelect(value) },
        modifier = modifier,
        shape = AppShapes.Chip,
        color = if (isSelected) DfColors.PurpleContainer else DfColors.SurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) DfColors.Purple.copy(alpha = 0.4f) else DfColors.Outline.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) DfColors.PurpleDark else DfColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
