package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import ir.divarfiling.mobile.core.design.components.DfBadge
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
    val cardWidth = (screenWidth * 0.72f).coerceIn(240.dp, 300.dp)

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
                    DfShimmerBox(modifier = Modifier.width(cardWidth).height(168.dp))
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentFileCard(
    file: RecentFileItem,
    cardWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val location = listOfNotNull(file.city, file.district).joinToString(" · ")
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
            .height(168.dp),
        shape = AppShapes.ListingCard,
        shadowElevation = AppElevations.card,
        color = DfColors.Surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
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
                    DfBadge(
                        text = badgeLabel,
                        color = badgeColor.copy(alpha = 0.9f),
                        textColor = Color.White,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = location.ifBlank { "فایل دیوار" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(DfIcons.Folder, null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
                        Text("${file.itemCount} آگهی", style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                    }
                    displayDate?.let {
                        Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    }
                }
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(AppShapes.Chip),
                    color = badgeColor,
                    trackColor = DfColors.SurfaceVariant,
                )
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
                RecentFileItem("1", "تهران", "زعفرانیه", "فروش", 369, "1405/04/01"),
                RecentFileItem("2", "تهران", "ونک", "اجاره", 120, "1405/03/28"),
            ),
            isLoading = false,
            onFileClick = {},
        )
    }
}
