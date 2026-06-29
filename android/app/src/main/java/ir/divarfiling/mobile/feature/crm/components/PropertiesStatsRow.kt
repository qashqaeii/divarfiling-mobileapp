package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PropertiesStatsRow(
    totalCount: Int,
    saleCount: Int,
    rentCount: Int,
    activeCount: Int,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .height(52.dp)
            .clip(AppShapes.Card)
            .background(DfColors.Surface),
    ) {
        PropertiesStatCell(
            value = numberFormat.format(totalCount),
            label = "کل",
            icon = DfIcons.Building,
            tint = DfColors.Purple,
            modifier = Modifier.weight(1f),
        )
        PropertiesStatDivider()
        PropertiesStatCell(
            value = numberFormat.format(saleCount),
            label = "فروش",
            iconRes = DfDecorIcons.Coins,
            modifier = Modifier.weight(1f),
        )
        PropertiesStatDivider()
        PropertiesStatCell(
            value = numberFormat.format(rentCount),
            label = "اجاره",
            icon = DfIcons.Tag,
            tint = DfColors.Blue,
            modifier = Modifier.weight(1f),
        )
        PropertiesStatDivider()
        PropertiesStatCell(
            value = numberFormat.format(activeCount),
            label = "فعال",
            icon = DfIcons.Zap,
            tint = DfColors.Green,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PropertiesStatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    tint: Color = DfColors.Purple,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                iconRes != null -> Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    contentScale = ContentScale.Fit,
                )
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(11.dp),
                )
            }
            Text(
                text = value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
                maxLines = 1,
            )
        }
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PropertiesStatDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 10.dp)
            .size(width = 1.dp, height = 32.dp)
            .background(DfColors.Outline.copy(alpha = 0.25f)),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PropertiesStatsRowPreview() {
    DivarFilingTheme {
        PropertiesStatsRow(totalCount = 24, saleCount = 14, rentCount = 8, activeCount = 18)
    }
}
