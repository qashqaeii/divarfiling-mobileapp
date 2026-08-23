package ir.divarfiling.mobile.core.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import ir.divarfiling.mobile.core.util.PhoneNormalizer

object DossierShareActions {
    private const val BALE_PACKAGE = "ir.nasim"

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

    fun openWhatsApp(context: Context, message: String, phone: String? = null) {
        val digits = PhoneNormalizer.digitsForMessaging(phone)
        val uri = if (digits.isNotBlank()) {
            Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")
        } else {
            Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
        }
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            .onFailure {
                Toast.makeText(context, "واتساپ در دسترس نیست", Toast.LENGTH_SHORT).show()
            }
    }

    fun openTelegram(context: Context, message: String) {
        val uri = Uri.parse("https://t.me/share/url?url=&text=${Uri.encode(message)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun openBale(context: Context, message: String, phone: String? = null) {
        val baleUser = PhoneNormalizer.baleUserId(phone)
        if (baleUser.isNotBlank()) {
            val opened = runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ble.ir/$baleUser")))
            }.isSuccess
            if (opened) return
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage(BALE_PACKAGE)
        }
        try {
            context.startActivity(send)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/$BALE_PACKAGE")))
                Toast.makeText(context, "بله نصب نیست. صفحه نصب باز شد.", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                shareText(context, message, "ارسال با بله")
            }
        }
    }

    fun openSms(context: Context, message: String, phone: String? = null) {
        val normalized = PhoneNormalizer.normalize(phone)
        val uri = if (normalized.isNotBlank()) {
            Uri.parse("smsto:$normalized")
        } else {
            Uri.parse("smsto:")
        }
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", message)
                },
            )
        }.onFailure {
            Toast.makeText(context, "امکان ارسال پیامک نیست", Toast.LENGTH_SHORT).show()
        }
    }

    fun dial(context: Context, phone: String) {
        val normalized = PhoneNormalizer.normalize(phone)
        if (normalized.isBlank()) return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalized")))
        }.onFailure {
            Toast.makeText(context, "امکان برقراری تماس نیست", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "dossier_share") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
