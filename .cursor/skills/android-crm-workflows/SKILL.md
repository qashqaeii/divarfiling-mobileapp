---
name: android-crm-workflows
description: Redesign complex Divar Filing CRM journeys in native Jetpack Compose, including contacts, personal properties, deals, contracts, reminders and smart matching while preserving server-backed forms.
---


# CRM Workflows & Data-Dense Forms

## زمان استفاده
برای featureهای مخاطبین، فایل شخصی، معاملات، قراردادها، امروز، یادآور، تطبیق فایل و فرم‌های ثبت/ویرایش.

## فایل‌های واقعی مهم
- `feature/crm/CrmScreens.kt`: `ContactsScreen`, `TodayScreen`, `CrmHubScreen`.
- `feature/crm/ContactDetailScreen.kt`, `ContactDetailViewModel.kt`, `components/ContactListCard.kt`, `components/ContactEditSheet.kt`.
- `feature/crm/PropertyScreens.kt`, `components/PropertyFormSheet.kt`, `components/PropertyDetailTabs.kt`, `PropertyFormState.kt`, `PropertyFeatureSchema.kt`.
- `feature/crm/DealsScreens.kt`, `DealsViewModels.kt`, `components/` معامله.
- `feature/contracts/ContractsScreens.kt` و `feature/crm/calendar/CrmCalendarScreen.kt`.
- `feature/ai/smartcommand/` و `feature/ai/voice/` برای ورودی هوشمند/صوتی، بدون بازنویسی AI logic.

## تفکیک مدل‌ها
- Listing استخراج‌شده ≠ Property شخصی؛ روابط، شناسه‌ها، URLها و عملکرد «ذخیره به‌عنوان ملک» را خراب نکن.
- Contacts ≠ Owner ≠ Agency published contact. حریم خصوصی، سطح دسترسی و نقش‌های تیم حفظ شود.
- دیتای سرور و endpointهای آن source of truth؛ ViewModel/Repository را فقط برای اتصال UI لازم لمس کن.

## الگوی UX برای لیست
- نخست «چه کسی/چه ملکی/کدام معامله» و وضعیت قابل تصمیم‌گیری؛ بعد metadata، سپس اکشن‌ها.
- Header موجز + Search، یک entrypoint فیلتر؛ Chips فقط active filters؛ Reset واضح.
- هر row/card یک نیت اصلی و ۱–۳ اکشن پرتکرار؛ سایر موارد menu/detail.
- در 360dp بدون horizontal overflow و بدون تراکم ۴ ستون KPI.
- Call/WhatsApp/Message فقط در صورت داده و اجازه معتبر؛ نشانگر پیگیری بعدی و وضعیت با semantic color + text.

## الگوی UX برای جزئیات
- Hero محدود: عنوان واقعی، status، مهم‌ترین داده (قیمت/متراژ/مشخصات/مخاطب) و primary next action.
- قسمت‌های ثانویه (یادداشت، امکانات، اسناد، تاریخچه، تطبیق) در بخش‌های معنادار/تب؛ از ۱۰ کارت پشت سرهم بپرهیز.
- «اقدام بعدی» برای پیگیری مشاور روشن باشد؛ نتایج AI به‌عنوان پیشنهاد برچسب‌خورده، نه حقیقت.

## الگوی فرم بلند
1. اطلاعات ضروری → شرایط/قیمت → ویژگی‌ها → ارتباطات/تصاویر → بازبینی و ثبت (در صورت طولانی بودن واقعی).
2. از `PropertyFormState`, `PropertyFeatureSchema`, mappers/DTO های موجود استفاده؛ داده‌ها را حذف/فقدان فیلد فرض نکن.
3. برای فیلد لازم indicator، validation inline، keyboard imeAction، focus management، restore state، چراغ saving/error.
4. Guard بستن فرم dirty؛ disabled و no double submit؛ فرم هنگام scroll یا rotation نباید مقدار را از دست بدهد.
5. جستجوی مخاطب/ملک در picker؛ empty result و pagination درست؛ در صورت لود ویرایش، جلوگیری از submit قبل load.
6. فقط وقتی بخش واقعی Form زیاد است wizard/full-screen را جایگزین sheet کن؛ اگر خطر رگرسیون بالاست اول UX را در ساختار فعلی بهتر کن.

## قیود محصول
- شماره آگهی از دیوار به‌صورت خودکار استخراج نمی‌شود؛ مالک/شماره ثبت‌شده در CRM از نظر منبع واضح باشد.
- حذف/ادغام/انتقال/انتشار آژانس عملیات حساس است؛ تأیید و توضیح پیامد و feedback لازم.
- license gating، sync/offline و retry باید با رفتار موجود سازگار بماند.

## سناریوهای کنترل
- ثبت مخاطب سریع → جزئیات → ایجاد یادآور → تکمیل.
- جستجوی آگهی → ذخیره در ملک شخصی → جزئیات همان ملک.
- ملک شخصی → انتخاب/پیوست مشتری → match → یادآور/اشتراک.
- معامله → تغییر stage/یادداشت/امور مالی/قرارداد و بازگشت بدون از دست رفتن state.

برای checkهای صفحه‌به‌صفحه: `references/CRM_SCENARIOS.md`.
