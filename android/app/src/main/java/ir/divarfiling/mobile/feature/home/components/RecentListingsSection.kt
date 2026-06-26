package ir.divarfiling.mobile.feature.home.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.RecentFileItem

@Composable
fun RecentListingsSection(
    files: List<RecentFileItem>,
    isLoading: Boolean,
    onFileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DfSpacing.sm),
    ) {
        DfSectionTitle(
            title = "آخرین فایل‌ها",
            modifier = Modifier.padding(horizontal = DfSpacing.screenHorizontal),
        )

        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = DfSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
            ) {
                items(3) {
                    DfShimmerBox(modifier = Modifier.width(200.dp).height(140.dp))
                }
            }
            return
        }

        if (files.isEmpty()) {
            Text(
                text = "هنوز فایلی استخراج نشده",
                style = MaterialTheme.typography.bodyMedium,
                color = DfColors.TextSecondary,
                modifier = Modifier.padding(horizontal = DfSpacing.screenHorizontal),
            )
            return
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = DfSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
        ) {
            items(files, key = { it.id }) { file ->
                RecentFileCard(file = file, onClick = { onFileClick(file.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentFileCard(
    file: RecentFileItem,
    onClick: () -> Unit,
) {
    val location = listOfNotNull(file.city, file.district).joinToString(" — ")
    val badgeLabel = when {
        file.transactionType?.contains("اجاره", ignoreCase = true) == true -> "اجاره"
        file.transactionType?.contains("رهن", ignoreCase = true) == true -> "رهن"
        else -> "فروش"
    }
    val badgeColor = if (badgeLabel == "اجاره") DfColors.Blue else DfColors.Purple

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(148.dp),
        shape = DfShapes.Card,
        shadowElevation = 4.dp,
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
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                            startY = 60f,
                        ),
                    ),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(DfSpacing.sm),
                shape = DfShapes.Chip,
                color = badgeColor.copy(alpha = 0.9f),
            ) {
                Text(
                    text = badgeLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(DfSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = location.ifBlank { "فایل دیوار" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = "${file.itemCount} آگهی",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
                file.createdAt?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
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
