package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.RecentFileItem

import ir.divarfiling.mobile.core.design.DateUtils

private fun fileTitle(file: RecentFileItem): String {
    val district = file.district?.trim().orEmpty()
    val transaction = when {
        file.transactionType?.contains("اجاره", ignoreCase = true) == true -> "اجاره"
        file.transactionType?.contains("رهن", ignoreCase = true) == true -> "رهن"
        else -> "فروش"
    }
    return when {
        district.isNotBlank() -> "$district - $transaction"
        file.city?.isNotBlank() == true -> "${file.city} - $transaction"
        else -> "فایل دیوار"
    }
}

@Composable
fun RecentListingsSection(
    files: List<RecentFileItem>,
    isLoading: Boolean,
    onFileClick: (String) -> Unit,
    onViewAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.58f).coerceIn(168.dp, 220.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        HomeSectionTitle(
            title = "آخرین فایل‌ها",
            icon = DfIcons.Folder,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )

        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(3) {
                    DfShimmerBox(modifier = Modifier.width(cardWidth).height(196.dp))
                }
            }
            return
        }

        if (files.isEmpty()) {
            DfEmptyState(
                title = "هنوز فایلی ندارید",
                subtitle = "از استخراج فایل یا ویندوز یک dataset بسازید",
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
            return
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = AppSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(files.take(6), key = { it.id }) { file ->
                RecentFileCard(
                    file = file,
                    cardWidth = cardWidth,
                    onClick = { onFileClick(file.id) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAll)
                .padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "مشاهده همه فایل‌ها",
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Medium,
                color = DfColors.Purple,
            )
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentFileCard(
    file: RecentFileItem,
    cardWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val badgeLabel = when {
        file.transactionType?.contains("اجاره", ignoreCase = true) == true -> "اجاره"
        file.transactionType?.contains("رهن", ignoreCase = true) == true -> "رهن"
        else -> "فروش"
    }
    val badgeColor = if (badgeLabel == "اجاره") DfColors.Blue else DfColors.Pink
    val badgeIcon = if (badgeLabel == "اجاره") DfIcons.Building else DfIcons.Home
    val displayDate = DateUtils.formatJalaliDate(file.createdAt)
    val estimatedSizeMb = ((file.itemCount * 0.48f).coerceAtLeast(8f)).toInt()

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(cardWidth)
            .height(196.dp),
        shape = AppShapes.ListingCard,
        shadowElevation = AppElevations.card,
        color = DfColors.Surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
            ) {
                val thumb = ImageUrlFormatter.normalize(file.thumbnailUrl)
                if (thumb != null) {
                    DfAsyncImage(
                        url = thumb,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(DfColors.PurpleLight, DfColors.BlueLight),
                                ),
                            ),
                    )
                }
                Box(modifier = Modifier.padding(8.dp)) {
                    Surface(
                        shape = AppShapes.IconContainer,
                        color = badgeColor.copy(alpha = 0.92f),
                        modifier = Modifier.size(28.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                badgeIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = fileTitle(file),
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                displayDate?.let {
                    Text(
                        it,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${file.itemCount} آگهی",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                    )
                    Text(
                        "${estimatedSizeMb}MB",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    Icon(
                        DfIcons.MoreVertical,
                        contentDescription = null,
                        tint = DfColors.TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RecentListingsSectionPreview() {
    DivarFilingTheme {
        RecentListingsSection(
            files = listOf(
                RecentFileItem("1", "تهران", "جردن", "فروش", 528, "1404/03/20"),
                RecentFileItem("2", "تهران", "ونک", "اجاره", 120, "1405/03/28"),
            ),
            isLoading = false,
            onFileClick = {},
        )
    }
}
