package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import androidx.compose.material3.HorizontalDivider
import ir.divarfiling.mobile.core.design.components.DfDecorIconBox
import ir.divarfiling.mobile.core.design.components.DfGlassIconButton
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

/** برچسب‌های یکسان بخش در هدر صفحات */
object DfHeaderSections {
    const val HOME = "میزکار"
    const val CRM = "مدیریت مشتری"
    const val FILING = "فایلینگ"
    const val EXTRACT = "استخراج"
    const val TOOLS = "ابزارها"
    const val SETTINGS = "تنظیمات"
    const val NOTIFICATIONS = "اعلان‌ها"
    const val TEAM = "تیم"
    const val AI = "دستیار هوشمند"
    const val MORE = "بیشتر"
    const val SUPPORT = "پشتیبانی"
    const val LICENSE = "اشتراک"
}
@Composable
fun DfStandardPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    sectionLabel: String? = null,
    titleIcon: ImageVector? = null,
    @DrawableRes titleIconRes: Int? = null,
    titleIconBackground: Color = DfThemeColors.primaryContainer(),
    titleColor: Color = DfThemeColors.textPrimary(),
    userName: String? = null,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    menuIcon: ImageVector = DfIcons.Menu,
    showBrandLogo: Boolean = false,
    toolbarContent: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DfHeaderTitleBlock(
            title = title,
            subtitle = subtitle,
            sectionLabel = sectionLabel,
            titleIcon = titleIcon,
            titleIconRes = titleIconRes,
            titleIconBackground = titleIconBackground,
            iconTint = DfThemeColors.primary(),
            titleColor = titleColor,
            modifier = Modifier
                .weight(1f)
                .padding(end = AppSpacing.sm),
        )

        DfHeaderToolbar(
            userName = userName,
            notificationCount = notificationCount,
            onNotificationsClick = onNotificationsClick,
            onMenuClick = onMenuClick,
            onBack = onBack,
            menuIcon = menuIcon,
            showBrandLogo = showBrandLogo,
            toolbarContent = toolbarContent,
        )
    }
}

