package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
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
    PropertyFilingNavSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        PropertyFilingNavHeader()

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

        FilingNavDashedDivider()

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
