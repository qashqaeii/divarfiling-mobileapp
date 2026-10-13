package ir.divarfiling.mobile.feature.postcall

data class PostCallCallMetadata(
    val phone: String,
    val callEndKind: PostCallLogic.CallEndKind,
    val callLogDateMs: Long?,
    val durationSec: Long?,
    val callLogType: Int?,
) {
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
