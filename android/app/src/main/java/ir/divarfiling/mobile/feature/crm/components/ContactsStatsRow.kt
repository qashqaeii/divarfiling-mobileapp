package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
fun ContactsStatsRow(
    todayCount: Int,
    newCount: Int,
    followUpCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ContactsStatCard(
            value = numberFormat.format(totalCount),
            label = "کل",
            icon = DfIcons.Users,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
        )
        ContactsStatCard(
            value = numberFormat.format(followUpCount),
            label = "پیگیری",
            icon = DfIcons.Phone,
            background = DfColors.GreenLight,
            iconTint = DfColors.Green,
        )
        ContactsStatCard(
            value = numberFormat.format(newCount),
            label = "جدید",
            icon = DfIcons.UserPlus,
            background = DfColors.BlueLight,
            iconTint = DfColors.Blue,
        )
        ContactsStatCard(
            value = numberFormat.format(todayCount),
            label = "امروز",
            icon = DfIcons.Calendar,
            background = DfColors.AmberLight,
            iconTint = DfColors.Amber,
        )
    }
}

@Composable
private fun ContactsStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    background: Color,
    iconTint: Color,
) {
    Surface(
        shape = AppShapes.Card,
        color = background,
        shadowElevation = AppElevations.none,
        modifier = Modifier.size(width = 96.dp, height = 88.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}
