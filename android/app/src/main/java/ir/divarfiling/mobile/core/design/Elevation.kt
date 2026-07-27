package ir.divarfiling.mobile.core.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Soft elevation scale — luxury through restraint, not heavy shadows. */
object AppElevations {
    val none: Dp = 0.dp
    val subtle: Dp = 1.dp
    val card: Dp = 1.5.dp
    val raised: Dp = 3.dp
    val floating: Dp = 4.dp
    val navBar: Dp = 6.dp
    val listingCard: Dp = 2.dp
    val sheet: Dp = 8.dp
}

typealias DfElevation = AppElevations
