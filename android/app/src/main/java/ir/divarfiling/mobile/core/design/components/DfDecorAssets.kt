package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    // Navigation tabs
    val House: Int = R.drawable.house
    val Folder: Int = R.drawable.folder
    val Users: Int = R.drawable.users
    val Handshake: Int = R.drawable.handshake
    val Settings: Int = R.drawable.settings

    // Headers & stats
    val Bell: Int = R.drawable.bell
    val Calendar: Int = R.drawable.calendar
    val Database: Int = R.drawable.database
    val Building: Int = R.drawable.building
    val Tag: Int = R.drawable.tag
    val Zap: Int = R.drawable.zap
    val Search: Int = R.drawable.search
    val Timer: Int = R.drawable.timer
    val ListTodo: Int = R.drawable.list_todo
    val User: Int = R.drawable.user
    val TrendingUp: Int = R.drawable.trending_up
    val Filter: Int = R.drawable.filter

    // Property amenities
    val Car: Int = R.drawable.car
    val Storage: Int = R.drawable.storage
    val Elevator: Int = R.drawable.elevator

    // Quick actions
    val Phone: Int = R.drawable.phone
    val ExternalLink: Int = R.drawable.external_link
    val Copy: Int = R.drawable.copy
    val FileEdit: Int = R.drawable.file_edit

    // Smart tools
    val Calculator: Int = R.drawable.calculator
    val RotateCcw: Int = R.drawable.rotate_ccw
    val Scale: Int = R.drawable.scale
    val Percent: Int = R.drawable.percent
    val BarChart: Int = R.drawable.bar_chart
    val LayoutGrid: Int = R.drawable.layout_grid

    val HomeIllustrationRobot: Int = R.drawable.home_illustration_robot
}

object DfDecorSize {
    val Tiny = 14.dp
    val Small = 20.dp
    val Medium = 28.dp
    val Large = 36.dp
    val Card = 44.dp
    val Hero = 72.dp
    val Illustration = 88.dp
    val NavSide = 22.dp
    val NavCenter = 26.dp
}

@Composable
fun DfDecorImage(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = DfDecorSize.Medium,
    contentDescription: String? = null,
    alpha: Float = 1f,
) {
    Image(
        painter = painterResource(resId),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .alpha(alpha),
        contentScale = ContentScale.Fit,
    )
}

/** Renders a decor PNG or a tinted vector icon in a single slot. */
@Composable
fun DfIconSlot(
    modifier: Modifier = Modifier,
    size: Dp = DfDecorSize.Small,
    tint: Color = Color.Unspecified,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    tintIconRes: Boolean = false,
    contentDescription: String? = null,
    alpha: Float = 1f,
) {
    when {
        iconRes != null && tintIconRes -> Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier
                .size(size)
                .alpha(alpha),
        )
        iconRes != null -> DfDecorImage(
            resId = iconRes,
            size = size,
            modifier = modifier,
            contentDescription = contentDescription,
            alpha = alpha,
        )
        icon != null -> Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier
                .size(size)
                .alpha(alpha),
        )
    }
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

@Composable
fun DfDecorIconButton(
    @DrawableRes resId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    imageSize: Dp = 22.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        DfDecorImage(resId = resId, size = imageSize, contentDescription = contentDescription)
    }
}
