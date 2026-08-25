package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface

/** Maps FontAwesome-style icon keys (from API) to Lucide vectors in the Android design system. */
fun folderIconVector(faIcon: String): ImageVector = when (faIcon) {
    "fa-book" -> DfIcons.Book
    "fa-book-open" -> DfIcons.BookOpen
    "fa-folder" -> DfIcons.Folder
    "fa-folder-open" -> DfIcons.Folder
    "fa-building" -> DfIcons.Building
    "fa-city" -> DfIcons.Building
    "fa-key" -> DfIcons.KeyRound
    "fa-star" -> DfIcons.Star
    "fa-briefcase" -> DfIcons.Briefcase
    "fa-layer-group" -> DfIcons.Layers
    "fa-house-chimney" -> DfIcons.Home
    "fa-landmark" -> DfIcons.Landmark
    "fa-map-pin" -> DfIcons.MapPin
    "fa-tag" -> DfIcons.Tag
    else -> DfIcons.Folder
}

@Composable
fun ZonkanBrandIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    DfDecorImage(
        resId = DfDecorIcons.Zonkan,
        size = size,
        modifier = modifier,
        contentDescription = "زونکن",
    )
}

/** Tinted glass fill for zonkan folder cards — stronger when selected. */
fun zonkanFolderGlassBrush(color: Color, selected: Boolean): Brush {
    return if (selected) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.68f),
                color.copy(alpha = 0.30f),
                color.copy(alpha = 0.18f),
                color.copy(alpha = 0.10f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.58f),
                color.copy(alpha = 0.14f),
                color.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.32f),
            ),
        )
    }
}

private fun zonkanFolderGlassBorderBrush(color: Color, selected: Boolean): Brush =
    Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (selected) 0.78f else 0.58f),
            color.copy(alpha = if (selected) 0.62f else 0.30f),
            color.copy(alpha = if (selected) 0.42f else 0.20f),
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

/** Glass card surface tinted with the folder color the user picked. */
fun Modifier.zonkanFolderGlassSurface(
    color: Color,
    selected: Boolean,
    shape: Shape = AppShapes.CardSmall,
): Modifier {
    val shadowColor = color.copy(alpha = if (selected) 0.30f else 0.11f)
    val elevation = if (selected) AppElevations.floating else AppElevations.card
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor,
        )
        .clip(shape)
        .background(zonkanFolderGlassBrush(color = color, selected = selected))
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (selected) 0.40f else 0.30f),
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                start = Offset(0f, 0f),
                end = Offset(220f, 140f),
            ),
        )
        .border(
            width = if (selected) 1.5.dp else 1.dp,
            brush = zonkanFolderGlassBorderBrush(color = color, selected = selected),
            shape = shape,
        )
}

@Composable
fun PropertyFolderIconBadge(
    faIcon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .liquidGlassSurface(
                shape = CircleShape,
                variant = DfGlassButtonVariant.Accent,
                accent = color,
                elevation = AppElevations.none,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = folderIconVector(faIcon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize),
        )
    }
}
