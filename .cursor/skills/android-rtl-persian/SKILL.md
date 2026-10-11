---
name: android-rtl-persian
description: Implement Persian RTL-first Compose UX for Iranian real-estate workflows: Jalali dates, mixed Persian/Latin text, phone and money fields, Farsi wording, numeral formatting and accessibility.
---


# RTL Persian & Iranian Domain Formatting

## هدف
رابط‌کاربری دقیق برای مشاور ایرانی، با متن فارسی، کلمات لاتین مثل CRM/AI، شماره تماس، آدرس، لینک آگهی، واحد پول و تاریخ جلالی.

## منابع واقعی
- `core/design/Theme.kt`: `LocalLayoutDirection provides LayoutDirection.Rtl`.
- `core/design/DateUtils.kt`: ابزارهای تاریخ فارسی/جلالی موجود.
- `core/design/FormatUtils.kt`, `PresentationLabels.kt`: نمایش عدد، قیمت، متن.
- `core/design/components/DfMoneyField.kt`, `DfDateTime.kt`: فیلدها و تاریخ.
- `core/util/PhoneNormalizer.kt` (اگر در پروژه وجود دارد)؛ `feature/ai/voice/VoiceTextField.kt`.

## قواعد RTL
1. RTL را در Theme نگه دار؛ روی تمام Column/Rowها به‌طور کورکورانه RTL دوباره نگذار.
2. `start/end` و `Arrangement` سازگار با layout direction؛ `left/right` فقط برای مختصات واقعی نقشه/تصویر.
3. آیکون Back باید AutoMirrored یا جهت درست فارسی؛ فلش مورد نیاز برای خروجی واقعی را با تصویر خنثی اشتباه نکن.
4. شماره موبایل، URL، کد، مختصات و قطعه انگلیسی در صورت نیاز با متن/فیلد LTR و BiDi isolation امن نمایش داده شود؛ **کل فرم** را LTR نکن.
5. نمایش `ریال/تومان` صریح و یکنواخت؛ محاسبه یا ذخیره‌سازی سرور را تبدیل نکن. عدد نمایش‌داده‌شده با API متفاوت نشود.
6. برای تاریخ از `DateUtils` موجود و منطق جلالی استاندارد پروژه استفاده کن؛ parse/stringify چندنقطه‌ای ننویس.
7. شماره تماس در UI legible، `KeyboardType.Phone`، call action با شماره normalizeشده؛ formatting دیداری با داده canonical تفاوت داشته باشد.

## واژه‌نامه محصول
- Dataset = «دیتاست» یا «مجموعه فایل‌ها» مطابق برچسب موجود بخش؛ در یک جریان ثابت بماند.
- Listing = «آگهی/فایل استخراج‌شده»، با `Property` = «ملک شخصی» یکسان نیست.
- Contact = «مخاطب/مشتری»، Deal = «معامله»، Reminder = «یادآور»، Extract = «استخراج»، Owner = «مالک» فقط وقتی داده و برچسب آن قطعی است.
- پیام‌های کوتاه و مستقیم: «ذخیره ملک»، «پیگیری امروز»، «نمایش فایل»، «رفع فیلتر»؛ از ادعاهای مبهم یا تبلیغاتی در UI عملیاتی پرهیز.

## آزمون نمایش
- نام مشتری و محله فارسی طولانی، شماره `09...`، آیدی `AI/CRM`, رقم‌های مخلوط، عدد بزرگ قیمت، مبلغ ناموجود، تاریخ جلالی، fallback خالی.
- بزرگ‌شدن فونت، اعداد در Chip، فیلد شماره در حالت RTL، Voice input ترکیبی.
- accessible label کامل؛ ellipsis اطلاعات حساس مثل مبلغ/شماره را مخفی نکند.

## Done
پیش از پایان، سه مثال واقعی عدد/قیمت/تاریخ از feature بررسی‌شده در Preview یا تست ارائه کن؛ **بدون اطلاعات خصوصی واقعی**.
