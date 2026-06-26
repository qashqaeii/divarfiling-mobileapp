package ir.divarfiling.mobile.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object AppShapes {
    val Card = RoundedCornerShape(16.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    val Button = RoundedCornerShape(14.dp)
    val ButtonPill = RoundedCornerShape(24.dp)
    val Field = RoundedCornerShape(14.dp)
    val Chip = RoundedCornerShape(10.dp)
    val Avatar = RoundedCornerShape(50)
    val Hero = RoundedCornerShape(24.dp)
    val BottomNav = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val StatCard = RoundedCornerShape(16.dp)
    val ListingCard = RoundedCornerShape(14.dp)
    val IconContainer = RoundedCornerShape(12.dp)
}

typealias DfShapes = AppShapes
