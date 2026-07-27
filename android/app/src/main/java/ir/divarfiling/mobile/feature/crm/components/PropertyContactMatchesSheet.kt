package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
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
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyContactMatchesSheet(
    visible: Boolean,
    matches: PropertyContactMatchesData?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSuggest: (List<PropertyContactMatchItemDto>) -> Unit,
) {
    if (!visible) return

    val allMatches = remember(matches) {
        matches?.matches.orEmpty().sortedByDescending { it.score }
    }
    var selected by remember(allMatches) { mutableStateOf(emptySet<Long>()) }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                "مشتریان مناسب",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "مخاطبانی که با این فایل شخصی تطبیق دارند",
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
            )

            when {
                isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xl),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = DfColors.Purple)
                    }
                }
                matches?.eligible == false || matches?.forbidden == true -> {
                    Text(
                        matches?.message ?: "تطبیق برای این فایل فعال نیست.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                }
                allMatches.isEmpty() -> {
                    Text(
                        "مشتری مناسبی برای این فایل پیدا نشد.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        items(allMatches, key = { it.customerId }) { match ->
                            PropertyContactMatchRow(
                                match = match,
                                checked = match.customerId in selected,
                                onCheckedChange = { checked ->
                                    selected = if (checked) selected + match.customerId else selected - match.customerId
                                },
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.Card,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
                        tonalElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Text(
                                text = if (selected.isEmpty()) {
                                    "مشتریان مناسب را انتخاب کنید"
                                } else {
                                    "${selected.size} مشتری انتخاب شده"
                                },
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selected = if (selected.size == allMatches.size) {
                                            emptySet()
                                        } else {
                                            allMatches.map { it.customerId }.toSet()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSubmitting,
                                ) {
                                    Text(if (selected.size == allMatches.size) "لغو انتخاب" else "انتخاب همه")
                                }
                                Button(
                                    onClick = {
                                        val picked = allMatches.filter { it.customerId in selected }
                                        onSuggest(picked)
                                    },
                                    enabled = selected.isNotEmpty() && !isSubmitting,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(end = 8.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                    Text("ثبت پیشنهاد")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyContactMatchRow(
    match: PropertyContactMatchItemDto,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = AppShapes.Card,
        color = DfColors.SurfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        match.fullName.orEmpty(),
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    DfBadge(
                        text = match.score.toString(),
                        color = when {
                            match.score >= 75 -> DfColors.Green
                            match.score >= 50 -> DfColors.Amber
                            else -> DfColors.TextMuted
                        },
                    )
                }
                match.customerType?.takeIf { it.isNotBlank() }?.let { type ->
                    Text(type, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                }
                match.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(phone, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                }
                if (match.reasons.isNotEmpty()) {
                    Text(
                        match.reasons.joinToString(" · "),
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                    )
                }
            }
        }
    }
}
