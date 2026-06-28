package ir.divarfiling.mobile.core.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@Composable
fun DfAsyncImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(0.dp),
    contentDescription: String? = null,
) {
    val normalized = url?.trim()?.takeIf { it.isNotBlank() }
    if (normalized == null) {
        DfImagePlaceholder(modifier = modifier, shape = shape)
        return
    }

    var retryKey by remember(normalized) { mutableIntStateOf(0) }

    SubcomposeAsyncImage(
        model = "$normalized?retry=$retryKey",
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        onState = { /* handled in subcompose */ },
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> DfImageShimmer(modifier = Modifier.matchParentSize(), shape = shape)
            is AsyncImagePainter.State.Error -> DfImageError(
                modifier = Modifier.matchParentSize(),
                shape = shape,
                onRetry = { retryKey++ },
            )
            else -> SubcomposeAsyncImageContent()
        }
    }
}

@Composable
fun DfImageShimmer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    val transition = rememberInfiniteTransition(label = "imageShimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerOffset",
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            DfColors.SurfaceVariant,
            DfColors.Surface,
            DfColors.SurfaceVariant,
        ),
        start = Offset(offset - 400f, 0f),
        end = Offset(offset, 400f),
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

@Composable
fun DfImagePlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(DfColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            DfIcons.Building,
            contentDescription = null,
            tint = DfColors.TextMuted,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun DfImageError(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(DfColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = "تلاش مجدد", tint = DfColors.Purple)
        }
    }
}
