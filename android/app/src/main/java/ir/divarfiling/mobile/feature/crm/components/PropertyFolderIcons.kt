package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfIcons

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
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
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
