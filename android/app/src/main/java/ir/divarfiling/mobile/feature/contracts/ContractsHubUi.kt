package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.ContractListItemDto
import ir.divarfiling.mobile.core.network.ContractListStatsDto

@Composable
fun ContractsPageHeader(
    stats: ContractListStatsDto,
    onBack: () -> Unit,
    onCreateContract: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalLabel = DateUtils.toPersianDigits(stats.all.toString())
    DfHubPageHeader(
        title = "قراردادها",
        subtitle = "مدیریت قرارداد دیجیتال، امضا و تحویل",
        sectionLabel = DfHeaderSections.CRM,
        titleIconRes = DfDecorIcons.FileText,
        onBack = onBack,
        showBottomDivider = false,
        modifier = modifier,
        toolbarContent = {
            Surface(
                onClick = onCreateContract,
                shape = RoundedCornerShape(999.dp),
                color = DfThemeColors.primary(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        DfIcons.Plus,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        "جدید",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        },
        bottomContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(bottom = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "$totalLabel قرارداد ثبت‌شده",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
                if (stats.needsAction > 0) {
                    Text(
                        "${DateUtils.toPersianDigits(stats.needsAction.toString())} نیازمند اقدام",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.warning(),
                    )
                }
            }
        },
    )
}

@Composable
fun ContractsKpiDashboard(
    stats: ContractListStatsDto,
    selectedTab: ContractSummaryTab,
    onTabSelected: (ContractSummaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ContractsKpiFeatured(
            label = "نیازمند اقدام",
            count = stats.needsAction,
            icon = DfIcons.CircleAlert,
            tint = DfColors.Amber,
            selected = selectedTab == ContractSummaryTab.NEEDS_ACTION,
            onClick = { onTabSelected(ContractSummaryTab.NEEDS_ACTION) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            ContractsKpiTile(
                label = "پیش‌نویس",
                count = stats.draft,
                icon = DfIcons.Pencil,
                tint = DfColors.PurpleDark,
                selected = selectedTab == ContractSummaryTab.DRAFT,
                onClick = { onTabSelected(ContractSummaryTab.DRAFT) },
                modifier = Modifier.weight(1f),
            )
            ContractsKpiTile(
                label = "در انتظار",
                count = stats.awaitingAcceptance,
                icon = DfIcons.Clock,
                tint = DfColors.Blue,
                selected = selectedTab == ContractSummaryTab.AWAITING,
                onClick = { onTabSelected(ContractSummaryTab.AWAITING) },
                modifier = Modifier.weight(1f),
            )
            ContractsKpiTile(
                label = "نهایی",
                count = stats.finalized,
                icon = DfIcons.CircleCheck,
                tint = DfColors.Green,
                selected = selectedTab == ContractSummaryTab.FINALIZED,
                onClick = { onTabSelected(ContractSummaryTab.FINALIZED) },
                modifier = Modifier.weight(1f),
            )
        }
        ContractsKpiAllRow(
            count = stats.all,
            selected = selectedTab == ContractSummaryTab.ALL,
            onClick = { onTabSelected(ContractSummaryTab.ALL) },
        )
    }
}

@Composable
private fun ContractsKpiFeatured(
    label: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = AppShapes.Hero
    val borderColor = if (selected) tint else DfThemeColors.outlineSubtle()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        tint.copy(alpha = if (selected) 0.22f else 0.12f),
                        DfThemeColors.surface(),
                    ),
                ),
            )
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(AppShapes.IconContainer)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            Text(
                DateUtils.toPersianDigits(count.toString()),
                style = AppTypography.pageTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
        }
        if (selected) {
            Text("فعال", style = AppTypography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContractsKpiTile(
    label: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppShapes.StatCard
    val borderColor = if (selected) tint else DfThemeColors.outlineSubtle()
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(if (selected) tint.copy(alpha = 0.08f) else DfThemeColors.surface())
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Text(
                label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            DateUtils.toPersianDigits(count.toString()),
            style = AppTypography.sectionTitle,
            fontWeight = FontWeight.Bold,
            color = if (selected) tint else DfThemeColors.textPrimary(),
        )
    }
}

@Composable
private fun ContractsKpiAllRow(
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) DfThemeColors.primaryContainer().copy(alpha = 0.45f)
                else DfThemeColors.surfaceVariant().copy(alpha = 0.35f),
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) DfThemeColors.primary() else DfThemeColors.outlineSubtle(),
                ),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(DfIcons.Layers, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(18.dp))
            Text("همه قراردادها", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        Text(
            DateUtils.toPersianDigits(count.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.primary(),
        )
    }
}

@Composable
fun ContractsResumeCard(
    item: ContractListItemDto,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.primaryContainer().copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DfThemeColors.primary().copy(alpha = 0.25f)),
    ) {
        Column(
            Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(DfIcons.Pencil, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(20.dp))
                Text("ادامه قرارداد نیمه‌کاره", fontWeight = FontWeight.Bold, color = DfThemeColors.primary())
            }
            Text(
                item.internalNumber.ifBlank { item.title }.ifBlank { item.contractTypeLabel },
                style = AppTypography.cardTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.wizardStep.isNotBlank()) {
                Text("مرحله: ${item.wizardStep}", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            DfPrimaryButton(
                text = item.nextAction.label.ifBlank { "ادامه تکمیل قرارداد" },
                onClick = onContinue,
            )
        }
    }
}
