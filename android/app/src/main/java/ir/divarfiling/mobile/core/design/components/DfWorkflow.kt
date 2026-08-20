package ir.divarfiling.mobile.core.design.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

data class DfNbaAction(
    val title: String,
    val subtitle: String,
    val cta: String,
    val onClick: () -> Unit,
    val tone: DfStatusTone = DfStatusTone.Info,
)

@Composable
fun DfNbaCard(
    action: DfNbaAction,
    modifier: Modifier = Modifier,
) {
    val colors = action.tone.colors()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = colors.container,
        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.22f)),
        shadowElevation = AppElevations.subtle,
        onClick = action.onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = action.tone.defaultIcon(),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "قدم بعدی",
                    style = AppTypography.labelSmall,
                    color = colors.content.copy(alpha = 0.8f),
                )
                Text(
                    action.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    action.subtitle,
                    style = AppTypography.bodyDescription,
                    color = colors.content.copy(alpha = 0.86f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                action.cta,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
            )
        }
    }
}

@Composable
fun DfActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = DfThemeColors.primary(),
    icon: ImageVector = DfIcons.ChevronLeft,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}

data class DfCompactStat(
    val label: String,
    val value: String,
)

@Composable
fun DfCompactStatBar(
    stats: List<DfCompactStat>,
    modifier: Modifier = Modifier,
) {
    if (stats.isEmpty()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stats.forEachIndexed { index, stat ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        stat.value,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        stat.label,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (index < stats.lastIndex) {
                    BoxDivider()
                }
            }
        }
    }
}

@Composable
private fun BoxDivider() {
    Surface(
        modifier = Modifier
            .size(width = 1.dp, height = 28.dp),
        color = DfThemeColors.outlineSubtle(),
    ) {}
}

@Composable
fun DfContinueOnWebRow(
    title: String = "ادامه در میزکار وب",
    subtitle: String = "تحلیل عمیق‌تر در مرورگر انجام می‌شود",
    url: String = AppLinks.WORKSPACE,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    DfActionCard(
        title = title,
        subtitle = subtitle,
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
        modifier = modifier,
        icon = DfIcons.ExternalLink,
    )
}

@Composable
fun DfSheetAdvancedBlock(
    title: String = "جزئیات بیشتر",
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.primary(),
            )
            Icon(
                imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                contentDescription = null,
                tint = DfThemeColors.primary(),
                modifier = Modifier.size(16.dp),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                content()
            }
        }
    }
}

fun friendlyNetworkMessage(raw: String?): String {
    val text = raw.orEmpty()
    val lower = text.lowercase()
    return when {
        text.isBlank() -> "اتصال برقرار نشد. دوباره تلاش کنید."
        "timeout" in lower || "timed out" in lower -> "اتصال کند است. دوباره تلاش کنید."
        "unable to resolve" in lower || "unknownhost" in lower -> "اینترنت در دسترس نیست."
        "failed to connect" in lower || "connect" in lower && "fail" in lower -> "اتصال به سرور برقرار نشد."
        "401" in lower || "unauthorized" in lower -> "نشست منقضی شده. دوباره وارد شوید."
        else -> text
    }
}
