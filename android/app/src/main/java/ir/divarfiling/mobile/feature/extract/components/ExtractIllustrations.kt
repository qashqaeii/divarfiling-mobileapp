package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDecorSize

@Composable
fun ExtractHeroIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 96.dp, height = 88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DfColors.Purple.copy(alpha = 0.12f)),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFF5F3FF))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.Folder,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(28.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 6.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DfColors.PurpleContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.Home,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(12.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp, y = (-6).dp)
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DfColors.BlueLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.File,
                contentDescription = null,
                tint = DfColors.Blue,
                modifier = Modifier.size(11.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 10.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(DfColors.GreenLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.BarChart,
                contentDescription = null,
                tint = DfColors.Green,
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

@Composable
fun ExtractMapIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 96.dp, height = 108.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 96.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DfColors.BlueLight.copy(alpha = 0.85f),
                            DfColors.PurpleContainer.copy(alpha = 0.7f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.42f)),
        )
        DfDecorImage(
            resId = DfDecorIcons.MapPin,
            size = DfDecorSize.Hero,
            modifier = Modifier
                .shadow(10.dp, CircleShape)
                .offset(y = (-4).dp),
        )
    }
}
