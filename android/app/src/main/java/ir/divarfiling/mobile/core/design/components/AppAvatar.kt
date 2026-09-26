package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.avatar.AvatarCatalog
import ir.divarfiling.mobile.core.avatar.AvatarResolver
import ir.divarfiling.mobile.core.avatar.ResolvedAvatar
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.UserDto

enum class AppAvatarSize(val dp: Dp) {
    Small(32.dp),
    Medium(44.dp),
    Picker(60.dp),
    Large(56.dp),
    XLarge(72.dp),
    Hero(84.dp),
}

@Composable
fun AppAvatar(
    user: UserDto?,
    modifier: Modifier = Modifier,
    size: AppAvatarSize = AppAvatarSize.Medium,
    selected: Boolean = false,
    showOnlineIndicator: Boolean = false,
    contentDescription: String = "آواتار",
) {
    AppAvatar(
        resolved = AvatarResolver.resolveUser(user),
        modifier = modifier,
        size = size,
        selected = selected,
        showOnlineIndicator = showOnlineIndicator,
        contentDescription = contentDescription,
    )
}

@Composable
fun AppContactAvatar(
    contact: ContactDto,
    modifier: Modifier = Modifier,
    size: AppAvatarSize = AppAvatarSize.Medium,
    selected: Boolean = false,
    contentDescription: String = "آواتار مخاطب",
) {
    AppAvatar(
        resolved = AvatarResolver.resolveContact(contact),
        modifier = modifier,
        size = size,
        selected = selected,
        contentDescription = contentDescription,
    )
}

@Composable
fun AppAvatar(
    resolved: ResolvedAvatar,
    modifier: Modifier = Modifier,
    size: AppAvatarSize = AppAvatarSize.Medium,
    selected: Boolean = false,
    showOnlineIndicator: Boolean = false,
    placeholderInitials: String? = null,
    contentDescription: String = "آواتار",
) {
    val dimension = size.dp
    val borderColor = if (selected) DfColors.Purple else Color.Transparent
    val ring = if (selected) BorderStroke(2.5.dp, borderColor) else null
    val ringScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "avatarRingScale",
    )

    Box(
        modifier = modifier
            .size(dimension)
            .graphicsLayer {
                scaleX = ringScale
                scaleY = ringScale
            }
            .semantics { this.contentDescription = contentDescription },
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DfColors.Purple.copy(alpha = 0.1f)),
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (selected) 3.dp else 0.dp),
            shape = CircleShape,
            color = DfThemeColors.primaryContainer(),
            border = ring,
            shadowElevation = if (selected) 2.dp else 0.dp,
        ) {
            when {
                !resolved.remoteUrl.isNullOrBlank() -> {
                    DfAsyncImage(
                        url = resolved.remoteUrl,
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        contentDescription = null,
                    )
                }
                resolved.presetDrawable != null -> {
                    Image(
                        painter = painterResource(resolved.presetDrawable),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
                !placeholderInitials.isNullOrBlank() -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            placeholderInitials,
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.onPrimaryContainer(),
                        )
                    }
                }
                else -> {
                    Image(
                        painter = painterResource(AvatarCatalog.drawableForKey(AvatarCatalog.DEFAULT_KEY)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((dimension * 0.28f).coerceAtLeast(16.dp))
                    .clip(CircleShape)
                    .background(DfColors.Purple)
                    .border(1.5.dp, DfThemeColors.surface(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    DfIcons.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(DfThemeColors.surface())
                    .padding(1.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(DfThemeColors.success()),
                )
            }
        }
    }
}

@Composable
fun AppPresetAvatar(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    size: AppAvatarSize = AppAvatarSize.Medium,
    selected: Boolean = false,
    contentDescription: String,
) {
    AppAvatar(
        resolved = ResolvedAvatar(
            presetDrawable = drawableRes,
            presetKey = null,
            usePreset = true,
        ),
        modifier = modifier,
        size = size,
        selected = selected,
        contentDescription = contentDescription,
    )
}
