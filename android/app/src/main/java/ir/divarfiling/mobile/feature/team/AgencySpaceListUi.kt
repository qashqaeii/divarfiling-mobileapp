package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.AgencySpaceItemDto

@Composable
fun AgencySpaceTabRow(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        items(AgencySpaceTab.labels, key = { it.first }) { (key, label) ->
            DfSoftChip(
                text = label,
                selected = selectedTab == key,
                onClick = { onTabSelected(key) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySpaceItemCard(
    item: AgencySpaceItemDto,
    isSubmitting: Boolean,
    selectMode: Boolean = false,
    selected: Boolean = false,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onViewCopied: (kind: String, targetId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isProperty = item.property != null
    val title = item.property?.title ?: item.customer?.name ?: "—"
    val region = item.property?.region ?: item.customer?.region.orEmpty()
    val priceOrBudget = item.property?.priceLabel ?: item.customer?.budgetLabel.orEmpty()
    val metaLine = listOfNotNull(
        item.property?.dealMode?.takeIf { it.isNotBlank() },
        item.customer?.needSummary?.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
    val copyKind = if (isProperty) "property" else "customer"
    val typeLabel = if (isProperty) "فایل" else "مخاطب"
    val accent = if (isProperty) DfThemeColors.info() else DfThemeColors.primary()
    DfCard(modifier = modifier, onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isProperty) {
                    DfListingImage(
                        thumbnailUrl = item.property?.thumbnailUrl,
                        modifier = Modifier.size(width = 72.dp, height = 64.dp),
                        shape = RoundedCornerShape(AppSpacing.sm),
                        contentDescription = title,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectMode) {
                            Checkbox(checked = selected, onCheckedChange = { onOpen() })
                        }
                        DfBadge(
                            text = typeLabel,
                            color = accent.copy(alpha = 0.12f),
                            textColor = accent,
                        )
                        Text(
                            title,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (priceOrBudget.isNotBlank()) {
                        Text(
                            priceOrBudget,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.textPrimary(),
                        )
                    } else if (metaLine.isNotBlank()) {
                        Text(
                            metaLine,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (region.isNotBlank()) {
                        Text(
                            region,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            item.publisher.name.ifBlank { "—" },
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        if (item.publishedAtDisplay.isNotBlank()) {
                            Text(
                                item.publishedAtDisplay,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AgencySpaceVisibilityBadge(
                            visibility = item.visibility,
                            label = item.visibilityLabel,
                        )
                        if (item.alreadyCopied) {
                            DfBadge(
                                text = if (isProperty) "در فایل‌های من" else "در CRM من",
                                color = DfThemeColors.successContainer(),
                                textColor = DfThemeColors.success(),
                            )
                        }
                    }
                    when {
                        item.alreadyCopied && item.copyTargetId != null -> {
                            DfSecondaryButton(
                                text = if (isProperty) "مشاهده نسخه من" else "مشاهده مخاطب",
                                onClick = { onViewCopied(copyKind, item.copyTargetId!!) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item.canCopy && !item.alreadyCopied -> {
                            DfPrimaryButton(
                                text = when {
                                    isSubmitting -> "…"
                                    isProperty -> "کپی به فایل‌های من"
                                    else -> "کپی به CRM من"
                                },
                                onClick = onCopy,
                                enabled = !isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgencySpaceDetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(title, style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
