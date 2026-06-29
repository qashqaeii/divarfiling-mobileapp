package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.network.DealDto

@Composable
fun DealDetailHeroCard(
    deal: DealDto,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = deal.stage ?: "سرنخ"
    val stageColors = DealUiUtils.dealStageColors(stage)
    val progress = DealsFilters.progressPercent(deal)
    val accent = DealUiUtils.dealAccentColor(deal.customerName ?: deal.title)
    val (jalaliDate, jalaliTime) = DealsFilters.splitDateTime(deal.updatedAt)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(stageColors.first, stageColors.first.copy(alpha = 0.45f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(accent, accent.copy(alpha = 0.65f)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = DealsFilters.customerInitials(deal.customerName),
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DealDetailStageBadge(stage = stage, colors = stageColors)
                        deal.customerName?.takeIf { it.isNotBlank() }?.let { name ->
                            Text(
                                text = name,
                                style = AppTypography.cardTitle,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.size(52.dp),
                            color = stageColors.first,
                            trackColor = DfColors.SurfaceVariant,
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = "$progress٪",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stageColors.first,
                        )
                    }
                }

                deal.amount?.let { amount ->
                    Text(
                        text = FormatUtils.formatPriceToman(amount),
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                } ?: Text(
                    text = "مبلغ ثبت نشده",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextMuted,
                )

                Text(
                    text = "احتمال بسته‌شدن بر اساس مرحله فروش",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )

                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.15f))

                DealDetailMetaRow(
                    icon = DfIcons.User,
                    label = "مخاطب",
                    value = deal.customerName ?: "—",
                    onClick = if (deal.customerId != null) onContactClick else null,
                )
                deal.propertyTitle?.takeIf { it.isNotBlank() }?.let { property ->
                    DealDetailMetaRow(
                        icon = DfIcons.Building,
                        label = "ملک",
                        value = property,
                    )
                }
                deal.commissionAmount?.let { commission ->
                    DealDetailMetaRow(
                        icon = DfIcons.Coins,
                        label = "کمیسیون",
                        value = FormatUtils.formatPriceToman(commission),
                        valueColor = DfColors.Green,
                    )
                }
                DealDetailMetaRow(
                    icon = DfIcons.Clock,
                    label = "آخرین به‌روزرسانی",
                    value = if (jalaliTime != "—") "$jalaliDate — $jalaliTime" else jalaliDate,
                )

                deal.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Surface(
                        shape = AppShapes.CardSmall,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = DfIcons.StickyNote,
                                    contentDescription = null,
                                    tint = DfColors.TextMuted,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "یادداشت",
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfColors.TextMuted,
                                )
                            }
                            Text(
                                text = notes,
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealStageSection(
    stages: List<String>,
    currentStage: String?,
    isSubmitting: Boolean,
    onStageSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "مرحله فروش",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextMuted,
            )
            if (isSubmitting) {
                Text(
                    text = "در حال به‌روزرسانی…",
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                )
            }
        }
        DealStagePipeline(
            stages = stages,
            currentStage = currentStage,
            isSubmitting = isSubmitting,
            onStageSelect = onStageSelect,
        )
    }
}

@Composable
fun DealDetailQuickActions(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DealQuickAction(
            label = "ویرایش",
            icon = DfIcons.File,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
            onClick = onEdit,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DealDetailStageBadge(
    stage: String,
    colors: Pair<Color, Color>,
) {
    Surface(shape = AppShapes.Chip, color = colors.second) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.first),
            )
            Text(
                text = stage,
                style = AppTypography.labelSmall,
                color = colors.first,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DealDetailMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = DfColors.TextPrimary,
    onClick: (() -> Unit)? = null,
) {
    val clickable = onClick != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) {
                    Modifier
                        .clip(AppShapes.CardSmall)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                        .padding(vertical = 2.dp)
                } else {
                    Modifier
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .liquidGlassSurface(
                    shape = AppShapes.IconContainer,
                    variant = DfGlassButtonVariant.Secondary,
                    elevation = 1.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(15.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            Text(
                text = value,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Medium,
                color = if (clickable) DfColors.Purple else valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (clickable) {
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun DealQuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = background.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = AppSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = iconTint,
            )
        }
    }
}
