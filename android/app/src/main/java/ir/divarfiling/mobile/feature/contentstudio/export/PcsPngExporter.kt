package ir.divarfiling.mobile.feature.contentstudio.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PcsPngExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun saveToGallery(bitmap: Bitmap, displayName: String): Result<Uri> {
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/DivarFiling")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values)
                ?: return Result.failure(IllegalStateException("ذخیره در گالری ممکن نشد"))
            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    return Result.failure(IllegalStateException("فشرده‌سازی PNG ناموفق بود"))
                }
            } ?: return Result.failure(IllegalStateException("نوشتن فایل ناموفق بود"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
