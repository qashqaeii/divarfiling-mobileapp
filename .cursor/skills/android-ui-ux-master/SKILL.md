---
name: android-ui-ux-master
description: Orchestrate phased UX and UI redesign of the Divar Filing native Android Kotlin Jetpack Compose app; use for full-screen redesign planning, feature audits, and acceptance reviews.
---


# Android UI/UX Master — Divar Filing

## مأموریت
بازطراحی **بومی اندروید** برای مشاور املاک؛ محصولی سریع، واضح، باکیفیت، حرفه‌ای و راست‌به‌چپ. Design direction: **Bold Premium / Quiet Luxury**؛ شخصیت پرقدرت با سلسله‌مراتب عالی، نه کارت/گرادیان/Glass بیشتر. این اسکیل هماهنگ‌کننده ۷ اسکیل تخصصی دیگر است.

## چه زمان فعال شود؟
- درخواست «بازطراحی اپ / صفحه / UX»، اولویت‌بندی صفحات، انتخاب الگو یا کنترل کیفیت نهایی.
- در تغییرات مربوط به یک صفحه، فقط **اسکیل‌های موضوعی مرتبط** را به‌صورت هدفمند بخوان؛ همه اسکیل‌ها را بی‌دلیل در کانتکست نریز.

## منابع واقعی قبل از شروع
- `android/app/src/main/java/ir/divarfiling/mobile/navigation/DivarFilingNavHost.kt` : مسیرهای واقعی و پنج تب اصلی.
- `android/app/src/main/java/ir/divarfiling/mobile/core/design/` : Color, Theme, Typography, Spacing, Shape, Elevation و components.
- `android/app/src/main/java/ir/divarfiling/mobile/feature/` : صفحات واقعی؛ از روی کد تصمیم بگیر نه اسناد قدیمی.
- `docs/MOBILE_DAILY_WORKSPACE_PLAN.md`, `docs/ECOSYSTEM_ROLES.md` : قیود نقش محصول؛ اگر تضاد بود کد جاری + رفتار API اولویت دارد.
- ریشه اندروید `android/` و اپ در `android/app/`؛ Gradle ماژول‌های فرضی نساز.

## محدودیت‌های تغییرناپذیر
1. برند بنفش اصلی **`#5B4FCF`**، فونت **Vazirmatn**، فارسی، RTL و دو تم حفظ شوند؛ رنگ/هویت/ساختار تجاری بدون تأیید عوض نشود.
2. کد **Kotlin + Jetpack Compose + Material3** است؛ از React/HTML/WebView به‌عنوان جایگزین UI نیتیو استفاده نکن.
3. API Django منبع حقیقت؛ Repository/ViewModel، شماره‌ها، sync، permissions، license، extraction و قراردادهای JSON را به بهانه طراحی دست‌کاری نکن.
4. اگر `WorkspaceWebScreen` یا صفحه لینک وب است آن را نیتیو جا نزن. هیچ API/داده/وضعیت ساختگی در نسخه اجرایی وارد نکن.
5. مرورگر خارجی یا پروژه دیگری را بدون اجازه کاربر باز نکن. Deploy، push و تغییر نسخه/Gradle را بدون درخواست انجام نده.
6. اسکیل‌های این پکیج را به‌عنوان پروژه‌محور استفاده کن؛ User Rules قبلی را پاک/بازنویسی نکن.

## روش عملیاتی (کم‌مصرف و قابل کنترل)
1. **Discover:** فایل صفحه و اجزای مستقیمش + ViewModel و route همان صفحه را بخوان؛ تمام کدبیس را هر بار scan نکن.
2. **UI inventory:** هدف کاربر، اقدام اصلی، اقدام دوم، داده مهم، states، خطرات فعلی (حداکثر ۸ یافته اثرگذار).
3. **Design brief کوتاه:** hierarchy صفحه، توالی اطلاعات، layout، CTA، تعامل، empty/loading/error/offline، اثر روی Navigation و accessibility. اولویت P0/P1/P2.
4. **Reuse first:** قبل از ساخت هر کامپوننت جدید `core/design/components/` را بررسی کن؛ shared component را فقط در صورت تکرار واقعی تعمیم بده.
5. **Implement one cohesive slice:** یک صفحه یا جریان نزدیک به هم؛ بدون redesign سراسری در یک patch و بدون refactor unrelated.
6. **Verify:** diff، کامپایل یا تست مرتبط، Compose Preview/اسکرین‌شات در صورت امکان، مشکلات کوچک/بزرگ، توصیف شفاف آنچه واقعاً تست شده.
7. **Close:** فایل‌های تغییرکرده + دلیل UX + تست/محدودیت + صفحات بعدی؛ خلاصه، بدون گزارش بلند تکراری.

## ماتریس صفحه و اولویت اجرایی
- **Foundation:** `core/design/`، Bottom Navigation، header، keyboard/insets، اجزای مشترک.
- **P0:** `feature/home`، `feature/filing` (datasets / listings / detail / filters)، `feature/extract`، `feature/crm` (contacts / today / properties / deals).
- **P1:** CRM detail/creates، تقویم، قراردادها، AI/voice، نقشه، global search، اشتراک/لایسنس.
- **P2:** تیم/فضای آژانس، ابزارها، پشتیبانی، اعلان‌ها، تنظیمات، ورود، آموزش/bridge.
- اولویت را با task واقعی و ارزش کاربر تنظیم کن؛ لیست بالا راهنمای پیش‌فرض است نه الزام اجرای خطی.

## معیار پایان هر صفحه (DoD)
- هدف و CTA غالب زیر ۳ ثانیه قابل فهم؛ بدون شلوغی بصری و عنوان تکراری.
- حالت‌های Loading، Empty، Error، Offline/Retry (اگر کاربرد دارد)، Success و Permission/Locked پوشش داده شده.
- RTL، تاریخ شمسی، اعداد و برچسب‌های قیمت درست؛ long title و text scaling باعث clipping نشوند.
- سطح لمس حداقل 48dp، معنا برای screen reader، کنتراست مناسب، بازگشت و کیبورد درست.
- Dark/Light، گوشی 360dp، گوشی معمولی و فضای بزرگ‌تر حداقل طراحی/بررسی شده.
- داده واقعی، navigation و عملیات CRUD سالم؛ هیچ عملکرد قبلی در اثر طراحی حذف نشده.
- نتیجه تست شفاف: «اجرا شد/اجرا نشد»؛ سبز شدن CI به‌تنهایی تأیید UX نیست.

## خروجی لازم در پاسخ Cursor
`صفحه/جریان | ۳ ایراد مهم | تغییرات | فایل‌ها | تست‌ها | خطر/گام بعدی`، حداکثر در ۱۰ تا ۱۵ خط مگر خطای بحرانی.

مطالعه اختیاری وقتی لازم بود: `references/SCREEN_AUDIT.md`.
