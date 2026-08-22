package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.filing.ListingImageUtils

/**
 * تصویر آگهی با fallback خودکار به URL بعدی در صورت خطای بارگذاری.
 * برای جایگزینی تصویر پیش‌فرض، فایل هم‌نام را در res/drawable قرار دهید.
 */
@Composable
fun DfListingImage(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    images: List<String> = emptyList(),
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(0.dp),
    contentDescription: String? = null,
) {
    val candidates = remember(thumbnailUrl, images) {
        ListingImageUtils.buildGalleryUrls(thumbnailUrl, images)
    }
    var activeIndex by remember(candidates) { mutableIntStateOf(0) }
    val activeUrl = candidates.getOrNull(activeIndex)

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        if (activeUrl.isNullOrBlank()) {
            DfListingImagePlaceholder(contentDescription = contentDescription)
        } else {
            SubcomposeAsyncImage(
                model = activeUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        DfImageShimmer(modifier = Modifier.fillMaxSize(), shape = shape)
                    }
                    is AsyncImagePainter.State.Error -> {
                        val hasNext = activeIndex + 1 < candidates.size
                        if (hasNext) {
                            LaunchedEffect(activeUrl) {
                                activeIndex++
                            }
                            DfImageShimmer(modifier = Modifier.fillMaxSize(), shape = shape)
                        } else {
                            DfListingImagePlaceholder(contentDescription = contentDescription)
                        }
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
        }
    }
}

@Composable
private fun DfListingImagePlaceholder(contentDescription: String?) {
    Image(
        painter = painterResource(R.drawable.img_listing_placeholder),
        contentDescription = contentDescription,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
