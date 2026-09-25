package ir.divarfiling.mobile.feature.team



import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Checkbox

import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight

import ir.divarfiling.mobile.core.design.AppSpacing

import ir.divarfiling.mobile.core.design.AppTypography

import ir.divarfiling.mobile.core.design.DfColors

import ir.divarfiling.mobile.core.design.DfThemeColors

import ir.divarfiling.mobile.core.design.components.DfBadge

import ir.divarfiling.mobile.core.design.components.DfPrimaryButton

import ir.divarfiling.mobile.core.design.components.DfSearchField

import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

import ir.divarfiling.mobile.core.design.components.DfSheetActions

import ir.divarfiling.mobile.core.design.components.DfSheetScaffold

import ir.divarfiling.mobile.core.design.components.DfSheetSection

import ir.divarfiling.mobile.core.design.components.DfSoftChip

import ir.divarfiling.mobile.core.network.AgencySpaceProvenanceDto

import ir.divarfiling.mobile.core.network.AgencySpaceSourceContextDto

import ir.divarfiling.mobile.core.network.AgencySpaceTeamMemberDto



@Composable

fun AgencySpaceVisibilityBadge(

    visibility: String,

    label: String,

    modifier: Modifier = Modifier,

) {

    val isConfidential = visibility == "selected_members" ||

        label.contains("محرمان") ||

        label.contains("منتخب")

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        DfBadge(
            text = if (isConfidential) "🔒 ${label.ifBlank { "محرمانه" }}" else "🌐 ${label.ifBlank { "عمومی" }}",
            color = if (isConfidential) DfColors.Amber.copy(alpha = 0.15f) else DfColors.Blue.copy(alpha = 0.12f),
            textColor = if (isConfidential) DfColors.Amber else DfColors.Blue,
        )
    }

}



@Composable

fun AgencySpacePublisherRow(

    name: String,

    roleLabel: String = "",

    modifier: Modifier = Modifier,

) {

    Row(

        modifier = modifier.fillMaxWidth(),

        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),

        verticalAlignment = Alignment.CenterVertically,

    ) {

        DfBadge(

            text = name.take(1).ifBlank { "?" },

            color = DfColors.PurpleLight,

            textColor = DfColors.Purple,

        )

        Column(modifier = Modifier.weight(1f)) {

            Text(name, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium)

            Text(

                roleLabel.ifBlank { "منتشرکننده" },

                style = AppTypography.labelSmall,

                color = DfThemeColors.textMuted(),

            )

        }

    }

}



@Composable

fun AgencySpaceProvenanceBadge(

    provenance: AgencySpaceProvenanceDto?,

    onOpenSpaceItem: ((Long) -> Unit)? = null,

    modifier: Modifier = Modifier,

) {

    val itemId = provenance?.spaceItemId ?: return

    if (provenance.publisherName.isBlank()) return

    Row(

        modifier = modifier.fillMaxWidth(),

        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),

        verticalAlignment = Alignment.CenterVertically,

    ) {

        DfBadge(

            text = provenance.label.ifBlank { "فضای آژانس" },

            color = DfColors.PurpleLight,

            textColor = DfColors.Purple,

        )

        Text(

            text = "از ${provenance.publisherName}",

            style = AppTypography.bodyDescription,

            color = DfThemeColors.textSecondary(),

            modifier = Modifier.weight(1f),

        )

        if (onOpenSpaceItem != null) {

            DfSecondaryButton(

                text = "مشاهده",

                onClick = { onOpenSpaceItem(itemId) },

            )

        }

    }

}



@Composable

