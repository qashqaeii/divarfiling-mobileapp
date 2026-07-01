package ir.divarfiling.mobile.core.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

object DossierShareActions {

    fun shareText(context: Context, message: String, chooserTitle: String = "اشتراک پرونده") {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                },
                chooserTitle,
            ),
        )
    }

    fun openWhatsApp(context: Context, message: String) {
        val uri = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun copyToClipboard(context: Context, text: String, label: String = "dossier_share") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
