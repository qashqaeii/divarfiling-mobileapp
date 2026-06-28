package ir.divarfiling.mobile.feature.filing.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun FilingHubHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                FilingHeaderIconButton(
                    icon = DfIcons.Bell,
                    contentDescription = "اعلان‌ها",
                    onClick = onNotificationsClick,
                    badgeCount = notificationCount,
                )
                FilingHeaderIconButton(
                    icon = DfIcons.Menu,
                    contentDescription = "منو",
                    onClick = onMenuClick,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
                ) {
                    Text(
                        text = "فایلینگ دیوار",
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "مدیریت، فیلتر و تحلیل فایل‌های استخراج‌شده از دیوار",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = DfColors.PurpleContainer,
                    shadowElevation = AppElevations.subtle,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.logo_divarfiling),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilingHeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    Box {
        Surface(
            onClick = onClick,
            shape = AppShapes.IconContainer,
            color = DfColors.SurfaceVariant,
            shadowElevation = 0.dp,
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
private fun FilingHubHeaderPreview() {
    DivarFilingTheme {
        FilingHubHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
