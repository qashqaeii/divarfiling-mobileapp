package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FilingStatsRow(
    totalAds: Int,
    filesCount: Int,
    estimatedSizeGb: Double,
    datasetsThisMonth: Int,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        FilingStatCard(
            value = numberFormat.format(totalAds),
            label = "از کل فایل‌ها",
            title = "کل آگهی‌ها",
            icon = DfIcons.ClipboardList,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
        )
        FilingStatCard(
            value = numberFormat.format(filesCount),
            label = "تعداد فایل",
            title = "فایل‌ها",
            icon = DfIcons.Folder,
            background = DfColors.GreenLight,
            iconTint = DfColors.Green,
        )
        FilingStatCard(
            value = String.format(Locale.US, "%.1f GB", estimatedSizeGb),
            label = "فضای استفاده‌شده",
            title = "حجم کل",
            icon = DfIcons.Database,
            background = DfColors.BlueLight,
            iconTint = DfColors.Blue,
        )
        FilingStatCard(
            value = numberFormat.format(datasetsThisMonth),
            label = "دیتاست‌های ایجادشده",
            title = "ماه جاری",
            icon = DfIcons.Calendar,
            background = DfColors.AmberLight,
            iconTint = DfColors.Amber,
        )
    }
}

@Composable
private fun FilingStatCard(
    value: String,
    label: String,
    title: String,
    icon: ImageVector,
    background: Color,
    iconTint: Color,
) {
    Surface(
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = background,
                modifier = Modifier.size(40.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = value,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
