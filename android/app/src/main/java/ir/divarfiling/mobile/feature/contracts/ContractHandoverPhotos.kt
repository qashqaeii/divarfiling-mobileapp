package ir.divarfiling.mobile.feature.contracts

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContractsRepository
import java.io.File

@Composable
fun HandoverPhotosGrid(
    contractId: Long,
    photos: List<HandoverPhotoItem>,
    repository: ContractsRepository,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) return
    var previewFile by remember { mutableStateOf<File?>(null) }
    var previewTitle by remember { mutableStateOf("") }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text("تصاویر (${photos.size})")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            modifier = Modifier.height((112 * ((photos.size + 2) / 3)).coerceAtMost(340).dp),
        ) {
            items(photos, key = { it.id }) { photo ->
                HandoverPhotoThumb(
                    contractId = contractId,
                    photo = photo,
                    repository = repository,
                    onOpen = { file ->
                        previewFile = file
                        previewTitle = photo.title
                    },
                )
            }
        }
    }
    previewFile?.let { file ->
        AlertDialog(
            onDismissRequest = { previewFile = null },
            confirmButton = { TextButton(onClick = { previewFile = null }) { Text("بستن") } },
            title = { Text(previewTitle.ifBlank { "تصویر ملک" }) },
            text = {
                val bitmap = remember(file) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = previewTitle,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FitWidth,
                    )
                } else {
                    Text("نمایش تصویر ممکن نیست")
                }
            },
        )
    }
}

@Composable
private fun HandoverPhotoThumb(
    contractId: Long,
    photo: HandoverPhotoItem,
    repository: ContractsRepository,
    onOpen: (File) -> Unit,
) {
    var loading by remember(photo.id) { mutableStateOf(true) }
    var bitmap by remember(photo.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var cachedFile by remember(photo.id) { mutableStateOf<File?>(null) }
    LaunchedEffect(photo.id) {
        loading = true
        when (
            val result = repository.downloadAttachment(
                contractId,
                photo.id,
                "ho_photo_${photo.id}.jpg",
            )
        ) {
            is ApiResult.Success -> {
                cachedFile = result.data
                bitmap = BitmapFactory.decodeFile(result.data.absolutePath)?.asImageBitmap()
                loading = false
            }
            is ApiResult.Error -> loading = false
        }
    }
    DfCard(
        modifier = Modifier.height(100.dp),
        onClick = cachedFile?.let { file -> ({ onOpen(file) }) },
    ) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            when {
                loading -> CircularProgressIndicator()
                bitmap != null -> Image(
                    bitmap = bitmap!!,
                    contentDescription = photo.title,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Crop,
                )
                else -> Text(photo.title.ifBlank { "عکس" }, modifier = Modifier.padding(4.dp))
            }
        }
    }
}
