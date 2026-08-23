package ir.divarfiling.mobile.core.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
    titleIconBackground: Color? = null,
    titleColor: Color = DfThemeColors.textPrimary(),
    sectionAccent: Color = DfThemeColors.primary(),
    userName: String? = null,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    menuIcon: ImageVector = DfIcons.Menu,
    showBrandLogo: Boolean = false,
    variant: DfHeaderVariant = DfHeaderVariant.Hub,
    showSectionLabel: Boolean? = null,
    showTitleIcon: Boolean = true,
    embeddedInFrame: Boolean = false,
    toolbarContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val horizontalPadding = if (embeddedInFrame) 0.dp else DfHeaderMetrics.horizontalPadding
    val verticalPadding = if (embeddedInFrame) 0.dp else DfHeaderMetrics.verticalPadding
    val sectionLabelMode = resolveSectionLabelMode(variant, sectionLabel, title, showSectionLabel)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (variant == DfHeaderVariant.Detail) {
            onBack?.let { DfHubBackButton(onClick = it, compact = true) }
        }

        DfHeaderTitleBlock(
            title = title,
            subtitle = subtitle,
            sectionLabel = sectionLabel?.takeIf { sectionLabelMode == DfSectionLabelMode.Eyebrow },
            titleIcon = titleIcon,
            titleIconRes = titleIconRes,
            titleIconBackground = titleIconBackground,
            iconTint = sectionAccent,
            sectionAccent = sectionAccent,
            titleColor = titleColor,
            variant = variant,
            showTitleIcon = showTitleIcon,
            modifier = Modifier
                .weight(1f)
                .padding(end = AppSpacing.xxs),
        )

        DfHeaderToolbar(
            userName = userName,
            notificationCount = notificationCount,
            onNotificationsClick = onNotificationsClick,
            onMenuClick = onMenuClick,
            onBack = if (variant != DfHeaderVariant.Detail) onBack else null,
            menuIcon = menuIcon,
            showBrandLogo = showBrandLogo,
            compact = variant == DfHeaderVariant.Detail,
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
    titleIconBackground: Color? = null,
    userName: String? = null,
    notificationCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    titleColor: Color = DfThemeColors.textPrimary(),
    showBrandLogo: Boolean = false,
    showBottomDivider: Boolean = false,
    showSectionLabel: Boolean? = null,
    showTitleIcon: Boolean = true,
    menuIcon: ImageVector = DfIcons.Menu,
    framed: Boolean = true,
    toolbarContent: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    val theme = headerThemeForSection(sectionLabel)
    val resolvedIconBackground = titleIconBackground ?: theme.iconBackground

    val headerContent: @Composable () -> Unit = {
        DfStandardPageHeader(
            title = title,
            subtitle = subtitle,
            sectionLabel = sectionLabel,
            titleIcon = titleIcon,
            titleIconRes = titleIconRes,
            titleIconBackground = resolvedIconBackground,
            titleColor = titleColor,
            sectionAccent = theme.accent,
            userName = userName,
            notificationCount = notificationCount,
            onNotificationsClick = onNotificationsClick,
            onMenuClick = onMenuClick,
            onBack = onBack,
            menuIcon = menuIcon,
            showBrandLogo = showBrandLogo,
            variant = DfHeaderVariant.Hub,
            showSectionLabel = showSectionLabel,
            showTitleIcon = showTitleIcon,
            embeddedInFrame = framed,
            toolbarContent = toolbarContent,
        )
        bottomContent?.invoke()
    }

    if (framed) {
        DfHeaderFrame(
            theme = theme,
            modifier = modifier,
            showBottomHairline = showBottomDivider,
            content = { headerContent() },
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            content = { headerContent() },
        )
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
    val theme = headerThemeForSection(sectionLabel)

    DfHeaderFrame(
        theme = theme,
        modifier = modifier,
        showBottomHairline = false,
    ) {
        DfStandardPageHeader(
            title = greetingTitleFor(userName),
            subtitle = subtitle,
            titleIconRes = titleIconRes,
            titleIconBackground = theme.iconBackground,
            sectionAccent = theme.accent,
            userName = userName,
            notificationCount = notificationCount,
            onNotificationsClick = onNotificationsClick,
            onMenuClick = onMenuClick,
            onBack = onBack,
            menuIcon = menuIcon,
            showBrandLogo = showBrandLogo,
            variant = DfHeaderVariant.Greeting,
            showSectionLabel = false,
            embeddedInFrame = true,
        )
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
    DfStandardPageHeader(
        title = title,
        subtitle = subtitle,
        titleIcon = titleIcon,
        onBack = onTrailingClick,
        variant = DfHeaderVariant.Detail,
        modifier = modifier,
        toolbarContent = {
            DfHubHeaderIconButton(
                icon = leadingIcon,
                contentDescription = leadingContentDescription,
                onClick = onLeadingClick,
                compact = true,
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
    titleIconBackground: Color? = null,
    iconTint: Color = DfThemeColors.primary(),
    sectionAccent: Color = DfThemeColors.primary(),
    titleColor: Color = DfThemeColors.textPrimary(),
    variant: DfHeaderVariant = DfHeaderVariant.Hub,
    showTitleIcon: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val compact = variant == DfHeaderVariant.Detail
    val iconContainerSize = if (compact) 32.dp else DfHeaderMetrics.iconContainerSize
    val iconSize = if (compact) 16.dp else DfHeaderMetrics.iconSize
    val titleStyle = when (variant) {
        DfHeaderVariant.Greeting -> AppTypography.cardTitle.copy(fontWeight = FontWeight.Bold)
        DfHeaderVariant.Detail -> AppTypography.sectionTitle.copy(fontWeight = FontWeight.Bold)
        DfHeaderVariant.Hub -> AppTypography.sectionTitle.copy(fontWeight = FontWeight.Bold)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showTitleIcon) {
            when {
                titleIconRes != null -> DfDecorIconBox(
                    resId = titleIconRes,
                    containerSize = iconContainerSize,
                    imageSize = iconSize,
                    background = titleIconBackground ?: DfThemeColors.primaryContainer(),
                )
                titleIcon != null -> Box(
                    modifier = Modifier
                        .size(iconContainerSize)
                        .clip(AppShapes.IconContainer)
                        .background(titleIconBackground ?: DfThemeColors.primaryContainer()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(DfHeaderMetrics.titleSubtitleGap),
        ) {
            sectionLabel?.takeIf { it.isNotBlank() }?.let { label ->
                Text(
                    text = label,
                    style = AppTypography.meta,
                    fontWeight = FontWeight.Medium,
                    color = sectionAccent.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = title,
                style = titleStyle,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = if (compact) 1 else 2,
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
    compact: Boolean,
    toolbarContent: @Composable (RowScope.() -> Unit)?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        userName?.let { DfHubUserAvatar(it, compact = compact) }
        onNotificationsClick?.let {
            DfHubHeaderDecorIconButton(
                resId = DfDecorIcons.Bell,
                contentDescription = "اعلان‌ها",
                onClick = it,
                badgeCount = notificationCount,
                compact = compact,
            )
        }
        toolbarContent?.invoke(this)
        onMenuClick?.let {
            DfHubHeaderIconButton(
                icon = menuIcon,
                contentDescription = "منو",
                onClick = it,
                compact = compact,
            )
        }
        onBack?.let { DfHubBackButton(onClick = it, compact = compact) }
        if (showBrandLogo) {
            DfHubBrandLogo(
                modifier = Modifier.size(if (compact) 32.dp else DfHeaderMetrics.avatarSize),
                logoSize = if (compact) 20.dp else 24.dp,
            )
        }
    }
}

@Composable
private fun DfHubUserAvatar(userName: String, compact: Boolean = false) {
    val size = if (compact) 32.dp else DfHeaderMetrics.avatarSize
    Box {
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = DfThemeColors.primaryContainer(),
            border = BorderStroke(0.5.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.none,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = userName.firstOrNull()?.toString() ?: "؟",
                    style = if (compact) AppTypography.labelSmall else AppTypography.meta,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.onPrimaryContainer(),
                    maxLines = 1,
                )
            }
        }
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

@Composable
private fun DfHubBrandLogo(
    modifier: Modifier = Modifier.size(DfHeaderMetrics.avatarSize),
    logoSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = DfThemeColors.primaryContainer(),
        border = BorderStroke(0.5.dp, DfThemeColors.outlineSubtle()),
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
private fun DfHubBackButton(onClick: () -> Unit, compact: Boolean = false) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(if (compact) 36.dp else DfHeaderMetrics.actionTouchSize),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "بازگشت",
            tint = DfThemeColors.textPrimary(),
            modifier = Modifier.size(if (compact) 20.dp else DfHeaderMetrics.actionIconSize),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DfHubHeaderDecorIconButton(
    @DrawableRes resId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    compact: Boolean = false,
) {
    val touchSize = if (compact) 32.dp else DfHeaderMetrics.actionTouchSize
    val iconSize = if (compact) 18.dp else DfHeaderMetrics.actionIconSize
    Box {
        IconButton(onClick = onClick, modifier = Modifier.size(touchSize)) {
            DfDecorImage(
                resId = resId,
                size = iconSize,
                contentDescription = contentDescription,
            )
        }
        if (badgeCount > 0) {
            DfHeaderNotificationBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = 2.dp),
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
    compact: Boolean = false,
) {
    val touchSize = if (compact) 32.dp else DfHeaderMetrics.actionTouchSize
    val iconSize = if (compact) 20.dp else DfHeaderMetrics.actionIconSize
    Box {
        IconButton(onClick = onClick, modifier = Modifier.size(touchSize)) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = DfThemeColors.textSecondary(),
                modifier = Modifier.size(iconSize),
            )
        }
        if (badgeCount > 0) {
            DfHeaderNotificationBadge(
                count = badgeCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = 2.dp),
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
    val shape = if (isWide) RoundedCornerShape(8.dp) else CircleShape

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = if (isWide) 16.dp else 14.dp, minHeight = 14.dp)
            .clip(shape)
            .background(DfColors.Purple)
            .padding(horizontal = if (isWide) 4.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = DateUtils.toPersianDigits(label),
            style = AppTypography.meta,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DfHeaderCompactPreview360() {
    DivarFilingTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            DfGreetingHeader(
                title = "ignored",
                subtitle = "میزکار فایلینگ دیوار",
                userName = "hossein",
                notificationCount = 15,
                onNotificationsClick = {},
                onMenuClick = {},
            )
            DfHubPageHeader(
                title = "فایلینگ دیوار",
                subtitle = "پوشه‌های استخراج‌شده — جستجو، تحلیل و نقشه",
                sectionLabel = DfHeaderSections.FILING,
                titleIconRes = DfDecorIcons.Folder,
                userName = "hossein",
                notificationCount = 15,
                onNotificationsClick = {},
                onMenuClick = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun DfHeaderCompactPreview320() {
    DivarFilingTheme {
        DfGreetingHeader(
            title = "ignored",
            subtitle = "میزکار فایلینگ دیوار",
            userName = "hossein",
            notificationCount = 15,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
