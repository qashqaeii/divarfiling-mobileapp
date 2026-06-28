package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.places.PlaceSearchResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceSearchField(
    query: String,
    suggestions: List<PlaceSearchResult>,
    selectedSummary: String?,
    enabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSuggestionSelect: (PlaceSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            label = { Text("جستجوی مکان") },
            placeholder = { Text("مثلاً سعادت آباد") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = DfColors.Purple) },
        )

        selectedSummary?.takeIf { it.isNotBlank() }?.let { summary ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DfColors.PurpleContainer,
            ) {
                Text(
                    summary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.PurpleDark,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (query.length >= 2 && suggestions.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DfColors.Surface,
                shadowElevation = 4.dp,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                ) {
                    items(suggestions, key = { "${it.provinceId}-${it.cityId}-${it.districtId}-${it.matchedText}" }) { item ->
                        PlaceSuggestionRow(
                            result = item,
                            query = query,
                            onClick = { onSuggestionSelect(item) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceSuggestionRow(
    result: PlaceSearchResult,
    query: String,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = DfColors.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                highlightText(result.matchedText, query),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOfNotNull(
                    "استان ${result.provinceName}",
                    result.cityName?.let { "شهر $it" },
                    result.districtName?.let { "منطقه $it" },
                ).forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DfColors.SurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(chip, style = MaterialTheme.typography.labelSmall, color = DfColors.TextSecondary)
                    }
                }
            }
        }
    }
}

private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val lowerText = text.lowercase()
    val lowerQuery = query.trim().lowercase()
    val index = lowerText.indexOf(lowerQuery)
    if (index < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(SpanStyle(color = DfColors.Purple, fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + lowerQuery.length))
        }
        append(text.substring(index + lowerQuery.length))
    }
}

@Composable
fun rememberDebouncedQuery(
    query: String,
    delayMs: Long = 300L,
    onDebounced: (String) -> Unit,
) {
    LaunchedEffect(query) {
        kotlinx.coroutines.delay(delayMs)
        onDebounced(query)
    }
}

fun placeSelectionSummary(
    provinceName: String,
    cityName: String,
    districtName: String?,
): String {
    val parts = buildList {
        if (provinceName.isNotBlank()) add("استان $provinceName")
        if (cityName.isNotBlank()) add("شهر $cityName")
        districtName?.takeIf { it.isNotBlank() }?.let { add("منطقه $it") }
    }
    return parts.joinToString(" · ")
}
