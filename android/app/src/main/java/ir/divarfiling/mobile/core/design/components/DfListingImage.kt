package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.filing.ListingImageUtils
import ir.divarfiling.mobile.core.image.ImageUrlFormatter

/**
 * تصویر آگهی با fallback به [R.drawable.img_listing_placeholder].
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
    val resolved = ListingImageUtils.buildGalleryUrls(thumbnailUrl, images).firstOrNull()
        ?: ImageUrlFormatter.normalize(thumbnailUrl)

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        if (resolved.isNullOrBlank()) {
            Image(
                painter = painterResource(R.drawable.img_listing_placeholder),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            DfAsyncImage(
                url = resolved,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                shape = shape,
                contentDescription = contentDescription,
            )
        }
    }
}
