package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.network.ListingFeatureGroupDto
import ir.divarfiling.mobile.core.network.ListingFeatureItemDto
import ir.divarfiling.mobile.core.network.ListingFeatureProfileDto

/**
 * پنل‌های گروه‌بندی مشخصات تفصیلی — فایلینگ، فایل شخصی و CRM.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureProfilePanels(
    profile: ListingFeatureProfileDto?,
    highlights: List<String> = emptyList(),
    title: String = "مشخصات کامل ملک",
    subtitle: String = "جزئیات ساختمان، امکانات، سند و شرایط سکونت",
    modifier: Modifier = Modifier,
    emptyMessage: String? = null,
) {
    val hasProfile = profile?.hasDetails == true ||
        profile?.core?.any { !it.value.isNullOrBlank() && it.value != "—" } == true ||
        profile?.groups?.any { it.items.isNotEmpty() } == true
    val hasHighlights = highlights.isNotEmpty()

    if (!hasProfile && !hasHighlights) {
        if (emptyMessage != null) {
            Column(
                modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                FeatureProfileHeader(title = title, subtitle = subtitle)
                DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = emptyMessage,
                        modifier = Modifier.padding(AppSpacing.md),
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        FeatureProfileHeader(title = title, subtitle = subtitle)

        if (hasHighlights) {
            DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DfDecorImage(resId = DfDecorIcons.Sparkles, size = 16.dp)
                        Text(
                            text = "نکات برجسته",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        highlights.forEach { highlight ->
                            DfBadge(text = highlight)
                        }
                    }
                }
            }
        }

        profile?.core?.filter { !it.value.isNullOrBlank() && it.value != "—" }.orEmpty().let { core ->
            if (core.isNotEmpty()) {
                FeatureGroupPanel(
                    title = "مشخصات اصلی",
                    iconRes = DfDecorIcons.Ruler,
                    items = core.map { item ->
                        FeatureRowItem(
                            label = item.label ?: item.key.orEmpty(),
                            value = item.value.orEmpty(),
                            state = item.state,
                        )
                    },
                )
            }
        }

        profile?.groups?.filter { group ->
            group.items.any { !it.value.isNullOrBlank() && it.value != "—" }
        }.orEmpty().forEach { group ->
            FeatureGroupPanel(
                title = group.title ?: "جزئیات",
                iconRes = featureGroupIconRes(group.id, group.title),
                items = group.items
                    .filter { !it.value.isNullOrBlank() && it.value != "—" }
                    .map { item ->
                        FeatureRowItem(
                            label = item.label ?: item.key.orEmpty(),
                            value = formatFeatureItemValue(item),
                            state = item.state,
                            chips = item.chips,
                        )
                    },
            )
        }
    }
}

@Composable
fun DossierGroupPanel(
    title: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    icon: ImageVector? = null,
    accent: Color = DfColors.Purple,
) {
    if (rows.isEmpty()) return

    FeatureGroupPanel(
        title = title,
        iconRes = iconRes,
        icon = icon,
        accent = accent,
        items = rows.map { (label, value) -> FeatureRowItem(label = label, value = value) },
        modifier = modifier,
    )
}

@Composable
private fun FeatureProfileHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = AppTypography.sectionTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
        )
        Text(
            text = subtitle,
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textMuted(),
        )
    }
}

private data class FeatureRowItem(
    val label: String,
    val value: String,
    val state: String? = null,
    val chips: List<String> = emptyList(),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureGroupPanel(
    title: String,
    items: List<FeatureRowItem>,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    icon: ImageVector? = null,
    accent: Color = DfColors.Purple,
) {
    if (items.isEmpty()) return

    DfPremiumCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    iconRes != null -> DfDecorImage(resId = iconRes, size = 18.dp)
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    if (item.chips.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = item.label,
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                item.chips.forEach { chip -> DfBadge(text = chip) }
                            }
                        }
                    } else {
                        FeatureSpecRow(
                            label = item.label,
                            value = item.value,
                            state = item.state,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureSpecRow(
    label: String,
    value: String,
    state: String? = null,
    modifier: Modifier = Modifier,
) {
    val stateColor = when (state) {
        "yes" -> DfColors.Green
        "no" -> DfColors.OverdueAccent
        else -> DfThemeColors.textPrimary()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                modifier = Modifier.weight(0.42f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = stateColor,
                modifier = Modifier.weight(0.58f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatFeatureItemValue(item: ListingFeatureItemDto): String {
    if (item.chips.isNotEmpty()) return item.chips.joinToString("، ")
    return item.value.orEmpty()
}

@DrawableRes
private fun featureGroupIconRes(groupId: String?, title: String?): Int {
    val id = groupId?.lowercase().orEmpty()
    val t = title.orEmpty()
    return when {
        id == "building" || "ساختمان" in t -> DfDecorIcons.Building
        id == "comfort" || "رفاهی" in t || "امکانات" in t -> DfDecorIcons.LayoutGrid
        id == "lifestyle" || "سکونت" in t -> DfDecorIcons.Users
        id == "legal" || "سند" in t -> DfDecorIcons.FileText
        id == "counts" || "تعداد" in t -> DfDecorIcons.ListTodo
        id == "luxury" || "لوکس" in t -> DfDecorIcons.Sparkles
        id == "exterior" || "نما" in t || "ابعاد" in t -> DfDecorIcons.Building
        else -> DfDecorIcons.Layers
    }
}
