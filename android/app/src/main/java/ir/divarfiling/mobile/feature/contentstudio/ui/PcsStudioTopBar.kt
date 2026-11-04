package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.feature.contentstudio.PcsSaveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcsStudioTopBar(
    projectTitle: String,
    saveStatus: PcsSaveStatus,
    canEdit: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    exporting: Boolean,
    canExport: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "استودیوی محتوا",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
                Text(
                    projectTitle.ifBlank { "پوستر ملک" },
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(DfIcons.ArrowRight, contentDescription = "بازگشت")
            }
        },
        actions = {
            PcsSaveStatusBadge(saveStatus)
            Spacer(Modifier.width(AppSpacing.xxs))
            Surface(
                onClick = onExport,
                enabled = canExport && !exporting,
                shape = CircleShape,
                color = if (canExport && !exporting) {
                    DfThemeColors.primary()
                } else {
                    DfThemeColors.surfaceVariant()
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                ) {
                    Icon(
                        DfIcons.Download,
                        contentDescription = null,
                        tint = if (canExport && !exporting) Color.White else DfThemeColors.textSecondary(),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (exporting) "…" else "PNG",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canExport && !exporting) Color.White else DfThemeColors.textSecondary(),
                    )
                }
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(DfIcons.MoreVertical, contentDescription = "منو")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("ذخیره") },
                    onClick = { menuOpen = false; onSave() },
                    enabled = canEdit && saveStatus == PcsSaveStatus.Unsaved,
                )
                DropdownMenuItem(
                    text = { Text("بازگشت") },
                    onClick = { menuOpen = false; onUndo() },
                    enabled = canUndo,
                    leadingIcon = { Icon(DfIcons.RotateCcw, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = { Text("جلو") },
                    onClick = { menuOpen = false; onRedo() },
                    enabled = canRedo,
                    leadingIcon = { Icon(DfIcons.RefreshCw, contentDescription = null) },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DfScreenContainerColor,
            scrolledContainerColor = DfScreenContainerColor,
        ),
    )
}

@Composable
private fun PcsSaveStatusBadge(status: PcsSaveStatus) {
    val (label, tint) = when (status) {
        PcsSaveStatus.Saving -> "در حال ذخیره" to DfThemeColors.textSecondary()
        PcsSaveStatus.Unsaved -> "ذخیره نشده" to DfThemeColors.warning()
        PcsSaveStatus.Error -> "خطا" to DfThemeColors.error()
        PcsSaveStatus.Saved -> "ذخیره شد" to DfThemeColors.success()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.9f)),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = tint,
            maxLines = 1,
        )
    }
}
