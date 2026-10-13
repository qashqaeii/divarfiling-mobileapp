package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.core.design.DateUtils

object PostCallNoteHeaderBuilder {

    fun build(metadata: PostCallCallMetadata): String {
        val lines = mutableListOf<String>()
        lines += "یادداشت تماس"
        metadata.callLogDateMs?.let { ms ->
            val date = DateUtils.formatJalaliDateFromMillis(ms)
            val time = DateUtils.formatTimeFromMillis(ms)
            lines += "تاریخ تماس: $date، ساعت $time"
        }
        lines += "نوع تماس: ${callKindLabel(metadata.callEndKind)}"
        formatDurationLine(metadata)?.let { lines += it }
        return lines.joinToString("\n")
    }

    fun composeNoteForCrm(header: String, userBody: String): String {
        val body = userBody.trim()
        if (header.isBlank()) return body
        if (body.isBlank()) return header
        return "$header\n\nتوضیحات:\n$body"
    }

    private fun callKindLabel(kind: PostCallLogic.CallEndKind): String = when (kind) {
        PostCallLogic.CallEndKind.Answered -> "ورودی پاسخ‌داده‌شده"
        PostCallLogic.CallEndKind.Outgoing -> "خروجی"
        PostCallLogic.CallEndKind.Missed -> "ورودی بی‌پاسخ"
        PostCallLogic.CallEndKind.Ignored -> "تماس"
    }

    private fun formatDurationLine(metadata: PostCallCallMetadata): String? {
        if (metadata.callEndKind == PostCallLogic.CallEndKind.Missed) return null
        val sec = metadata.durationSec ?: return null
        if (sec <= 0L) return null
        val minutes = sec / 60
        val seconds = sec % 60
        val text = when {
            minutes > 0 && seconds > 0 ->
                "${DateUtils.toPersianDigits(minutes.toString())} دقیقه و ${DateUtils.toPersianDigits(seconds.toString())} ثانیه"
            minutes > 0 -> "${DateUtils.toPersianDigits(minutes.toString())} دقیقه"
            else -> "${DateUtils.toPersianDigits(seconds.toString())} ثانیه"
        }
        return "مدت مکالمه: $text"
    }
}
