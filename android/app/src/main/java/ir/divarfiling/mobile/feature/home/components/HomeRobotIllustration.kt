package ir.divarfiling.mobile.feature.home.components

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

@Composable
fun HomeRobotIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DfColors.Purple.copy(alpha = 0.25f)),
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.White.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, Color(0xFFF5F3FF)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DfIcons.Bot,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(36.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(DfColors.Purple),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(DfColors.PurpleLight),
        )
    }
}
