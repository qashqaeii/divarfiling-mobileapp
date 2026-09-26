package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.network.UserDto

@Composable
fun ProfileAvatarEditor(
    user: UserDto?,
    onChangeAvatar: () -> Unit,
    onGalleryPick: () -> Unit,
    onRemove: (() -> Unit)?,
    isUploadingGallery: Boolean,
    modifier: Modifier = Modifier,
    previewAvatarKey: String? = null,
    onSurface: Boolean = false,
) {
    val previewUser = remember(user, previewAvatarKey) {
        when {
            previewAvatarKey != null && user != null ->
                user.copy(
                    avatarKey = previewAvatarKey,
                    hasCustomAvatar = false,
                    avatarUrl = null,
                )
            else -> user
        }
    }
    val muted = if (onSurface) DfThemeColors.textMuted() else Color.White.copy(alpha = 0.78f)
    val label = if (onSurface) DfThemeColors.textSecondary() else Color.White.copy(alpha = 0.9f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                onClick = onChangeAvatar,
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(
                    2.5.dp,
                    if (onSurface) DfThemeColors.outlineSubtle() else Color.White.copy(alpha = 0.35f),
                ),
                shadowElevation = if (onSurface) 2.dp else 0.dp,
            ) {
                AppAvatar(
                    user = previewUser,
                    size = AppAvatarSize.Hero,
                    contentDescription = "آواتار پروفایل",
                )
            }
            Surface(
                onClick = onChangeAvatar,
                shape = CircleShape,
                color = DfColors.Purple,
                border = BorderStroke(2.dp, if (onSurface) DfThemeColors.surface() else Color.White),
                modifier = Modifier
                    .size(30.dp)
                    .offset(x = 4.dp, y = 4.dp),
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        DfIcons.Pencil,
                        contentDescription = "تغییر آواتار",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Text(
            text = "برای تغییر آواتار بزنید",
            style = AppTypography.labelSmall,
            color = label,
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onGalleryPick, enabled = !isUploadingGallery) {
                Text(
                    text = if (isUploadingGallery) "در حال بارگذاری…" else "گالری",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (onSurface) DfThemeColors.primary() else Color.White,
                )
            }
            if (onRemove != null && (user?.hasCustomAvatar == true || !user?.avatarKey.isNullOrBlank())) {
                TextButton(onClick = onRemove, enabled = !isUploadingGallery) {
                    Text(
                        text = "بازنشانی",
                        style = AppTypography.labelSmall,
                        color = muted,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsHeroAvatar(
    user: UserDto?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onAvatarClick,
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.28f)),
        modifier = modifier,
    ) {
        AppAvatar(
            user = user,
            size = AppAvatarSize.Large,
            contentDescription = "آواتار — تغییر",
        )
    }
}
