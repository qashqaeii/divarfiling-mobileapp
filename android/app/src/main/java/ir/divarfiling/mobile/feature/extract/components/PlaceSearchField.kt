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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            label = { Text("جستجوی مکان") },
            placeholder = { Text("مثلاً سعادت آباد") },
            shape = AppShapes.Field,
            leadingIcon = {
                Icon(
                    DfIcons.MapPin,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                )
            },
        )

        selectedSummary?.takeIf { it.isNotBlank() }?.let { summary ->
            Surface(
                shape = AppShapes.CardSmall,
                color = DfThemeColors.primaryContainer(),
            ) {
                Text(
                    summary,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.onPrimaryContainer(),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        if (query.length >= 2 && suggestions.isNotEmpty()) {
            Surface(
                shape = AppShapes.Card,
                color = DfThemeColors.surface(),
                shadowElevation = AppElevations.raised,
                tonalElevation = 0.dp,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PlaceSuggestionRow(
    result: PlaceSearchResult,
    query: String,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = DfThemeColors.surface()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Text(
                highlightText(result.matchedText, query),
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                listOfNotNull(
                    "استان ${result.provinceName}",
                    result.cityName?.let { "شهر $it" },
                    result.districtName?.let { "منطقه $it" },
                ).forEach { chip ->
                    Box(
                        modifier = Modifier
                            .background(DfThemeColors.surfaceVariant(), AppShapes.Chip)
                            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                    ) {
                        Text(
                            chip,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
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
        withStyle(SpanStyle(color = AppColors.Purple, fontWeight = FontWeight.Bold)) {
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
