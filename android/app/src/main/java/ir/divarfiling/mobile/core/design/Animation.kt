package ir.divarfiling.mobile.core.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object DfAnimation {
    const val Fast = 200
    const val Medium = 350
    const val Slow = 500

    val tweenFast = tween<Float>(durationMillis = Fast, easing = FastOutSlowInEasing)
    val tweenMedium = tween<Float>(durationMillis = Medium, easing = FastOutSlowInEasing)

    fun <T> springGentle() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    fun <T> springSnappy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
