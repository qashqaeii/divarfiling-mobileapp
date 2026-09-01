package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.network.PropertyCabinetDto
import ir.divarfiling.mobile.core.network.PropertyFolderDto

@Composable
fun PropertyFilingNav(
    cabinets: List<PropertyCabinetDto>,
    folders: List<PropertyFolderDto>,
    selectedCabinetId: String?,
    selectedFolderId: Long?,
    unassignedFolderCount: Int,
    totalCount: Int,
    onSelectAllCabinets: () -> Unit,
    onSelectCabinet: (PropertyCabinetDto) -> Unit,
    onSelectUnassigned: () -> Unit,
    onCreateCabinet: () -> Unit,
    onManageCabinets: () -> Unit,
    onSelectAllFolders: () -> Unit,
    onSelectFolder: (PropertyFolderDto) -> Unit,
    onCreateFolder: () -> Unit,
    onManageFolders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfPremiumCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ZonkanBrandIcon(size = 28.dp)
                Column {
                    Text(
                        text = "سازمان‌دهی فایل‌ها",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "کمد · زونکن",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }

            PropertyCabinetsRail(
                cabinets = cabinets,
                selectedCabinetId = selectedCabinetId,
                unassignedFolderCount = unassignedFolderCount,
                onSelectAll = onSelectAllCabinets,
                onSelectCabinet = onSelectCabinet,
                onSelectUnassigned = onSelectUnassigned,
                onCreateCabinet = onCreateCabinet,
                onManageCabinets = onManageCabinets,
                compact = true,
            )

            HorizontalDivider(color = DfColors.OutlineSubtle.copy(alpha = 0.6f))

            PropertyFoldersRail(
                folders = folders,
                selectedFolderId = selectedFolderId,
                totalCount = totalCount,
                onSelectAll = onSelectAllFolders,
                onSelectFolder = onSelectFolder,
                onCreateFolder = onCreateFolder,
                onManageFolders = onManageFolders,
                folderMetaFor = { folder -> folder.cabinet?.name?.let { "· $it" }.orEmpty() },
                compact = true,
            )
        }
    }
}
