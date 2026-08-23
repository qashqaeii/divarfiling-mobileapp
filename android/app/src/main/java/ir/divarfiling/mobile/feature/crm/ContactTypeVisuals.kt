package ir.divarfiling.mobile.feature.crm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

data class ContactTypeVisual(
    val icon: ImageVector,
    val accent: Color,
    val container: Color,
)

object ContactTypeVisuals {
    val primaryTypes = listOf("خریدار", "فروشنده", "مالک", "مستاجر", "سازنده", "سرنخ")

    fun visualFor(customerType: String?): ContactTypeVisual = when (customerType) {
        "خریدار", "متقاضی خرید" -> ContactTypeVisual(DfIcons.Handshake, DfColors.Green, DfColors.GreenLight)
        "فروشنده", "فروشنده ملک" -> ContactTypeVisual(DfIcons.Tag, DfColors.Purple, DfColors.PurpleContainer)
        "سازنده" -> ContactTypeVisual(DfIcons.Building, DfColors.Amber, DfColors.AmberLight)
        "مالک" -> ContactTypeVisual(DfIcons.KeyRound, DfColors.Blue, DfColors.BlueLight)
        "مستاجر", "متقاضی اجاره" -> ContactTypeVisual(DfIcons.Bed, DfColors.Blue, DfColors.BlueLight)
        "موجر" -> ContactTypeVisual(DfIcons.Home, DfColors.Purple, DfColors.PurpleContainer)
        "سرمایه‌گذار" -> ContactTypeVisual(DfIcons.Coins, DfColors.Amber, DfColors.AmberLight)
        "اشخاص حقوقی" -> ContactTypeVisual(DfIcons.Briefcase, DfColors.Purple, DfColors.PurpleContainer)
        "سرنخ" -> ContactTypeVisual(DfIcons.Sparkles, DfColors.Purple, DfColors.PurpleContainer)
        else -> ContactTypeVisual(DfIcons.User, DfColors.Purple, DfColors.PurpleContainer)
    }
}
