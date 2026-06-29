package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.DfColors

/** Decorative PNG icons in `res/drawable-nodpi`. */
object DfDecorIcons {
    val ClipboardList: Int = R.drawable.clipboard_list
    val Coins: Int = R.drawable.coins
    val Download: Int = R.drawable.download
    val FileText: Int = R.drawable.file_text
    val Layers: Int = R.drawable.layers
    val MapPin: Int = R.drawable.map_pin
    val Rocket: Int = R.drawable.rocket
    val Ruler: Int = R.drawable.ruler
    val Share2: Int = R.drawable.share_2
    val Sparkles: Int = R.drawable.sparkles
    val StickyNote: Int = R.drawable.sticky_note
    val Upload: Int = R.drawable.upload
}

object DfDecorSize {
    val Tiny = 14.dp
    val Small = 20.dp
    val Medium = 28.dp
    val Large = 36.dp
    val Card = 44.dp
    val Hero = 72.dp
    val Illustration = 88.dp
}

@Composable
fun DfDecorImage(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = DfDecorSize.Medium,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun DfExportLinkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "خروجی Excel / JSON / CSV",
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        DfDecorImage(
            resId = DfDecorIcons.Download,
            size = 18.dp,
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(label)
    }
}

@Composable
fun DfDecorIconBox(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    imageSize: Dp = 24.dp,
    background: Color = DfColors.PurpleContainer,
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .background(background, AppShapes.IconContainer),
        contentAlignment = Alignment.Center,
    ) {
        DfDecorImage(resId = resId, size = imageSize)
    }
}
