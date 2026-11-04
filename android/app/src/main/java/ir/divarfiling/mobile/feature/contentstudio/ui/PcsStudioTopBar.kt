package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
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
            Text(
                projectTitle.ifBlank { "استودیوی محتوا" },
                style = AppTypography.sectionTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(DfIcons.ChevronLeft, contentDescription = "بازگشت")
            }
        },
        actions = {
            Text(
                when (saveStatus) {
                    PcsSaveStatus.Saving -> "…"
                    PcsSaveStatus.Unsaved -> "●"
                    PcsSaveStatus.Error -> "!"
                    PcsSaveStatus.Saved -> "✓"
                },
                style = AppTypography.labelSmall,
            )
            IconButton(onClick = onExport, enabled = canExport && !exporting) {
                Text(if (exporting) "…" else "PNG")
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
                DropdownMenuItem(text = { Text("Undo") }, onClick = { menuOpen = false; onUndo() }, enabled = canUndo)
                DropdownMenuItem(text = { Text("Redo") }, onClick = { menuOpen = false; onRedo() }, enabled = canRedo)
            }
        },
    )
}
