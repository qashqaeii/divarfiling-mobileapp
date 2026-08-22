package ir.divarfiling.mobile.feature.filing.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.filing.DatasetDisplayUtils
import ir.divarfiling.mobile.core.filing.ListingImageUtils
import ir.divarfiling.mobile.core.network.DatasetDto

@Composable
fun FilingDatasetsSection(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            Surface(shape = AppShapes.Chip, color = DfThemeColors.primaryContainer()) {
                Text(
                    text = "$count فایل",
                    modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.primary(),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingDatasetCard(
    dataset: DatasetDto,
    onClick: () -> Unit,
    onExport: () -> Unit = {},
    onDelete: () -> Unit = {},
    showLocation: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val location = listOfNotNull(dataset.district, dataset.city).joinToString("، ")
    val format = dataset.fileFormat?.uppercase() ?: "JSON"
    val cardTitle = datasetDisplayTitle(dataset)
    val coverShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val coverUrls = remember(dataset.id, dataset.thumbnailUrl, dataset.thumbnailUrls) {
        ListingImageUtils.buildGalleryUrls(
            dataset.thumbnailUrl,
            datasetCoverFallbackUrls(dataset),
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.card,
        tonalElevation = 0.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(coverShape),
            ) {
                if (coverUrls.isEmpty()) {
                    FilingDatasetCoverPlaceholder(
                        itemCount = dataset.itemCount,
                        format = format,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    DfListingImage(
                        thumbnailUrl = dataset.thumbnailUrl,
                        images = datasetCoverFallbackUrls(dataset),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = cardTitle,
                        modifier = Modifier.weight(1f),
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconAction(
                            contentDescription = "خروجی",
                            onClick = onExport,
                            iconRes = DfDecorIcons.Download,
                        )
                        IconAction(
                            contentDescription = "مشاهده فایل",
                            onClick = onClick,
                            iconRes = DfDecorIcons.FileText,
                        )
                        Box {
                            IconAction(
                                contentDescription = "بیشتر",
                                onClick = { showMenu = true },
                                icon = DfIcons.MoreVertical,
                            )
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("مشاهده آگهی‌ها") },
                                    onClick = { showMenu = false; onClick() },
                                )
                                DropdownMenuItem(
                                    text = { Text("خروجی Excel / JSON") },
                                    onClick = { showMenu = false; onExport() },
                                )
                                DropdownMenuItem(
                                    text = { Text("حذف فایل", color = DfThemeColors.error()) },
                                    onClick = { showMenu = false; onDelete() },
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FormatBadge(format)
                    CountBadge(dataset.itemCount)
                }

                if (showLocation && location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.MapPin,
                            contentDescription = null,
                            tint = DfThemeColors.primary(),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = location,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                dataset.createdAt?.let { created ->
                    DateUtils.formatJalaliDateTime(created) ?: DateUtils.formatJalaliDate(created)
                }?.let { jalaliDate ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Calendar,
                            contentDescription = null,
                            tint = DfThemeColors.textMuted(),
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = jalaliDate,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilingDatasetCoverPlaceholder(
    itemCount: Int,
    format: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    DfColors.Purple.copy(alpha = 0.18f),
                    DfColors.Blue.copy(alpha = 0.12f),
                    DfThemeColors.surfaceVariant(),
                ),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(AppShapes.GlassSmall)
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            DfDecorImage(
                resId = DfDecorIcons.Folder,
                size = 34.dp,
                contentDescription = null,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "پوشه استخراج",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            Text(
                text = "$itemCount آگهی · $format",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
        }
    }
}

internal fun datasetCoverFallbackUrls(dataset: DatasetDto): List<String> =
    ListingImageUtils.datasetCoverFallbackUrls(dataset.thumbnailUrl, dataset.thumbnailUrls)

internal fun datasetDisplayTitle(dataset: DatasetDto): String =
    DatasetDisplayUtils.displayTitle(dataset)

@Composable
private fun FormatBadge(format: String) {
    val (bg, fg) = when (format.uppercase()) {
        "CSV" -> DfColors.GreenLight to DfColors.OnSuccess
        "XLSX", "XLS" -> DfColors.SuccessContainer to DfColors.OnSuccess
        else -> DfThemeColors.primaryContainer() to DfThemeColors.primary()
    }
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = format,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CountBadge(count: Int) {
    Surface(shape = AppShapes.Chip, color = DfThemeColors.surfaceVariant()) {
        Text(
            text = "$count آگهی",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
        )
    }
}

@Composable
private fun IconAction(
    contentDescription: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        when {
            iconRes != null -> Image(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit,
            )
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
