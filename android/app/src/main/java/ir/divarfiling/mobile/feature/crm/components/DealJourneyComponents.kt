package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.DealContractItemDto
import ir.divarfiling.mobile.core.network.DealCommissionSplitsData
import ir.divarfiling.mobile.core.network.DealContractsData
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.DealJourneyCtaDto
import ir.divarfiling.mobile.core.network.DealJourneyData

enum class DealJourneyTab(val label: String) {
    OVERVIEW("نمای کلی"),
    ACTIVITY("فعالیت‌ها"),
    FINANCE("مالی"),
    CONTRACT("قرارداد"),
    CHECKLIST("چک‌لیست"),
}

@Composable
fun DealJourneyTabRow(
    selected: DealJourneyTab,
    onSelect: (DealJourneyTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = DealJourneyTab.entries.map { DfFilterOption(it, it.label) }
    DfFilterChipRow(
        options = options,
        selected = selected,
        onSelect = onSelect,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    )
}

@Composable
fun DealJourneyHeroCard(
    journey: DealJourneyData?,
    isSubmitting: Boolean,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = journey?.primary ?: DealJourneyCtaDto()
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = journey?.headline?.ifBlank { "قدم بعدی" } ?: "قدم بعدی",
                style = AppTypography.TitleSmall,
                fontWeight = FontWeight.Bold,
            )
            journey?.subline?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = AppTypography.BodySmall, color = DfColors.TextMuted)
            }
            primary.description.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = AppTypography.BodySmall)
            }
            if (primary.label.isNotBlank()) {
                DfPrimaryButton(
                    text = primary.label,
                    onClick = onPrimaryClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun DealJourneyStickyBar(
    onNextAction: () -> Unit,
    onFollowUp: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = DfColors.Surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            DfPrimaryButton(
                text = "اقدام بعدی",
                onClick = onNextAction,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "پیگیری",
                onClick = onFollowUp,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "بیشتر",
                onClick = onMore,
                modifier = Modifier.weight(0.7f),
            )
        }
    }
}

@Composable
fun DealContractsTabContent(
    data: DealContractsData?,
    isSubmitting: Boolean,
    onCreateContract: () -> Unit,
    onOpenContract: (DealContractItemDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        if (data == null) {
            DfEmptyState(title = "در حال بارگذاری…", subtitle = "")
            return
        }
        if (data.locked) {
            DfEmptyState(
                title = "قرارداد دیجیتال قفل است",
                subtitle = "برای استفاده، لایسنس فعال ربات نیاز است.",
            )
            return
        }
        if (data.items.isEmpty()) {
            DfEmptyState(
                title = "قرارداد این معامله هنوز ایجاد نشده",
                subtitle = "قرارداد دیجیتال را از همینجا شروع کنید.",
            )
            DfPrimaryButton(
                text = "ایجاد قرارداد",
                onClick = onCreateContract,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            return
        }
        data.items.forEach { item ->
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${item.contractTypeLabel} · ${item.internalNumber}",
                        style = AppTypography.TitleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = item.statusLabel,
                        style = AppTypography.BodySmall,
                        color = DfColors.TextMuted,
                    )
                    if (item.parties.isNotEmpty()) {
                        Text(
                            text = item.parties.joinToString(" · "),
                            style = AppTypography.BodySmall,
                        )
                    }
                    Text(
                        text = "امضا: ${item.signatureStatus} · تحویل: ${item.handoverStatus}",
                        style = AppTypography.BodySmall,
                        color = DfColors.TextMuted,
                    )
                    val cta = when (item.cta) {
                        "continue_wizard" -> "ادامه قرارداد"
                        "view_signature" -> "مشاهده وضعیت امضا"
                        else -> "مشاهده قرارداد"
                    }
                    DfPrimaryButton(
                        text = cta,
                        onClick = { onOpenContract(item) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun DuplicateDealBottomSheetContent(
    customerName: String,
    propertyTitle: String,
    stage: String,
    lastActivity: String,
    onViewExisting: () -> Unit,
    onForceCreate: () -> Unit,
    isSubmitting: Boolean,
) {
    Column(
        Modifier.padding(AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "برای این مشتری و ملک یک معامله مشابه پیدا کردیم",
            style = AppTypography.TitleSmall,
            fontWeight = FontWeight.Bold,
        )
        DfCard {
            Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(propertyTitle, fontWeight = FontWeight.SemiBold)
                Text(customerName, style = AppTypography.BodySmall)
                Text(stage, style = AppTypography.BodySmall, color = DfColors.TextMuted)
                if (lastActivity.isNotBlank()) {
                    Text("آخرین فعالیت: $lastActivity", style = AppTypography.BodySmall)
                }
            }
        }
        DfPrimaryButton(
            text = "مشاهده معامله موجود",
            onClick = onViewExisting,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        DfSecondaryButton(
            text = "با این حال معامله جدید بساز",
            onClick = onForceCreate,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DealCommissionSplitsSummary(
    data: DealCommissionSplitsData,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("تقسیم کمیسیون", style = AppTypography.TitleSmall, fontWeight = FontWeight.Bold)
            Text(
                "کمیسیون کل: ${FormatUtils.formatPriceToman(data.totalCommission)}",
                style = AppTypography.BodySmall,
            )
            val alloc = data.allocatedPercent
            Text(
                text = if (data.isComplete) "۱۰۰٪ تخصیص داده شده ✓" else "${(100 - alloc).toInt()}٪ هنوز تخصیص داده نشده",
                style = AppTypography.BodySmall,
                color = if (data.isComplete) DfColors.Green else DfColors.TextMuted,
            )
            data.splits.forEach { row ->
                Text(
                    "${row.name} — ${row.percent.toInt()}٪ — ${FormatUtils.formatPriceToman(row.amount ?: 0L)}",
                    style = AppTypography.BodySmall,
                )
            }
            if (!data.canEdit && data.splits.isEmpty() && data.availableMembers.isNotEmpty()) {
                Text(
                    "تقسیم بین اعضا از میزکار تیم قابل ویرایش است.",
                    style = AppTypography.BodySmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}
