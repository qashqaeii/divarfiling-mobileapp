package ir.divarfiling.mobile.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ExportShareHelper {
    fun shareFile(context: Context, file: File, mimeType: String, title: String = "اشتراک فایل") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
