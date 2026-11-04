package ir.divarfiling.mobile.core

import java.net.URLEncoder
import kotlin.text.Charsets

/** Percent-encode for Compose Navigation query values — pure JVM (unit-test friendly). */
object UrlQueryEncoding {
    fun encodeQueryComponent(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
