package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun DfHubPageHeader(
    title: String,
    subtitle: String,
    titleIcon: ImageVector,
    modifier: Modifier = Modifier,
    userName: String? = null,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    titleColor: Color = DfColors.TextPrimary,
    showBrandLogo: Boolean = false,
    menuIcon: ImageVector = DfIcons.SlidersHorizontal,
    toolbarActionsEnd: Boolean = false,
    hideTitleSection: Boolean = false,
    titleAlignment: Alignment.Horizontal = Alignment.End,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        if (toolbarActionsEnd) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        DfHubBackButton(onClick = onBack)
                    }
                    userName?.let { DfHubUserAvatar(it) }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onNotificationsClick != null) {
                        DfHubHeaderIconButton(
                            icon = DfIcons.Bell,
                            contentDescription = "اعلان‌ها",
                            onClick = onNotificationsClick,
                            badgeCount = notificationCount,
                        )
                    }
                    if (onMenuClick != null) {
                        DfHubHeaderIconButton(
                            icon = menuIcon,
                            contentDescription = "منو",
                            onClick = onMenuClick,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        DfHubBackButton(onClick = onBack)
                    }
                    userName?.let { DfHubUserAvatar(it) }
                    if (onNotificationsClick != null) {
                        DfHubHeaderIconButton(
                            icon = DfIcons.Bell,
                            contentDescription = "اعلان‌ها",
                            onClick = onNotificationsClick,
                            badgeCount = notificationCount,
                        )
                    }
                    if (onMenuClick != null) {
                        DfHubHeaderIconButton(
                            icon = menuIcon,
                            contentDescription = "منو",
                            onClick = onMenuClick,
                        )
                    }
                }

                if (!hideTitleSection) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DfHubTitleBlock(
                            title = title,
                            subtitle = subtitle,
                            titleIcon = titleIcon,
                            titleColor = titleColor,
                            horizontalAlignment = titleAlignment,
                        )
                        if (showBrandLogo) {
                            DfHubBrandLogo()
                        }
                    }
                }
            }
        }

        bottomContent?.invoke()
    }
}

@Composable
fun DfGreetingHeader(
    title: String,
    subtitle: String,
    userName: String,
    modifier: Modifier = Modifier,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    showBrandLogo: Boolean = false,
    menuIcon: ImageVector = DfIcons.Menu,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DfHubUserAvatar(userName)
            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
            ) {
                Text(
                    text = title,
                    style = AppTypography.pageTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (showBrandLogo) {
            DfHubBrandLogo(modifier = Modifier.size(44.dp), logoSize = 28.dp)
        } else if (onNotificationsClick != null || onMenuClick != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                onNotificationsClick?.let {
                    DfHubHeaderIconButton(
                        icon = DfIcons.Bell,
                        contentDescription = "اعلان‌ها",
                        onClick = it,
                        badgeCount = notificationCount,
                    )
                }
                onMenuClick?.let {
                    DfHubHeaderIconButton(
                        icon = menuIcon,
                        contentDescription = "منو",
                        onClick = it,
                    )
                }
            }
        }
    }
}

@Composable
fun DfActionPageHeader(
    title: String,
    subtitle: String,
    titleIcon: ImageVector,
    onLeadingClick: () -> Unit,
    leadingIcon: ImageVector,
    leadingContentDescription: String,
    onTrailingClick: () -> Unit,
    trailingIcon: ImageVector,
    trailingContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DfHubHeaderIconButton(
            icon = leadingIcon,
            contentDescription = leadingContentDescription,
            onClick = onLeadingClick,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = AppSpacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = AppTypography.pageTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Icon(
                    imageVector = titleIcon,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = subtitle,
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        DfHubHeaderIconButton(
            icon = trailingIcon,
            contentDescription = trailingContentDescription,
            onClick = onTrailingClick,
        )
    }
}

@Composable
private fun DfHubTitleBlock(
    title: String,
    subtitle: String,
    titleIcon: ImageVector,
    titleColor: Color,
    horizontalAlignment: Alignment.Horizontal,
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AppTypography.pageTitle,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = titleIcon,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = subtitle,
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DfHubUserAvatar(userName: String) {
    Box {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = DfColors.PurpleContainer,
            shadowElevation = AppElevations.subtle,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = userName.firstOrNull()?.toString() ?: "؟",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.PurpleDark,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DfColors.Green),
            )
        }
    }
}

@Composable
private fun DfHubBrandLogo(
    modifier: Modifier = Modifier.size(52.dp),
    logoSize: androidx.compose.ui.unit.Dp = 34.dp,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = DfColors.PurpleContainer,
        shadowElevation = AppElevations.subtle,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.logo_divarfiling),
                contentDescription = null,
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun DfHubBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        Icon(
            imageVector = DfIcons.ChevronLeft,
            contentDescription = "بازگشت",
            tint = DfColors.TextSecondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DfHubHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    Box {
        Surface(
            onClick = onClick,
            shape = AppShapes.IconContainer,
            color = DfColors.Surface.copy(alpha = 0.85f),
            shadowElevation = AppElevations.subtle,
            border = BorderStroke(1.dp, DfColors.GlassBorder),
            modifier = Modifier.size(42.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = DfColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (badgeCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp),
                shape = CircleShape,
                color = DfColors.Purple,
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    style = AppTypography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DfHubPageHeaderPreview() {
    DivarFilingTheme {
        DfHubPageHeader(
            title = "تنظیمات",
            subtitle = "پروفایل، اعلان‌ها و امنیت",
            titleIcon = DfIcons.Settings,
            userName = "حسین",
            notificationCount = 3,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
