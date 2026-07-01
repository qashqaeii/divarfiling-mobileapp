package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.places.PlaceOption
import ir.divarfiling.mobile.core.places.PlaceSearchResult

@Composable
fun ExtractLocationCard(
    query: String,
    suggestions: List<PlaceSearchResult>,
    provinceName: String,
    cityName: String,
    districtName: String?,
    provinces: List<String>,
    cities: List<PlaceOption>,
    districts: List<PlaceOption>,
    districtId: String,
    enabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSuggestionSelect: (PlaceSearchResult) -> Unit,
    onProvinceChange: (String) -> Unit,
    onCityChange: (PlaceOption) -> Unit,
    onDistrictChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showManualPicker by remember { mutableStateOf(false) }

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ExtractSectionTitle(title = "انتخاب موقعیت", iconRes = DfDecorIcons.MapPin)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = AppShapes.Field,
                    color = DfColors.SurfaceVariant,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Icon(
                            imageVector = DfIcons.Search,
                            contentDescription = null,
                            tint = DfColors.TextMuted,
                            modifier = Modifier.size(18.dp),
                        )
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            textStyle = AppTypography.bodyDescription.copy(color = DfColors.TextPrimary),
                            singleLine = true,
                            cursorBrush = SolidColor(DfColors.Purple),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "مثلاً سعادت آباد",
                                            style = AppTypography.bodyDescription,
                                            color = DfColors.TextMuted,
                                            maxLines = 1,
                                        )
                                    }
                                    inner()
                                }
                            },
                        )
                        if (query.isNotEmpty() && enabled) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = DfIcons.X,
                                    contentDescription = "پاک کردن",
                                    tint = DfColors.TextMuted,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
                Surface(
                    shape = AppShapes.IconContainer,
                    color = DfColors.PurpleContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DfDecorImage(
                            resId = DfDecorIcons.MapPin,
                            size = 28.dp,
                        )
                    }
                }
            }

            if (query.length >= 2 && suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                    Text(
                        text = "نتایج پیشنهادی",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        fontWeight = FontWeight.Medium,
                    )
                    suggestions.take(4).forEach { item ->
                        Surface(
                            onClick = { if (enabled) onSuggestionSelect(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = DfColors.SurfaceVariant.copy(alpha = 0.65f),
                        ) {
                            Text(
                                text = item.matchedText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    if (provinceName.isNotBlank()) {
                        LocationSelectedRow(
                            label = "استان $provinceName",
                            icon = DfIcons.Building,
                        )
                    }
                    if (cityName.isNotBlank()) {
                        LocationSelectedRow(
                            label = "شهر $cityName",
                            icon = DfIcons.Building,
                        )
                    }
                    districtName?.takeIf { it.isNotBlank() }?.let { district ->
                        LocationSelectedRow(label = "منطقه $district", useMapPin = true)
                    }
                    Text(
                        text = if (showManualPicker) "بستن انتخاب دستی" else "انتخاب دستی استان و شهر",
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = enabled) { showManualPicker = !showManualPicker }
                            .padding(vertical = 4.dp),
                    )
                    if (showManualPicker) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            DfDropdown(
                                label = "استان",
                                value = provinceName,
                                options = provinces,
                                enabled = enabled,
                                onSelect = onProvinceChange,
                            )
                            DfDropdown(
                                label = "شهر",
                                value = cityName,
                                options = cities.map { it.name },
                                enabled = enabled,
                                onSelect = { name ->
                                    cities.firstOrNull { it.name == name }?.let(onCityChange)
                                },
                            )
                            if (districts.isNotEmpty()) {
                                DfDropdown(
                                    label = "منطقه (اختیاری)",
                                    value = districts.firstOrNull { it.id == districtId }?.name ?: "همه مناطق",
                                    options = listOf("همه مناطق") + districts.map { it.name },
                                    enabled = enabled,
                                    onSelect = { name ->
                                        if (name == "همه مناطق") {
                                            onDistrictChange("")
                                        } else {
                                            districts.firstOrNull { it.name == name }?.id?.let(onDistrictChange)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                ExtractMapIllustration(modifier = Modifier.padding(start = AppSpacing.xs))
            }
        }
    }
}

@Composable
private fun LocationSelectedRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    useMapPin: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DfColors.SurfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            when {
                useMapPin -> DfDecorImage(resId = DfDecorIcons.MapPin, size = 18.dp)
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.bodyDescription,
                color = DfColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = DfIcons.CircleCheck,
            contentDescription = null,
            tint = DfColors.Green,
            modifier = Modifier.size(18.dp),
        )
    }
}