fun AgencySpacePublishSheet(

    context: AgencySpaceSourceContextDto,

    isSubmitting: Boolean,

    onDismiss: () -> Unit,

    onPublish: (visibility: String, contactShareLevel: String, memberIds: List<Long>, note: String) -> Unit,

    onManageExisting: (Long) -> Unit,

    onUnpublish: (Long) -> Unit,

) {

    var visibility by remember(context) {

        mutableStateOf(context.activeItem?.visibility ?: "agency_public")

    }

    var shareLevel by remember { mutableStateOf("summary") }

    var note by remember { mutableStateOf("") }

    var selectedMembers by remember { mutableStateOf(setOf<Long>()) }

    var memberQuery by remember { mutableStateOf("") }

    val activeId = context.activeItemId

    val filteredMembers = remember(context.teamMembers, memberQuery) {

        val q = memberQuery.trim()

        if (q.isBlank()) context.teamMembers

        else context.teamMembers.filter {

            it.name.contains(q, ignoreCase = true) ||

                it.role.contains(q, ignoreCase = true)

        }

    }



    DfSheetScaffold(

        title = if (activeId != null) "مدیریت انتشار" else "انتشار در فضای آژانس",

        subtitle = "هم‌تیمی‌ها می‌توانند این مورد را در فضای آژانس ببینند",

        onClose = onDismiss,

        footer = {

            if (activeId == null) {

                DfSheetActions(

                    primaryText = if (isSubmitting) "در حال انتشار…" else "انتشار در فضای آژانس",

                    onPrimary = {

                        onPublish(visibility, shareLevel, selectedMembers.toList(), note.trim())

                    },

                    primaryEnabled = !isSubmitting &&

                        (visibility != "selected_members" || selectedMembers.isNotEmpty()),

                    isSubmitting = isSubmitting,

                    secondaryText = "انصراف",

                    onSecondary = onDismiss,

                )

            }

        },

    ) {

        DfSheetSection(title = "محدوده دید") {

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {

                DfSoftChip(

                    text = "عمومی برای آژانس",

                    selected = visibility == "agency_public",

                    onClick = { visibility = "agency_public" },

                )

                DfSoftChip(

                    text = "فقط افراد منتخب",

                    selected = visibility == "selected_members",

                    onClick = { visibility = "selected_members" },

                )

            }

            Text(

                "اعضای خارج از محدوده، این مورد را نمی‌بینند.",

                style = AppTypography.labelSmall,

                color = DfThemeColors.textMuted(),

                modifier = Modifier.padding(top = AppSpacing.xs),

            )

        }

        if (context.sourceKind == "customer") {

            DfSheetSection(title = "اطلاعات قابل اشتراک") {

                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {

                    DfSoftChip(

                        text = "فقط اطلاعات کاری",

                        selected = shareLevel == "summary",

                        onClick = { shareLevel = "summary" },

                    )

                    DfSoftChip(

                        text = "همراه اطلاعات تماس",

                        selected = shareLevel == "full",

                        onClick = { shareLevel = "full" },

                    )

                }

                Text(

                    "شماره و راه‌های تماس فقط در حالت دوم در فضای آژانس دیده می‌شود.",

                    style = AppTypography.labelSmall,

                    color = DfThemeColors.textMuted(),

                    modifier = Modifier.padding(top = AppSpacing.xs),

                )

            }

        }

        if (visibility == "selected_members" && context.teamMembers.isNotEmpty()) {

            DfSheetSection(title = "انتخاب اعضا") {

                if (context.teamMembers.size > 6) {

                    DfSearchField(

                        value = memberQuery,

                        onValueChange = { memberQuery = it },

                        placeholder = "جستجو در اعضای تیم…",

                        modifier = Modifier.padding(bottom = AppSpacing.sm),

                    )

                }

                LazyColumn(

                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),

                    modifier = Modifier.fillMaxWidth(),

                ) {

                    items(filteredMembers, key = { it.id }) { member ->

                        AgencySpaceMemberRow(

                            member = member,

                            selected = member.id in selectedMembers,

                            onToggle = {

                                selectedMembers = if (member.id in selectedMembers) {

                                    selectedMembers - member.id

                                } else {

                                    selectedMembers + member.id

                                }

                            },

                        )

                    }

                }

                Text(

                    "${selectedMembers.size} نفر انتخاب شده‌اند",

                    style = AppTypography.labelSmall,

                    color = DfThemeColors.textSecondary(),

                    modifier = Modifier.padding(top = AppSpacing.sm),

                )

            }

        }

        DfSheetSection(title = "یادداشت (اختیاری)") {

            OutlinedTextField(

                value = note,

                onValueChange = { note = it },

                modifier = Modifier.fillMaxWidth(),

                placeholder = { Text("برای هم‌تیمی‌ها") },

                minLines = 2,

            )

        }

        if (activeId != null) {

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {

                DfPrimaryButton(

                    text = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",

                    onClick = {

                        onPublish(visibility, shareLevel, selectedMembers.toList(), note.trim())

                    },

                    enabled = !isSubmitting &&

                        (visibility != "selected_members" || selectedMembers.isNotEmpty()),

                    modifier = Modifier.fillMaxWidth(),

                )

                DfSecondaryButton(

                    text = "مشاهده در فضای آژانس",

                    onClick = { onManageExisting(activeId) },

                    modifier = Modifier.fillMaxWidth(),

                )

                DfSecondaryButton(

                    text = "لغو انتشار",

                    onClick = { onUnpublish(activeId) },

                    modifier = Modifier.fillMaxWidth(),

                )

            }

        }

    }

}



@Composable

private fun AgencySpaceMemberRow(

    member: AgencySpaceTeamMemberDto,

    selected: Boolean,

    onToggle: () -> Unit,

) {

    Row(

        modifier = Modifier

            .fillMaxWidth()

            .padding(vertical = AppSpacing.xxs),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),

    ) {

        DfBadge(

            text = member.name.take(1).ifBlank { "?" },

            color = DfColors.PurpleLight,

            textColor = DfColors.Purple,

        )

        Column(modifier = Modifier.weight(1f)) {

            Text(member.name, style = AppTypography.labelLarge)

            if (member.role.isNotBlank()) {

                Text(member.role, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())

            }

        }

        Checkbox(checked = selected, onCheckedChange = { onToggle() })

    }

}


