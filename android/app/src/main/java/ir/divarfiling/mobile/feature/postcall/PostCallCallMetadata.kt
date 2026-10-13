package ir.divarfiling.mobile.feature.postcall

data class PostCallCallMetadata(
    val phone: String,
    val callEndKind: PostCallLogic.CallEndKind,
    val callLogDateMs: Long?,
    val durationSec: Long?,
    val callLogType: Int?,
) {
    /** فقط فیلدهای معتبر Call Log در سربرگ یادداشت استفاده شوند. */
    fun withValidCallLogFieldsOnly(): PostCallCallMetadata = copy(
        callLogDateMs = callLogDateMs?.takeIf { it > 0L },
        durationSec = durationSec?.takeIf { it > 0L },
    )

    companion object {
        fun fromResolved(
            phone: String,
            kind: PostCallLogic.CallEndKind,
            resolved: PostCallLogic.ResolvedCallLog?,
        ): PostCallCallMetadata = PostCallCallMetadata(
            phone = phone,
            callEndKind = kind,
            callLogDateMs = resolved?.dateMs?.takeIf { it > 0L },
            durationSec = resolved?.durationSec?.takeIf { it > 0L },
            callLogType = resolved?.logType,
        )
    }
}
