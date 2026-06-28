package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@Composable
fun FilingDatasetsSection(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ExtractSectionCard(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer) {
                    Text(
                        text = "$count فایل",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingDatasetCard(
    dataset: DatasetDto,
    onClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val location = listOfNotNull(dataset.city, dataset.district).joinToString("، ")
    val format = dataset.fileFormat?.uppercase() ?: "JSON"
    val filename = dataset.originalFilename ?: "${dataset.id}.${format.lowercase()}"
    val thumb = ImageUrlFormatter.normalize(dataset.thumbnailUrl)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(DfColors.SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (thumb != null) {
                    AsyncImage(
                        model = thumb,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = DfIcons.Building,
                        contentDescription = null,
                        tint = DfColors.TextMuted,
                        modifier = Modifier.size(32.dp),
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
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = dataset.name,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = filename,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconAction(DfIcons.Download, "باز کردن", onClick)
                        IconAction(DfIcons.RotateCcw, "بروزرسانی", onRefreshClick)
                        Box {
                            IconAction(DfIcons.MoreVertical, "بیشتر") { showMenu = true }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("مشاهده آگهی‌ها") },
                                    onClick = { showMenu = false; onClick() },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی")
                                    },
                                    onClick = { showMenu = false; onToggleFavorite() },
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FormatBadge(format)
                    dataset.transactionType?.let {
                        Surface(shape = AppShapes.Chip, color = DfColors.BlueLight) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = AppTypography.labelSmall,
                                color = DfColors.Blue,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (location.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = DfIcons.MapPin,
                                contentDescription = null,
                                tint = DfColors.Purple,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = location,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        text = "${dataset.itemCount} آگهی",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                }
                dataset.createdAt?.let { created ->
                    DateUtils.formatJalaliDateTime(created) ?: DateUtils.formatJalaliDate(created)
                }?.let { jalaliDate ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Calendar,
                            contentDescription = null,
                            tint = DfColors.TextMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = jalaliDate,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatBadge(format: String) {
    val (bg, fg) = when (format.uppercase()) {
        "CSV" -> DfColors.GreenLight to DfColors.Green
        "XLSX", "XLS" -> Color(0xFFECFDF5) to Color(0xFF0F766E)
        else -> DfColors.PurpleContainer to DfColors.Purple
    }
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = format,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = DfColors.TextMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}
