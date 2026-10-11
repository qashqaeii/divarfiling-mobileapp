---
name: android-adaptive-accessibility
description: Audit and improve native Android Compose responsive layouts, text scaling, talkback semantics, 48dp touch targets, contrast, keyboard/insets, edge-to-edge and reduced motion.
---


# Adaptive Layout & Accessibility

## فعال‌سازی
هر screen/layout shared، list, form، FAB، navigation، sheet یا تغییر در انیمیشن/gesture که روی دسترس‌پذیری و اندازه صفحه اثر می‌گذارد.

## وضعیت شناخته‌شده پروژه
- `MainActivity.kt` از `enableEdgeToEdge()` استفاده می‌کند.
- ریشه در Theme RTL است.
- `AppSpacing.screenHorizontal = 20.dp`، `bottomNavHeight = 72.dp`.
- `WindowSizeClass` در snapshot مخزن شناسایی نشد؛ پیش از افزودن dependency از `BoxWithConstraints` و patternهای فعلی در صورت کفایت استفاده کن.
- از `Dp`, `sp` و semantic color موجود استفاده کن.

## استانداردهای ضروری
- حداقل **48×48dp** برای hit target عملیاتی؛ حتی اگر icon بصری 20–24dp است.
- contrast هدف متن معمولی 4.5:1 و اجزای غیرمتنی معنی‌دار 3:1؛ با محاسبه/ابزار بررسی کن، ادعا بدون اندازه‌گیری نکن.
- contentDescription/role/stateDescription/selected برای عملیات و state مهم؛ decorationها `contentDescription=null`.
- `Modifier.clickable` روی Box بدون semantics کافی نیست: role/label/state و حداقل hit size را بسنج.
- font scaling حداقل 1.3× و ترجیحاً 1.5×، landscape در موارد مناسب، TalkBack focus order.
- IME: scroll field into view، primary CTA پشت keyboard/nav قرار نگیرد؛ `imePadding`/insets را بدون double-padding.
- feedback غیررنگی برای خطا، انتخاب، انجام‌شده، loading؛ خطا نزدیک همان فیلد.
- motion: بدون flash، animation بلند یا انیمیشن مداوم و پرهزینه؛ کاربرانی با motion sensitivity را در نظر بگیر.

## طرح تست سبک
1. 360dp portrait؛ 393dp؛ 600dp+ یا foldable اگر runtime موجود است.
2. Dark + Light، font scale 1 و 1.3، فارسی طولانی، keyboard visible.
3. ورود/خروج از BottomSheet، لیست با keyboard، scroll و navigation bar.
4. TalkBack: دکمه‌های تماس، back، tab selection، checkbox/filter، حذف، ثبت.
5. بررسی `LazyColumn` keys و list nested scrolling برای حافظه/پایداری.

## خطرات مشخص برای شروع
- `core/design/components/DfComponents.kt`: `DfFab` ورودی `contentDescription` دارد اما Box کلیک‌پذیر از آن برای semantics استفاده نکرده؛ در Scope مرتبط اصلاح و تست کن.
- `DfActionButton` حداقل ارتفاع 42dp دارد؛ حداقل tap area عملیاتی 48dp تأمین شود.
- `core/design/components/DfBottomSheet.kt`: ارتفاع درصدی از `screenHeightDp`؛ با IME و font scale و footer بررسی شود.
- `feature/crm/components/PropertyFormSheet.kt`: فرم بلند؛ full-height sheet و focus navigation تست شود.

## تعریف Done
یا تست/preview واقعی یا «بررسی کد بدون تست runtime» را صادقانه گزارش بده؛ درخواست کاربر برای UI/UX به‌خودی‌خود اجازه تغییر لایسنس/پرداخت یا API نیست.
