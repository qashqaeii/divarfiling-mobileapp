package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

fun parseContactAreas(raw: String): LinkedHashSet<String> =
    raw.split(",", "،", "\n", ";")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toCollection(LinkedHashSet())

fun serializeContactAreas(areas: Collection<String>): String =
    areas.map { it.trim() }.filter { it.isNotBlank() }.joinToString("، ")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactAreasPickerField(
    label: String,
    value: String,
    catalogNeighborhoods: List<String>,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = remember(value) { parseContactAreas(value) }

    fun commit(next: Collection<String>) {
        onValueChange(serializeContactAreas(next))
    }
    var expanded by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }
    var customDraft by remember { mutableStateOf("") }
    val filteredCatalog = remember(search, catalogNeighborhoods) {
        val q = search.trim()
        if (q.isBlank()) catalogNeighborhoods
        else catalogNeighborhoods.filter { it.contains(q, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfColors.Purple)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selected.size} محله",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
                Surface(
                    onClick = { expanded = !expanded },
                    enabled = enabled,
                    shape = AppShapes.Chip,
                    color = DfThemeColors.surfaceVariant(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DfThemeColors.textSecondary(),
                        )
                        Text(
                            text = if (expanded) "بستن لیست" else "انتخاب محله",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                }
            }
        }

        Text(
            text = "از محله‌های فایلینگ تیک بزنید یا محله دلخواه را اضافه کنید.",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
        )

        if (selected.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                selected.forEach { area ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            if (!enabled) return@FilterChip
                            commit(selected.filterNot { it == area })
                        },
                        label = { Text(area, style = AppTypography.labelSmall) },
                        enabled = enabled,
                        trailingIcon = {
                            Icon(
                                DfIcons.X,
                                contentDescription = "حذف",
                                modifier = Modifier.size(14.dp),
                            )
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("جستجو در محله‌های فایلینگ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = enabled,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = customDraft,
                        onValueChange = { customDraft = it },
                        label = { Text("محله دلخواه") },
                        placeholder = { Text("مثلاً ونک") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = enabled,
                    )
                    IconButton(
                        onClick = {
                            val custom = customDraft.trim()
                            if (custom.isBlank() || !enabled) return@IconButton
                            commit(selected + custom)
                            customDraft = ""
                        },
                        enabled = enabled && customDraft.trim().isNotBlank(),
                    ) {
                        Icon(DfIcons.Plus, contentDescription = "افزودن محله", tint = DfColors.Purple)
                    }
                }

                if (filteredCatalog.isEmpty()) {
                    Text(
                        text = if (catalogNeighborhoods.isEmpty()) {
                            "هنوز محله‌ای در فایلینگ شما ثبت نشده — محله دلخواه را دستی اضافه کنید."
                        } else {
                            "محله‌ای با این جستجو یافت نشد."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextMuted,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        filteredCatalog.take(48).forEach { neighborhood ->
                            val picked = neighborhood in selected
                            FilterChip(
                                selected = picked,
                                onClick = {
                                    if (!enabled) return@FilterChip
                                    commit(
                                        if (picked) selected.filterNot { it == neighborhood } else selected + neighborhood,
                                    )
                                },
                                label = { Text(neighborhood, style = AppTypography.labelSmall) },
                                enabled = enabled,
                            )
                        }
                    }
                }
            }
        }
    }
}
