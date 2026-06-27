package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.RecentFileItem

private val JalaliDatePattern = Regex("""^1[34]\d{2}/\d{2}/\d{2}$""")

private fun formatJalaliDate(createdAt: String?): String? {
    val value = createdAt?.trim().orEmpty()
    if (value.isBlank()) return null
    return if (JalaliDatePattern.matches(value)) value else null
}

@Composable
fun RecentListingsSection(
    files: List<RecentFileItem>,
    isLoading: Boolean,
    onFileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.52f).coerceIn(168.dp, 220.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSectionTitle(
            title = "آخرین فایل‌ها",
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )

        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AppSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                items(3) {
                    DfShimmerBox(modifier = Modifier.width(cardWidth).height(156.dp))
                }
            }
            return
        }

        if (files.isEmpty()) {
            DfEmptyState(
                title = "هنوز فایلی ندارید",
                subtitle = "از تب استخراج یا ویندوز یک dataset بسازید",
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
            return
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = AppSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            items(files, key = { it.id }) { file ->
                RecentFileCard(
                    file = file,
                    cardWidth = cardWidth,
                    onClick = { onFileClick(file.id) },
                )
            }
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
    val location = listOfNotNull(file.city, file.district).joinToString(" — ")
    val badgeLabel = when {
        file.transactionType?.contains("اجاره", ignoreCase = true) == true -> "اجاره"
        file.transactionType?.contains("رهن", ignoreCase = true) == true -> "رهن"
        else -> "فروش"
    }
    val badgeColor = if (badgeLabel == "اجاره") DfColors.Blue else DfColors.Purple
    val displayDate = formatJalaliDate(file.createdAt)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(cardWidth)
            .height(156.dp),
        shape = AppShapes.ListingCard,
        shadowElevation = AppElevations.listingCard,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!file.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = file.thumbnailUrl,
                    contentDescription = null,
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DfColors.ImageOverlayStart,
                                DfColors.ImageScrimLight,
                                DfColors.ImageOverlayEnd,
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY,
                        ),
                    ),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(AppSpacing.sm),
                shape = AppShapes.Chip,
                color = badgeColor.copy(alpha = 0.92f),
            ) {
                Text(
                    text = badgeLabel,
                    modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                    style = AppTypography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(
                    text = location.ifBlank { "فایل دیوار" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${file.itemCount} آگهی",
                    style = AppTypography.bodyDescription,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                displayDate?.let { date ->
                    Text(
                        text = date,
                        style = AppTypography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 390)
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun RecentListingsSectionPreview() {
    DivarFilingTheme {
        RecentListingsSection(
            files = listOf(
                RecentFileItem("1", "تهران", "زعفرانیه", "فروش", 369, "1405/04/01"),
                RecentFileItem("2", "تهران", "ونک", "اجاره", 120, "1405/03/28"),
                RecentFileItem("3", "تهران", "پاسداران", "فروش", 85, "2025-06-20"),
            ),
            isLoading = false,
            onFileClick = {},
        )
    }
}
