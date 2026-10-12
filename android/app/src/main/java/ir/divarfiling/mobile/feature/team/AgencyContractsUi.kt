package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.network.AgencyContractListItemDto
import ir.divarfiling.mobile.core.network.ContractListStatsDto
import ir.divarfiling.mobile.feature.contracts.ContractSummaryTab

@Composable
fun AgencyContractsStatsRow(
    stats: ContractListStatsDto,
    selectedTab: ContractSummaryTab,
    onTabSelected: (ContractSummaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        listOf(
            ContractSummaryTab.NEEDS_ACTION to ("نیازمند اقدام" to stats.needsAction),
            ContractSummaryTab.DRAFT to ("پیش‌نویس" to stats.draft),
            ContractSummaryTab.AWAITING to ("در انتظار" to stats.awaitingAcceptance),
            ContractSummaryTab.FINALIZED to ("نهایی" to stats.finalized),
        ).forEach { (tab, labelCount) ->
            val (label, count) = labelCount
            DfCard(
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    Modifier.padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        DateUtils.toPersianDigits(count.toString()),
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == tab) DfThemeColors.primary() else DfThemeColors.textPrimary(),
                    )
                    Text(
                        label,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun AgencyContractListCard(
    item: AgencyContractListItemDto,
    onClick: () -> Unit,
    onPrimaryAction: () -> Unit = onClick,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val statusColors = agencyContractStatusColors(item.statusLabel)
    val headline = item.internalNumber.ifBlank { item.title }.ifBlank { item.contractTypeLabel }
    DfCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(if (compact) 64.dp else 80.dp)
                    .background(statusColors.first, RoundedCornerShape(4.dp)),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.contractTypeLabel,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textSecondary(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    DfBadge(
                        text = item.statusLabel,
                        color = statusColors.first,
                        textColor = statusColors.second,
                    )
                }
                Text(
                    headline,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.advisorName.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            DfIcons.User,
                            contentDescription = null,
                            tint = DfThemeColors.textMuted(),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            item.advisorName,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (!compact) {
                    if (item.primaryParty.isNotBlank()) {
                        Text(
                            item.primaryParty,
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val meta = buildList {
                            if (item.signatureStatus.isNotBlank() && item.signatureStatus != "—") {
                                add(item.signatureStatus)
                            }
                            if (item.dealTitle.isNotBlank()) add("معامله: ${item.dealTitle}")
                        }.joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(
                                meta,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (item.nextAction.label.isNotBlank() && item.nextAction.nativeTarget.isNotBlank()) {
                        DfTextButton(
                            text = item.nextAction.label,
                            onClick = onPrimaryAction,
                        )
                    }
                }
            }
        }
    }
}

private fun agencyContractStatusColors(
    statusLabel: String,
): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val label = statusLabel.trim()
    return when {
        label.contains("نهایی") -> DfThemeColors.successContainer() to DfThemeColors.success()
        label.contains("پیش") || label.contains("پیش‌نویس") -> DfThemeColors.warningContainer() to DfThemeColors.warning()
        label.contains("لغو") -> DfThemeColors.errorContainer() to DfThemeColors.error()
        label.contains("امضا") || label.contains("انتظار") || label.contains("ارسال") ->
            DfThemeColors.infoContainer() to DfThemeColors.info()
        else -> DfThemeColors.surfaceVariant() to DfThemeColors.textSecondary()
    }
}
