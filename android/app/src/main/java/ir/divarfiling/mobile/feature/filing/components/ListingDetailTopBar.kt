package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailTopBar(
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isFavorite: Boolean,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = AppShapes.IconContainer,
            color = DfColors.Surface.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = DfIcons.ChevronLeft,
                    contentDescription = "بازگشت",
                    tint = DfColors.TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.xxs)) {
            TopBarIconButton(
                icon = DfIcons.Heart,
                contentDescription = "علاقه‌مندی",
                onClick = onFavoriteToggle,
                tint = if (isFavorite) DfColors.Rose else DfColors.TextSecondary,
            )
            TopBarIconButton(
                icon = DfIcons.Share2,
                contentDescription = "اشتراک",
                onClick = onShare,
            )
            Box {
                TopBarIconButton(
                    icon = DfIcons.MoreVertical,
                    contentDescription = "بیشتر",
                    onClick = { showMenu = true },
                )
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("کپی لینک") },
                        onClick = {
                            showMenu = false
                            onCopyLink()
                        },
                        leadingIcon = {
                            Icon(DfIcons.Copy, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("اشتراک‌گذاری") },
                        onClick = {
                            showMenu = false
                            onShare()
                        },
                        leadingIcon = {
                            Icon(DfIcons.Share2, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = DfColors.TextSecondary,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.IconContainer,
        color = DfColors.Surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
