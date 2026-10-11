package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
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
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfComponentSpec
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.DealChecklistItemDto
import ir.divarfiling.mobile.core.network.DealDto

@Composable
fun DealDetailHeroCard(
    deal: DealDto,
    onContactClick: () -> Unit,
    onPropertyClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    stageDef: ir.divarfiling.mobile.core.network.DealStageDefDto? = null,
) {
    val stage = deal.stage ?: "سرنخ"
    val stageColors = DealUiUtils.dealStageColors(stage, stageDef)
    val progress = DealsFilters.progressPercent(deal)
    val accent = DealUiUtils.dealAccentColor(deal.customerName ?: deal.title)
    val (jalaliDate, jalaliTime) = DealsFilters.splitDateTime(deal.updatedAt)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
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
                    ir.divarfiling.mobile.core.design.components.AppAvatar(
                        resolved = ir.divarfiling.mobile.core.avatar.AvatarResolver.resolveContact(
                            deal.customerName,
                            null,
                        ),
                        size = ir.divarfiling.mobile.core.design.components.AppAvatarSize.Medium,
                    )
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
                                color = DfThemeColors.textPrimary(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        deal.propertyTitle?.takeIf { it.isNotBlank() }?.let { property ->
                            Text(
                                text = property,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
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
                            trackColor = DfThemeColors.surfaceVariant(),
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

                DealHeroMetricsRow(deal = deal, stage = stage, stageDef = stageDef)

                if (deal.isStale) {
                    Surface(shape = AppShapes.Chip, color = DfThemeColors.warningContainer()) {
                        Text(
                            text = "این معامله ${deal.daysInStage} روز در این مرحله مانده و نیاز به پیگیری دارد",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.warning(),
                        )
                    }
                }

                HorizontalDivider(color = DfThemeColors.outlineSubtle())

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
                        onClick = if (deal.propertyId != null && onPropertyClick != null) onPropertyClick else null,
                    )
                }
                deal.probability?.let { probability ->
                    DealDetailMetaRow(
                        icon = DfIcons.Compass,
                        label = "احتمال بسته‌شدن",
                        value = "$probability٪",
                    )
                }
                deal.expectedCloseDate?.takeIf { it.isNotBlank() }?.let { closeDate ->
                    DealDetailMetaRow(
                        icon = DfIcons.Calendar,
                        label = "تاریخ پیش‌بینی بستن",
                        value = closeDate,
                    )
                }
                deal.contractNumber?.takeIf { it.isNotBlank() }?.let { contract ->
                    DealDetailMetaRow(
                        icon = DfIcons.File,
                        label = "شماره قرارداد",
                        value = contract,
                    )
                }
                deal.commissionRate?.let { rate ->
                    DealDetailMetaRow(
                        icon = DfIcons.Percent,
                        label = "نرخ کمیسیون",
                        value = "${rate.toInt()}٪",
                    )
                }
                deal.lostReason?.takeIf { it.isNotBlank() }?.let { reason ->
                    DealDetailMetaRow(
                        icon = DfIcons.CircleAlert,
                        label = "دلیل از دست رفتن",
                        value = reason,
                        valueColor = DfThemeColors.warning(),
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
                        color = DfThemeColors.surfaceVariant().copy(alpha = 0.55f),
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
                                    tint = DfThemeColors.textMuted(),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = "یادداشت",
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfThemeColors.textMuted(),
                                )
                            }
                            Text(
                                text = notes,
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DealHeroMetricsRow(
    deal: DealDto,
    stage: String,
    stageDef: ir.divarfiling.mobile.core.network.DealStageDefDto?,
) {
    val dealValue = deal.contractAmount ?: deal.amount
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        DealHeroMetricCell(
            label = "ارزش",
            value = dealValue?.let { FormatUtils.formatPriceShort(it) } ?: "—",
            modifier = Modifier.weight(1f),
        )
        DealHeroMetricCell(
            label = "کمیسیون",
            value = if (deal.hasCommissionDisplay()) {
                FormatUtils.formatPriceShort(deal.displayCommissionTotal())
            } else {
                "—"
            },
            valueColor = if (deal.hasCommissionDisplay()) DfThemeColors.success() else DfThemeColors.textMuted(),
            modifier = Modifier.weight(1f),
        )
        DealHeroMetricCell(
            label = "احتمال",
            value = deal.probability?.let { "$it٪" } ?: "—",
            modifier = Modifier.weight(1f),
        )
    }
    Text(
        text = DealUiUtils.stageDescription(stage, stageDef),
        style = AppTypography.labelSmall,
        color = DfThemeColors.textMuted(),
    )
}

@Composable
private fun DealHeroMetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DfThemeColors.textPrimary(),
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            Text(
                value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    stageDefs: List<ir.divarfiling.mobile.core.network.DealStageDefDto> = emptyList(),
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
                color = DfThemeColors.textMuted(),
            )
            if (isSubmitting) {
                Text(
                    text = "در حال به‌روزرسانی…",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.primary(),
                )
            }
        }
        DealStagePipeline(
            stages = stages,
            currentStage = currentStage,
            isSubmitting = isSubmitting,
            onStageSelect = onStageSelect,
            stageDefs = stageDefs,
        )
    }
}

@Composable
fun DealDetailQuickActions(
    onEdit: () -> Unit,
    onFinance: () -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DealQuickAction(
            label = "ویرایش",
            icon = DfIcons.Pencil,
            background = DfThemeColors.primaryContainer(),
            iconTint = DfThemeColors.primary(),
            onClick = onEdit,
            modifier = Modifier.weight(1f),
        )
        DealQuickAction(
            label = "مالی",
            icon = DfIcons.Coins,
            background = DfThemeColors.successContainer(),
            iconTint = DfThemeColors.success(),
            onClick = onFinance,
            modifier = Modifier.weight(1f),
        )
        DealQuickAction(
            label = "حذف",
            icon = DfIcons.Trash,
            background = DfThemeColors.errorContainer(),
            iconTint = DfThemeColors.error(),
            onClick = onDelete,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun DealChecklistSection(
    items: List<DealChecklistItemDto>,
    onToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val doneCount = items.count { it.done }
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
                text = "چک‌لیست معامله",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textMuted(),
            )
            Text(
                text = "$doneCount از ${items.size}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.primary(),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Surface(
            shape = AppShapes.Card,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = AppSpacing.xs)) {
                items.forEachIndexed { index, item ->
                    val itemId = item.id ?: return@forEachIndexed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(itemId, !item.done) }
                            .padding(horizontal = AppSpacing.sm, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = item.done,
                            onCheckedChange = { checked -> onToggle(itemId, checked) },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label.orEmpty(),
                                style = AppTypography.bodyDescription,
                                fontWeight = if (item.done) FontWeight.Normal else FontWeight.Medium,
                                color = if (item.done) DfThemeColors.textMuted() else DfThemeColors.textPrimary(),
                            )
                            if (item.required) {
                                Text(
                                    text = "الزامی",
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.warning(),
                                )
                            }
                        }
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm),
                            color = DfThemeColors.outlineSubtle(),
                        )
                    }
                }
            }
        }
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
    valueColor: Color = DfThemeColors.textPrimary(),
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
                .clip(AppShapes.IconContainer)
                .background(DfThemeColors.surfaceVariant()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(15.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
            Text(
                text = value,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Medium,
                color = if (clickable) DfThemeColors.primary() else valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (clickable) {
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
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
        modifier = modifier.defaultMinSize(minHeight = DfComponentSpec.MinTouchTargetDp.dp),
        shape = AppShapes.Card,
        color = background.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = AppSpacing.sm),
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
