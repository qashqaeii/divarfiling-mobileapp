---
name: android-compose-design-system
description: Build and maintain the Divar Filing Jetpack Compose Material 3 design system, tokens, reusable headers, cards, actions, forms, surfaces, dark/light theme and premium visual consistency.
---


# Compose Design System — Bold Premium

## محدوده
فقط کد Compose زیر `android/app/src/main/java/ir/divarfiling/mobile/core/design/` و اجزای UI که از آن استفاده می‌کنند. اول توکن‌ها و اجزای فعلی را بخوان؛ Design System جدید موازی نساز.

## نقشه مرجع واقعی
- `Color.kt`: `AppColors`, `DfThemeColors` — تأکید `#5B4FCF`؛ dark semantic colors.
- `Theme.kt`: `DivarFilingTheme` / `MaterialTheme` / RTL.
- `Typography.kt`: `VazirmatnFamily`, `AppTypography`, `DfTypography`.
- `Spacing.kt`: `AppSpacing` (4/8/12/16/20/24/32/40dp).
- `Shape.kt`, `Elevation.kt`, `Animation.kt`.
- `components/`: `DfComponents.kt`, `PremiumComponents.kt`, `DfHubPageHeader.kt`, `DfDetailPageHeader.kt`, `DfBottomSheet.kt`, `DfTextField.kt`, `DfSkeleton.kt`, `DfScreenScaffold.kt`, `GlassComponents.kt`, `DfScreenBackground.kt`, `DfSnackbar.kt`.

## اصل بصری
**Bold Premium = وضوح و شخصیت برند، نه افکت فراوان.** بیشترین وزن بصری برای اطلاعات/اقدام اصلی. سطح پیش‌فرض ساده، رنگ بنفش برای اقدام مهم، سایه ظریف، whitespace حساب‌شده، آیکون یکپارچه؛ گرادیان/Glass فقط در سطح‌های محدود و تأییدشده (مثل CTA/Navigation)، نه همه کارت‌ها.

## قرارداد توکن و رنگ
- هیچ hex جدید پراکنده در feature نساز مگر semantic token واقعاً جدید؛ در صورت نیاز متمرکز تعریف کن.
- در Composableها به‌جای `AppColors.Surface`, `Color.White` و `AppColors.TextPrimary` برای surface/text عمومی از `MaterialTheme.colorScheme` یا `DfThemeColors` استفاده کن.
- **ریسک مشاهده‌شده:** `DfCard(containerColor = DfColors.Surface)` پیش‌فرض سفید ثابت دارد؛ آن را در یک اصلاح کم‌ریسک theme-aware کن و callers را بررسی کن. رنگ intentional خاص قابل استثنا است.
- `GlassComponents.kt` از سفید و گرادیان ثابت استفاده می‌کند؛ در تم تیره و contrast بررسی و اگر لازم شد از semantic surface استفاده کن، بدون تغییر هویت.
- رنگ stateها semantic: success/warning/error/info/locked. تکیه صرف بر رنگ برای وضعیت ممنوع.

## تایپوگرافی و محتوا
- Vazirmatn تنها خانواده اصلی؛ مقیاس `AppTypography` / `DfTypography`، با `sp` و lineHeight متناسب.
- تیتر صفحه 20–22sp، بخش 16–18sp، محتوای اصلی 14–16sp؛ meta 12sp فقط برای metadata غیراساسی؛ labels مهم 11sp نشوند.
- وزن‌ها محدود (regular/medium/semibold/bold)؛ ۳ سطح بصری کافی است.
- متن‌های بلند با wrap یا ellipsis سنجیده؛ هر جا truncation اطلاعات حیاتی را مخفی کند راه نمایش کامل بگذار.

## قرارداد کامپوننت
- `DfCard`: title/description/actions slots؛ بدون nested clickable غیرقابل‌تشخیص؛ کلیک کارت و اکشن‌های مستقل semantically تفکیک شوند.
- `DfHubPageHeader`/`DfDetailPageHeader`: عنوان تکراری نشود؛ back فقط در جزئیات؛ toolbar حداکثر اکشن‌های فوری، بقیه overflow.
- `DfPrimaryButton`/secondary/destructive: default minHeight 48dp، loading با disabled state و feedback. CTA اصلی صفحه یکی.
- `DfBottomSheet`/`DfSheetScaffold`: header کوتاه، body scrolling، footer sticky امن، IME/insets، خروج با تأیید در فرم dirty.
- `DfTextField`: label واقعی، helper/error، keyboardType و imeAction؛ validation به‌جای toast مبهم کنار فیلد.
- skeleton/loading، empty، error، success، locked هم‌خانواده و قابل تمایز.
- برای screenهای پُرمتن UI را با reusable building blocks بساز؛ از ساخت طراحی یکسان برای تمام featureها خودداری کن.

## اجرای قابل قبول
1. برای هر اصلاح shared، usage sites مرتبط را جستجو کن؛ API کامپوننت را بی‌دلیل نشکن.
2. Preview روشن/تاریک و فارسی واقعی بساز یا نمونه موجود را گسترش بده.
3. از overdraw، blur شدید، `rememberInfiniteTransition` نامحدود در لیست‌ها و تصاویر بدون sizing پرهیز کن.
4. برای تغییراتی که `Scaffold`، background یا insets را لمس می‌کنند Navigation و sheet overlays را نیز چک کن.
5. Diff کوچک، compile/test مرتبط و گزارش.

## Anti-pattern
hero gradient + ۴ KPI + ۶ کارت + floating CTA در هر صفحه = عدم رعایت سبک. ظاهر لوکس از سلسله‌مراتب، تایپوگرافی و فاصله می‌آید.

برای نمونه code review دقیق و قرارداد کامپوننت: `references/COMPONENT_CONTRACTS.md`.
