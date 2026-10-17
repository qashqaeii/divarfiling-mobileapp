package ir.divarfiling.mobile.feature.postcall

import ir.divarfiling.mobile.core.design.DateUtils

object PostCallUiCopy {

    fun callKindShort(kind: PostCallLogic.CallEndKind): String = when (kind) {
        PostCallLogic.CallEndKind.Missed -> "تماس بی‌پاسخ"
        PostCallLogic.CallEndKind.Outgoing -> "تماس خروجی"
        PostCallLogic.CallEndKind.Answered -> "تماس ورودی"
        PostCallLogic.CallEndKind.Ignored -> "تماس"
    }

    fun crmStatusLabel(mode: PostCallUiMode): String = when (mode) {
        PostCallUiMode.Existing -> "در CRM"
        PostCallUiMode.New -> "مخاطب جدید"
        PostCallUiMode.Ambiguous -> "چند مخاطب"
        PostCallUiMode.Invalid -> "شماره نامعتبر"
        PostCallUiMode.Error -> "خطا در بررسی"
        PostCallUiMode.Loading -> "در حال بررسی…"
    }

    fun compactPrimaryCta(
        mode: PostCallUiMode,
        callEndKind: PostCallLogic.CallEndKind,
    ): String = when (mode) {
        PostCallUiMode.Loading -> "بررسی تماس"
        PostCallUiMode.Existing -> when (callEndKind) {
            PostCallLogic.CallEndKind.Missed -> "پیگیری تماس"
            else -> "ثبت نتیجه تماس"
        }
        PostCallUiMode.New -> "ثبت مخاطب"
        PostCallUiMode.Ambiguous -> "انتخاب مخاطب"
        PostCallUiMode.Invalid -> "بستن"
        PostCallUiMode.Error -> "تلاش دوباره"
    }

    fun sheetTitle(
        mode: PostCallUiMode,
        callEndKind: PostCallLogic.CallEndKind,
    ): String = when (mode) {
        PostCallUiMode.Ambiguous -> "انتخاب پرونده مرتبط"
        PostCallUiMode.New -> if (callEndKind == PostCallLogic.CallEndKind.Missed) {
            "تماس بی‌پاسخ"
        } else {
            "ثبت مخاطب جدید"
        }
        PostCallUiMode.Existing -> when (callEndKind) {
            PostCallLogic.CallEndKind.Missed -> "تماس بی‌پاسخ"
            PostCallLogic.CallEndKind.Outgoing -> "نتیجه تماس خروجی"
            else -> "ثبت نتیجه مکالمه"
        }
        PostCallUiMode.Invalid -> "شماره نامعتبر"
        PostCallUiMode.Error -> "خطا در ارتباط"
        PostCallUiMode.Loading -> "دستیار پس از تماس"
    }

    fun formatCallContextLine(metadata: PostCallCallMetadata): String {
        val parts = mutableListOf(callKindShort(metadata.callEndKind))
        metadata.callLogDateMs?.takeIf { it > 0L }?.let { ms ->
            parts += DateUtils.formatTimeFromMillis(ms)
        }
        metadata.durationSec?.takeIf { it > 0L && metadata.callEndKind != PostCallLogic.CallEndKind.Missed }
            ?.let { sec ->
                val m = sec / 60
                val s = sec % 60
                val duration = when {
                    m > 0 && s > 0 -> "${DateUtils.toPersianDigits(m.toString())}:${DateUtils.toPersianDigits(s.toString().padStart(2, '0'))}"
                    m > 0 -> "${DateUtils.toPersianDigits(m.toString())} دقیقه"
                    else -> "${DateUtils.toPersianDigits(s.toString())} ثانیه"
                }
                parts += duration
            }
        return parts.joinToString(" · ")
    }

    fun computeAdaptiveSheetHeightFraction(state: PostCallUiState): Float = when {
        state.isResolving -> 0.42f
        state.mode == PostCallUiMode.Ambiguous -> 0.72f
        state.mode == PostCallUiMode.Invalid || state.mode == PostCallUiMode.Error -> 0.48f
        state.mode == PostCallUiMode.New -> if (state.callEndKind == PostCallLogic.CallEndKind.Missed) 0.58f else 0.68f
        state.mode == PostCallUiMode.Existing -> when {
            state.callEndKind == PostCallLogic.CallEndKind.Missed -> 0.62f
            state.expanded -> 0.88f
            else -> 0.76f
        }
        else -> 0.55f
    }
}
