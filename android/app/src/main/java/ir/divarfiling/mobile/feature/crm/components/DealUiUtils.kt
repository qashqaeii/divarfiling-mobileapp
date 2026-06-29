package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import kotlin.math.absoluteValue

object DealUiUtils {
    fun dealAccentColor(seed: String): Color {
        val palette = listOf(DfColors.Purple, DfColors.Blue, DfColors.Green, DfColors.Amber, DfColors.Rose)
        return palette[seed.hashCode().absoluteValue % palette.size]
    }

    fun dealStageColors(stage: String): Pair<Color, Color> = when {
        stage.contains("از دست") || stage.contains("سرد") -> DfColors.OverdueAccent to DfColors.RoseLight
        stage.contains("بسته") -> DfColors.Green to DfColors.GreenLight
        stage.contains("قرارداد") || stage.contains("پیش") -> Color(0xFFEC4899) to Color(0xFFFCE7F3)
        stage.contains("بازدید") -> DfColors.Blue to DfColors.BlueLight
        stage.contains("مذاکره") -> DfColors.Amber to DfColors.AmberLight
        stage == "سرنخ" || stage == "جدید" -> DfColors.Blue to DfColors.BlueLight
        else -> DfColors.Purple to DfColors.PurpleContainer
    }

    fun stageIcon(stage: String): ImageVector = when {
        stage.contains("از دست") || stage.contains("سرد") -> DfIcons.TrendingDown
        stage.contains("بسته") -> DfIcons.Trophy
        stage.contains("قرارداد") || stage.contains("پیش") -> DfIcons.File
        stage.contains("بازدید") -> DfIcons.Building
        stage.contains("مذاکره") -> DfIcons.Handshake
        else -> DfIcons.Sparkles
    }

    fun stageDescription(stage: String): String = when {
        stage.contains("از دست") || stage.contains("سرد") -> "فرصت از دست رفته یا سرد شده"
        stage.contains("بسته") -> "معامله با موفقیت بسته شد"
        stage.contains("قرارداد") -> "قرارداد نهایی در حال انجام"
        stage.contains("پیش") -> "پیش‌قرارداد یا توافق اولیه"
        stage.contains("بازدید") -> "بازدید ملک یا جلسه حضوری"
        stage.contains("مذاکره") -> "مذاکره قیمت و شرایط"
        stage == "سرنخ" || stage == "جدید" -> "سرنخ اولیه — نیاز به پیگیری"
        else -> "مرحله فعلی فرصت فروش"
    }
}
