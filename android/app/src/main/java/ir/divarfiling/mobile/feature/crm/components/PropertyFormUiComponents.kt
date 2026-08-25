package ir.divarfiling.mobile.feature.crm.components

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

const val MAX_PROPERTY_FORM_IMAGES = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyLocationMapPickerSheet(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultLat = 35.6892
    val defaultLng = 51.3890
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DfModalBottomSheet(
        onDismissRequest = onDismiss,
        dismissOnScrimOrSwipe = false,
    ) {
        DfSheetScaffold(
            title = "انتخاب موقعیت روی نقشه",
            subtitle = "نقشه را جابه‌جا کنید تا نشانگر روی محل دقیق قرار بگیرد",
            icon = DfIcons.Map,
            iconContainerColor = DfColors.BlueLight,
            iconTint = DfColors.Blue,
            bodyHeightFraction = 0.78f,
            scrollable = false,
            onClose = onDismiss,
            footer = {
                DfSheetActions(
                    primaryText = "تأیید موقعیت",
                    onPrimary = {
                        val center = mapView?.mapCenter as? GeoPoint
                        if (center != null) {
                            onConfirm(center.latitude, center.longitude)
                        } else {
                            onConfirm(initialLatitude ?: defaultLat, initialLongitude ?: defaultLng)
                        }
                    },
                    onSecondary = onDismiss,
                )
            },
        ) {
            Text(
                text = "نشانگر وسط صفحه = موقعیت انتخاب‌شده",
                style = MaterialTheme.typography.bodySmall,
                color = DfColors.TextMuted,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            isClickable = true
                            isFocusable = true
                            controller.setZoom(16.0)
                            val start = GeoPoint(
                                initialLatitude ?: defaultLat,
                                initialLongitude ?: defaultLng,
                            )
                            controller.setCenter(start)
                            setOnTouchListener { view, event ->
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                                        view.parent?.requestDisallowInterceptTouchEvent(true)
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                                        view.parent?.requestDisallowInterceptTouchEvent(false)
                                }
                                false
                            }
                            mapView = this
                        }
                    },
                    update = { view ->
                        val start = GeoPoint(
                            initialLatitude ?: defaultLat,
                            initialLongitude ?: defaultLng,
                        )
                        view.controller.setCenter(start)
                        view.invalidate()
                        mapView = view
                    },
                )
                Icon(
                    imageVector = DfIcons.MapPin,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PropertyCoordPreviewChip(
                    label = "عرض",
                    value = (mapView?.mapCenter as? GeoPoint)?.latitude?.formatCoord()
                        ?: (initialLatitude ?: defaultLat).formatCoord(),
                    modifier = Modifier.weight(1f),
                )
                PropertyCoordPreviewChip(
                    label = "طول",
                    value = (mapView?.mapCenter as? GeoPoint)?.longitude?.formatCoord()
                        ?: (initialLongitude ?: defaultLng).formatCoord(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    DisposableEffect(Unit) {
        mapView?.onResume()
        onDispose { mapView?.onPause() }
    }
}

@Composable
private fun PropertyCoordPreviewChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Chip,
        color = DfThemeColors.surfaceVariant(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(value, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PropertyAmenitySelectableChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val green = DfColors.Green
    val greenBg = DfColors.GreenLight
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Chip,
        color = if (selected) greenBg else DfThemeColors.surfaceVariant(),
        border = if (selected) BorderStroke(1.5.dp, green) else BorderStroke(1.dp, DfColors.Outline.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) green else DfThemeColors.textSecondary(),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) green else DfThemeColors.textSecondary(),
            )
            if (selected) {
                Icon(
                    imageVector = DfIcons.CircleCheck,
                    contentDescription = null,
                    tint = green,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun PropertyFormImageThumbnailRow(
    imageUrls: List<String>,
    pendingUris: List<String>,
    enabled: Boolean,
    isUploading: Boolean,
    onMoveUp: (index: Int, isPending: Boolean) -> Unit,
    onMoveDown: (index: Int, isPending: Boolean) -> Unit,
    onRemove: (index: Int, isPending: Boolean) -> Unit,
) {
    val total = imageUrls.size + pendingUris.size
    if (total == 0) return

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        imageUrls.forEachIndexed { index, url ->
            PropertyFormImageItem(
                label = if (index == 0) "کاور" else "تصویر ${index + 1}",
                model = url,
                enabled = enabled && !isUploading,
                canMoveUp = index > 0,
                canMoveDown = index < total - 1,
                onMoveUp = { onMoveUp(index, false) },
                onMoveDown = { onMoveDown(index, false) },
                onRemove = { onRemove(index, false) },
            )
        }
        pendingUris.forEachIndexed { index, uri ->
            val globalIndex = imageUrls.size + index
            PropertyFormImageItem(
                label = if (globalIndex == 0) "کاور (در انتظار آپلود)" else "تصویر ${globalIndex + 1} (در انتظار)",
                model = uri,
                enabled = enabled && !isUploading,
                canMoveUp = globalIndex > 0,
                canMoveDown = globalIndex < total - 1,
                onMoveUp = { onMoveUp(index, true) },
                onMoveDown = { onMoveDown(index, true) },
                onRemove = { onRemove(index, true) },
            )
        }
    }
}

@Composable
private fun PropertyFormImageItem(
    label: String,
    model: String,
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DfAsyncImage(
            url = model,
            contentDescription = label,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (model.startsWith("http")) "آپلود شده" else "از گالری",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
        IconButton(onClick = onMoveUp, enabled = enabled && canMoveUp, modifier = Modifier.size(32.dp)) {
            Icon(DfIcons.ChevronUp, contentDescription = "بالا", modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onMoveDown, enabled = enabled && canMoveDown, modifier = Modifier.size(32.dp)) {
            Icon(DfIcons.ChevronDown, contentDescription = "پایین", modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onRemove, enabled = enabled, modifier = Modifier.size(32.dp)) {
            Icon(DfIcons.Trash, contentDescription = "حذف", tint = DfColors.Rose, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun PropertyMapLocationButton(
    latitude: String,
    longitude: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Chip,
        color = DfColors.BlueLight,
        border = BorderStroke(1.dp, DfColors.Blue.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(DfIcons.Map, contentDescription = null, tint = DfColors.Blue, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "انتخاب از روی نقشه",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.Blue,
                )
                val hint = when {
                    latitude.isNotBlank() && longitude.isNotBlank() ->
                        "${latitude.formatCoord()} ، ${longitude.formatCoord()}"
                    else -> "موقعیت دقیق را روی نقشه مشخص کنید"
                }
                Text(hint, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            Icon(DfIcons.ChevronLeft, contentDescription = null, tint = DfColors.Blue, modifier = Modifier.size(16.dp))
        }
    }
}

fun dropdownNumericOptions(max: Int, start: Int = 0): List<String> =
    listOf("—") + (start..max).map { it.toString() }

fun dropdownDisplayValue(value: String): String = value.ifBlank { "—" }

fun parseDropdownSelection(option: String): String = if (option == "—") "" else option

fun isNumericDropdownChoices(choices: List<String>): Boolean =
    choices.isNotEmpty() && choices.all { it.toIntOrNull() != null }

private fun Double.formatCoord(): String = "%.6f".format(this)

private fun String.formatCoord(): String = toDoubleOrNull()?.formatCoord() ?: this
