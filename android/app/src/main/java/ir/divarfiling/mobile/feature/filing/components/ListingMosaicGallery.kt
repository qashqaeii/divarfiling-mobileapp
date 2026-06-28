package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfFullscreenImageViewer
import ir.divarfiling.mobile.core.design.components.DfImagePlaceholder
import ir.divarfiling.mobile.core.image.ImageUrlFormatter

@Composable
fun ListingMosaicGallery(
    images: List<String>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val urls = remember(images) {
        images.mapNotNull { ImageUrlFormatter.normalize(it) }.distinct()
    }
    var fullscreenIndex by remember { mutableIntStateOf(-1) }

    if (urls.isEmpty()) {
        DfImagePlaceholder(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = AppSpacing.screenHorizontal)
                .clip(AppShapes.Hero),
            shape = AppShapes.Hero,
        )
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
                .clip(AppShapes.Card)
                .clickable { fullscreenIndex = 0 },
        ) {
            DfAsyncImage(
                url = urls[0],
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.xs),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.55f),
            ) {
                Text(
                    text = "1 / ${urls.size}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            if (urls.size > 1) {
                GalleryThumb(
                    url = urls[1],
                    onClick = { fullscreenIndex = 1 },
                    modifier = Modifier.weight(1f),
                )
            }
            if (urls.size > 2) {
                val remaining = urls.size - 3
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(AppShapes.CardSmall)
                        .clickable { fullscreenIndex = 2 },
                ) {
                    DfAsyncImage(
                        url = urls[2],
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    if (remaining > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "+$remaining عکس",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            } else if (urls.size == 1) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }

    if (fullscreenIndex >= 0) {
        DfFullscreenImageViewer(
            urls = urls,
            initialIndex = fullscreenIndex,
            onDismiss = { fullscreenIndex = -1 },
        )
    }
}

@Composable
private fun GalleryThumb(
    url: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .clickable(onClick = onClick),
    ) {
        DfAsyncImage(
            url = url,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
