package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.AgencyContractListItemDto
import ir.divarfiling.mobile.core.network.ContractListStatsDto
import ir.divarfiling.mobile.feature.contracts.ContractSummaryTab
import ir.divarfiling.mobile.feature.contracts.ContractsKpiDashboard

@Composable
fun AgencyContractsStatsRow(
    stats: ContractListStatsDto,
    selectedTab: ContractSummaryTab,
    onTabSelected: (ContractSummaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    ContractsKpiDashboard(
        stats = stats,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val typeIcon = agencyContractTypeIcon(item.contractType)
    val accent = statusColors.second
    val showCta = item.nextAction.label.isNotBlank() && item.nextAction.nativeTarget.isNotBlank()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.1f), Color.Transparent),
                    ),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 44.dp else 52.dp)
                        .clip(AppShapes.IconContainer)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(typeIcon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            TeamAvatar(name = item.advisorName, size = 22.dp, accent = accent)
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
                                "طرف: ${item.primaryParty}",
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        val meta = buildList {
                            if (item.signatureStatus.isNotBlank() && item.signatureStatus != "—") {
                                add(item.signatureStatus)
                            }
                            if (item.currentVersionNumber > 0) {
                                add("نسخه ${DateUtils.toPersianDigits(item.currentVersionNumber.toString())}")
                            }
                        }.joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(
                                meta,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
            if (showCta && !compact) {
                DfPrimaryButton(
                    text = item.nextAction.label,
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.md),
                )
            }
        }
    }
}

private fun agencyContractTypeIcon(contractType: String): ImageVector = when (contractType) {
    "lease" -> DfIcons.Home
    "jv" -> DfIcons.Layers
    else -> DfIcons.Handshake
}

@Composable
@ReadOnlyComposable
private fun agencyContractStatusColors(
    statusLabel: String,
): Pair<Color, Color> {
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
