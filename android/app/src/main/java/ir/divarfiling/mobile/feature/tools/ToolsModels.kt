package ir.divarfiling.mobile.feature.tools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

enum class SmartToolId(val key: String) {
    RentCommission("rent_commission"),
    DepositConvert("deposit"),
    Compare("compare"),
    AreaPrice("area"),
    Discount("discount"),
    SalesCommission("commission"),
}

data class SmartTool(
    val id: SmartToolId,
    val number: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

val smartToolsCatalog: List<SmartTool> = listOf(
    SmartTool(
        id = SmartToolId.RentCommission,
        number = 1,
        title = "محاسبه کمیسیون اجاره",
        subtitle = "محاسبه سریع کمیسیون اجاره بر اساس مبلغ رهن و اجاره",
        icon = DfIcons.Calculator,
        tint = DfColors.Purple,
        background = DfColors.PurpleContainer,
    ),
    SmartTool(
        id = SmartToolId.DepositConvert,
        number = 2,
        title = "تبدیل رهن ↔ اجاره",
        subtitle = "تبدیل مبلغ رهن به اجاره و برعکس بر اساس نرخ بازار",
        icon = DfIcons.RotateCcw,
        tint = DfColors.Blue,
        background = DfColors.BlueLight,
    ),
    SmartTool(
        id = SmartToolId.Compare,
        number = 3,
        title = "مقایسه دو آگهی",
        subtitle = "مقایسه دقیق ویژگی‌ها، قیمت و ارزش دو ملک",
        icon = DfIcons.Scale,
        tint = DfColors.Green,
        background = DfColors.GreenLight,
    ),
    SmartTool(
        id = SmartToolId.AreaPrice,
        number = 4,
        title = "متراژ و قیمت",
        subtitle = "محاسبه قیمت هر مترمربع و تخمین قیمت کل ملک",
        icon = DfIcons.Ruler,
        tint = DfColors.Amber,
        background = DfColors.AmberLight,
    ),
    SmartTool(
        id = SmartToolId.Discount,
        number = 5,
        title = "تخفیف و مذاکره",
        subtitle = "محاسبه مبلغ تخفیف و پیشنهاد قیمت مناسب برای مذاکره",
        icon = DfIcons.Tag,
        tint = DfColors.Pink,
        background = DfColors.PinkLight,
    ),
    SmartTool(
        id = SmartToolId.SalesCommission,
        number = 6,
        title = "کمیسیون فروش",
        subtitle = "محاسبه سریع کمیسیون فروش بر اساس قیمت نهایی ملک",
        icon = DfIcons.Coins,
        tint = Color(0xFF0D9488),
        background = Color(0xFFCCFBF1),
    ),
)
