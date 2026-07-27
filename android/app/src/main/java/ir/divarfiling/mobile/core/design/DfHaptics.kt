package ir.divarfiling.mobile.core.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Light haptic helpers for critical CTAs (call, save, destructive confirm).
 */
object DfHaptics {
    @Composable
    fun rememberPerformer(): DfHapticPerformer {
        val haptic = LocalHapticFeedback.current
        return DfHapticPerformer { type -> haptic.performHapticFeedback(type) }
    }
}

fun interface DfHapticPerformer {
    fun perform(type: HapticFeedbackType)

    fun tick() = perform(HapticFeedbackType.TextHandleMove)

    fun confirm() = perform(HapticFeedbackType.LongPress)

    fun reject() = perform(HapticFeedbackType.LongPress)
}
