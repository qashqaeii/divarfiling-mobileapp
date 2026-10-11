package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.ContractDetailDto

@Composable
fun ContractDetailHero(
    detail: ContractDetailDto,
    modifier: Modifier = Modifier,
) {
    val (statusBg, statusFg) = contractStatusThemeColors(detail.statusLabel)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(statusFg),
            )
            Column(
                modifier = Modifier.padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = detail.title.ifBlank { detail.contractTypeLabel },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DfBadge(text = detail.contractTypeLabel, color = DfThemeColors.primaryContainer(), textColor = DfThemeColors.onPrimaryContainer())
                    DfBadge(text = detail.statusLabel, color = statusBg, textColor = statusFg)
                }
                if (detail.internalNumber.isNotBlank()) {
                    Text(
                        "شماره داخلی: ${detail.internalNumber}",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                detail.updatedAt?.let { iso ->
                    DateUtils.parseInstantMillis(iso)?.let { ms ->
                        Text(
                            "آخرین بروزرسانی: ${DateUtils.formatJalaliDateTimeLatinFromMillis(ms)}",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContractDetailNextActionCard(
    label: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.primaryContainer().copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DfThemeColors.primary().copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(DfThemeColors.primary()),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    "اقدام بعدی",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
                Text(
                    label.ifBlank { "ادامه فرایند قرارداد" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                )
                DfPrimaryButton(
                    text = label.ifBlank { "ادامه" },
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ContractDetailSummaryCard(
    finHighlight: String?,
    propertyLine: String,
    partyLine: String?,
    onOpenDeal: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("خلاصه پرونده", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfThemeColors.textPrimary())
            finHighlight?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
            if (propertyLine.isNotBlank()) {
                Text("ملک: $propertyLine", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            partyLine?.let {
                Text("طرف: $it", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            onOpenDeal?.let { open ->
                Text(
                    text = "مشاهده معامله مرتبط",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.primary(),
                    modifier = Modifier
                        .clickable(onClick = open)
                        .padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun ContractDetailExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfThemeColors.textPrimary())
                Icon(
                    imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                    contentDescription = null,
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier.size(18.dp),
                )
            }
            if (expanded) {
                HorizontalDivider(color = DfThemeColors.outlineSubtle())
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun contractStatusThemeColors(statusLabel: String): Pair<Color, Color> {
    val label = statusLabel.trim()
    return when {
        label.contains("پیش") -> DfThemeColors.warningContainer() to DfThemeColors.warning()
        label.contains("نهای") || label.contains("امض") -> DfThemeColors.successContainer() to DfThemeColors.success()
        label.contains("انتظار") -> DfThemeColors.infoContainer() to DfThemeColors.info()
        else -> DfThemeColors.primaryContainer() to DfThemeColors.primary()
    }
}

@Composable
fun ContractDetailLabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), modifier = Modifier.weight(0.42f))
        Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.58f), color = DfThemeColors.textPrimary())
    }
}
