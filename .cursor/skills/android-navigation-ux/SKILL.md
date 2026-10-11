---
name: android-navigation-ux
description: Improve Divar Filing Android navigation hierarchy, 5-tab bottom navigation, back-stack, deep links, screen discovery, contextual actions and native/web transitions without breaking routes.
---


# Navigation & Information Architecture

## فعال‌سازی
تغییر Tab Bar، صفحه Hub، منوی «بیشتر»، navigation از کارت، Back/DeepLink، ناوبری بین مخاطب/ملک/معامله/آگهی، مسیر انتقال به Web.

## واقعیت کد
- `navigation/DivarFilingNavHost.kt` دارای `Routes` و `composable` برای ۵۲ مقصد است.
- Tab های فعلی: `HOME` میزکار، `FILING` فایلینگ، `CRM_CONTACTS` مخاطبین، `CRM_TODAY` امروز، `MORE` بیشتر.
- `core/design/components/PremiumComponents.kt`: `DfBottomNavigation` / `DfSideNavItem`.
- `navigation/DeepLinkParser.kt`, `DeepLinkNavigator.kt`: مسیر اعلان، لینک و ورود مستقیم.
- `feature/more/MoreHubScreen.kt`: گروه‌بندی مقصدهای کمتر استفاده‌شده.
- `feature/crm/WorkspaceWebScreen.kt`: WebView bridge؛ حفظ boundary امنیت/URL.

## قوانین IA
1. پنج تب اصلی را بدون درخواست تغییر نده. نام/آیکون/ترتیب فقط بعد از استدلال UX و بررسی سازگاری به‌روزرسانی شود.
2. **اصلی:** کار امروز مشاور؛ **فرعی:** ابزارهای تخصصی. کاربری نباید برای کار پرتکرار چندین منوی مبهم را بگردد.
3. `More` هاب شاخه‌ها باشد، نه Dump بی‌اولویت از ده‌ها دکمه. گروه‌بندی براساس وظیفه، عنوان معنی‌دار و تعداد کلیک کم.
4. Search، Extract، Add Contact/Property باید با context صفحه کشف‌پذیر باشند؛ Floating + Header duplicate ممنوع.
5. مسیر جزئیات back navigation منطقی، route argument محفوظ و state restored؛ رفتار deep link مهم‌تر از ظاهر انیمیشن است.
6. onClickهای test preview را وارد production نکن؛ هیچ گزینه مرده/کلیک نمایشی در منو نگذار.

## الگوهای حرفه‌ای
- Root tab: bottom nav ثابت با indicator قابل خواندن؛ 48dp+ hit target؛ selected route semantically stateful.
- Hub: عنوان و هدف کوتاه، گروه‌های واضح، نهایتاً یک CTA اصلی، secondary actions در overflow.
- Detail: بازگشت، عنوان شفاف، وضعیت موجودیت، عملیات متناسب؛ tab/section محلی فقط اگر اطلاعات واقعاً زیاد است.
- Cross-entity: پس از save، کاربر وارد detail همان entity واقعی شود (در پروژه «save as personal property» نمونه‌ای از این الگو است).
- Form modal vs full screen: کوتاه و reversible → Sheet؛ فرم طولانی/حساس → صفحه یا wizard معنادار فقط اگر منطق موجود اجازه می‌دهد.
- External/web: وقتی واقعاً native نیست با عنوان «ادامه در وب» و امکان بازگشت روشن؛ بدون redirect ناگهانی.

## QA ناوبری
- Root tab → detail → Back → همان tab/اسکرول در صورت امکان.
- Switch tabs repeatedly: no duplicate stacks, duplicate destinations, or disappearing bottom nav.
- Notification deep link + cold/warm start؛ session/license gating ثابت.
- Start create/edit in sheet → keyboard → Back/dismiss: unsaved changes safe.
- Screen readers: selected tab role/label/state؛ دکمه «بیشتر» متمایز.
- Invalid route ID/result/loading: fail gracefully, no blank surface.

## خروجی
قبل/بعد مسیر user journey در حداکثر ۵ مرحله و فهرست routeهای تغییرکرده؛ بدون بازنویسی ناوبری مگر ضرورت مستند.
