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
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyMatchDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactMatchesSheet(
    visible: Boolean,
    matches: ContactMatchesData?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    contactPhone: String?,
    onDismiss: () -> Unit,
    onSuggest: (List<PropertyMatchDto>, shareViaWhatsApp: Boolean) -> Unit,
) {
    if (!visible) return

    val allMatches = remember(matches) {
        val grouped = matches?.matchGroups.orEmpty()
            .flatMap { it.crmMatches + it.divarMatches }
        val flat = matches?.crmMatches.orEmpty() + matches?.divarMatches.orEmpty()
        (if (grouped.isNotEmpty()) grouped else flat).sortedByDescending { it.score }
    }
    var selected by remember(allMatches) { mutableStateOf(emptySet<String>()) }

    fun matchKey(m: PropertyMatchDto): String =
        if (m.source == "crm") "crm:${m.propertyId}" else "divar:${m.token}"

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                "تطبیق هوشمند ملک",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (matches?.isBuilder == true) {
                    "پیشنهاد دوگانه — تأمین پروژه و بازار آپارتمان"
                } else {
                    "بر اساس بودجه، محله و متراژ مخاطب"
                },
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
                matches?.eligible == false -> {
                    Text(
                        matches.message ?: "تطبیق برای این نوع مخاطب فعال نیست.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                }
                allMatches.isEmpty() -> {
                    Text(
                        if (matches?.divarLocked == true) {
                            "فایل شخصی مناسبی یافت نشد. تطبیق فایلینگ دیوار نیاز به لایسنس دارد."
                        } else {
                            "ملک مناسبی برای این مخاطب پیدا نشد."
                        },
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                }
                else -> {
                    val crmCount = allMatches.count { it.source == "crm" }
                    val divarCount = allMatches.count { it.source == "divar" }
                    if (crmCount > 0) {
                        DfSectionHeader("فایل‌های شخصی", crmCount)
                    }
                    if (divarCount > 0) {
                        DfSectionHeader("فایلینگ دیوار", divarCount)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        items(allMatches, key = { matchKey(it) }) { match ->
                            MatchRow(
                                match = match,
                                checked = matchKey(match) in selected,
                                onCheckedChange = { checked ->
                                    val key = matchKey(match)
                                    selected = if (checked) selected + key else selected - key
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
                                    "ملک‌های مناسب را انتخاب کنید"
                                } else {
                                    "${selected.size} ملک انتخاب شده"
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
                                            allMatches.map { matchKey(it) }.toSet()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSubmitting,
                                ) {
                                    Text(if (selected.size == allMatches.size) "لغو انتخاب" else "انتخاب همه")
                                }
                                Button(
                                    onClick = {
                                        val picked = allMatches.filter { matchKey(it) in selected }
                                        onSuggest(picked, false)
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
                            if (!contactPhone.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        val picked = allMatches.filter { matchKey(it) in selected }
                                        onSuggest(picked, true)
                                    },
                                    enabled = selected.isNotEmpty() && !isSubmitting,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("ثبت و ارسال واتساپ")
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
private fun MatchRow(
    match: PropertyMatchDto,
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
                        match.title.orEmpty(),
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
                val meta = listOfNotNull(
                    match.priceLabel?.takeIf { it.isNotBlank() },
                    match.area?.let { "${it.toInt()} متر" },
                    match.neighborhood?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                }
                match.intentLabel?.takeIf { it.isNotBlank() }?.let { label ->
                    DfBadge(text = label, color = DfColors.Blue)
                }
                if (match.reasons.isNotEmpty()) {
                    Text(
                        match.reasons.joinToString(" · "),
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                    )
                }
                Text(
                    if (match.source == "crm") "فایل شخصی" else "فایلینگ دیوار",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}