@Composable
fun DfHubPageHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    sectionLabel: String? = null,
    titleIcon: ImageVector? = null,
    @DrawableRes titleIconRes: Int? = null,
    titleIconBackground: Color = DfThemeColors.primaryContainer(),
    userName: String? = null,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    titleColor: Color = DfThemeColors.textPrimary(),
    showBrandLogo: Boolean = false,
    showBottomDivider: Boolean = true,
    menuIcon: ImageVector = DfIcons.Menu,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfStandardPageHeader(
            title = title,
            subtitle = subtitle,
            sectionLabel = sectionLabel,
            titleIcon = titleIcon,
            titleIconRes = titleIconRes,
            titleIconBackground = titleIconBackground,
            titleColor = titleColor,
            userName = userName,
            notificationCount = notificationCount,
            onNotificationsClick = onNotificationsClick,
            onMenuClick = onMenuClick,
            onBack = onBack,
            menuIcon = menuIcon,
            showBrandLogo = showBrandLogo,
        )
        bottomContent?.invoke()
        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                color = DfThemeColors.outlineSubtle().copy(alpha = 0.65f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
fun DfGreetingHeader(
    title: String,
    subtitle: String,
    userName: String,
    modifier: Modifier = Modifier,
    sectionLabel: String = DfHeaderSections.HOME,
    @DrawableRes titleIconRes: Int? = DfDecorIcons.House,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    showBrandLogo: Boolean = false,
    menuIcon: ImageVector = DfIcons.Menu,
) {
    DfHubPageHeader(
        title = title,
        subtitle = subtitle,
        sectionLabel = sectionLabel,
        titleIconRes = titleIconRes,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        onBack = onBack,
        menuIcon = menuIcon,
        showBrandLogo = showBrandLogo,
        showBottomDivider = true,
        modifier = modifier,
    )
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
    DfStandardPageHeader(
        title = title,
        subtitle = subtitle,
        titleIcon = titleIcon,
        onBack = onTrailingClick,
        modifier = modifier,
        toolbarContent = {
            DfHubHeaderIconButton(
                icon = leadingIcon,
                contentDescription = leadingContentDescription,
                onClick = onLeadingClick,
            )
        },
    )
}

@Composable
private fun DfHeaderTitleBlock(
    title: String,
    subtitle: String,
    sectionLabel: String?,
    titleIcon: ImageVector?,
    @DrawableRes titleIconRes: Int? = null,
    titleIconBackground: Color = DfThemeColors.primaryContainer(),
    iconTint: Color = DfThemeColors.primary(),
    titleColor: Color = DfThemeColors.textPrimary(),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            titleIconRes != null -> DfDecorIconBox(
                resId = titleIconRes,
                containerSize = 48.dp,
                imageSize = 24.dp,
                background = titleIconBackground,
            )
            titleIcon != null -> Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppShapes.IconContainer)
                    .background(titleIconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = titleIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            sectionLabel?.takeIf { it.isNotBlank() }?.let { label ->
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.primary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = title,
                style = AppTypography.pageTitle,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DfHeaderToolbar(
    userName: String?,
    notificationCount: Int,
    onNotificationsClick: (() -> Unit)?,
    onMenuClick: (() -> Unit)?,
    onBack: (() -> Unit)?,
    menuIcon: ImageVector,
    showBrandLogo: Boolean,
    toolbarContent: @Composable (RowScope.() -> Unit)?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onBack?.let { DfHubBackButton(onClick = it) }
        userName?.let { DfHubUserAvatar(it) }
        onNotificationsClick?.let {
            DfHubHeaderDecorIconButton(
                resId = DfDecorIcons.Bell,
                contentDescription = "اعلان‌ها",
                onClick = it,
                badgeCount = notificationCount,
            )
        }
        toolbarContent?.invoke(this)
        onMenuClick?.let {
            DfHubHeaderIconButton(
                icon = menuIcon,
                contentDescription = "منو",
                onClick = it,
            )
        }
        if (showBrandLogo) {
            DfHubBrandLogo(modifier = Modifier.size(44.dp), logoSize = 28.dp)
        }
    }
}

@Composable
private fun DfHubUserAvatar(userName: String) {
    Box {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = DfThemeColors.primaryContainer(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.none,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = userName.firstOrNull()?.toString() ?: "؟",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.onPrimaryContainer(),
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(DfThemeColors.surface())
                .padding(2.dp),
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

@Composable
private fun DfHubBrandLogo(
    modifier: Modifier = Modifier.size(44.dp),
    logoSize: androidx.compose.ui.unit.Dp = 28.dp,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = DfThemeColors.primaryContainer(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.none,
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
    DfGlassIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "بازگشت",
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DfHubHeaderDecorIconButton(
    @DrawableRes resId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    Box(modifier = Modifier.padding(top = 4.dp, end = 4.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .liquidGlassSurface(shape = AppShapes.IconContainer, variant = DfGlassButtonVariant.Secondary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            DfDecorImage(
                resId = resId,
                size = 22.dp,
                contentDescription = contentDescription,
            )
        }
        if (badgeCount > 0) {
            DfHeaderNotificationBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp),
            )
        }
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
    Box(modifier = Modifier.padding(top = 4.dp, end = 4.dp)) {
        DfGlassIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            variant = DfGlassButtonVariant.Secondary,
        )
        if (badgeCount > 0) {
            DfHeaderNotificationBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp),
            )
        }
    }
}

@Composable
private fun DfHeaderNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val label = when {
        count > 99 -> "99+"
        else -> count.toString()
    }
    val isWide = label.length > 1
    val shape = if (isWide) RoundedCornerShape(10.dp) else CircleShape

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = if (isWide) 22.dp else 18.dp,
                minHeight = 18.dp,
            )
            .clip(shape)
            .background(DfColors.Purple)
            .padding(horizontal = if (isWide) 5.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = DateUtils.toPersianDigits(label),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DfStandardPageHeaderPreview() {
    DivarFilingTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            DfGreetingHeader(
                title = "سلام حسین",
                subtitle = "میزکار فایلینگ دیوار",
                userName = "حسین",
                notificationCount = 3,
                onNotificationsClick = {},
                onMenuClick = {},
            )
            DfHubPageHeader(
                title = "فایلینگ دیوار",
                subtitle = "مدیریت فایل‌های استخراج‌شده",
                titleIcon = DfIcons.Folder,
                userName = "حسین",
                notificationCount = 20,
                onNotificationsClick = {},
                onMenuClick = {},
                menuIcon = DfIcons.Menu,
            )
        }
    }
}
