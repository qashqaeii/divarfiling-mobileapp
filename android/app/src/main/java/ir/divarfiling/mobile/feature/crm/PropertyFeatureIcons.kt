package ir.divarfiling.mobile.feature.crm

import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfIcons

object PropertyFeatureIcons {
    fun iconFor(key: String): ImageVector = when (key) {
        "بالکن" -> DfIcons.LayoutGrid
        "حیوان خانگی مجاز" -> DfIcons.Heart
        "بازسازی شده" -> DfIcons.Wrench
        "استخر" -> DfIcons.Sparkles
        "سونا", "جکوزی" -> DfIcons.Sparkles
        "مبله" -> DfIcons.Bed
        "وام دارد" -> DfIcons.Landmark
        "قابل معاوضه" -> DfIcons.RotateCcw
        "اتاق مدیریت", "اتاق کنفرانس" -> DfIcons.Briefcase
        "فضای پذیرش" -> DfIcons.Users
        "آبدارخانه" -> DfIcons.ClipboardList
        "تابلوخور" -> DfIcons.Tag
        "ورودی مجزا" -> DfIcons.KeyRound
        "نگهبانی" -> DfIcons.Lock
        "تعداد واحد در طبقه", "تعداد کل طبقات ساختمان" -> DfIcons.Layers
        "تعداد پارکینگ" -> DfIcons.Car
        "تعداد حمام" -> DfIcons.Bath
        "تعداد سرویس بهداشتی" -> DfIcons.Bath
        "حداقل مدت قرارداد" -> DfIcons.Calendar
        "تعداد خط تلفن" -> DfIcons.Phone
        "جهت ساختمان" -> DfIcons.Compass
        "جنس کف" -> DfIcons.Ruler
        "سرویس بهداشتی" -> DfIcons.Bath
        "سرمایش", "گرمایش" -> DfIcons.Zap
        "تأمین‌کننده آب گرم" -> DfIcons.Bath
        "نوع آشپزخانه" -> DfIcons.LayoutGrid
        "جنس کابینت" -> DfIcons.LayoutList
        "مناسب برای" -> DfIcons.Users
        "نوع سند", "وضعیت سند" -> DfIcons.File
        "نمای ساختمان" -> DfIcons.Building
        "وضعیت سکونت", "وضعیت واحد", "وضعیت فعلی" -> DfIcons.Home
        "موقعیت ساختمان" -> DfIcons.MapPin
        "نوع کاربری ملک" -> DfIcons.Briefcase
        else -> DfIcons.Sparkles
    }
}
