---
name: android-motion-states
description: Standardize motion, loading, skeletons, errors, retries, empty and offline states in Divar Filing Compose screens while keeping interactions responsive and UI feedback truthful.
---


# Motion, Feedback & Screen States

## هدف
تجربه روان، قابل اعتماد و کم‌اصطکاک؛ UI را برای هر وضعیت واقعی state machine طراحی کن، نه اینکه فقط وضعیت موفق را زیبا کنیم.

## مراجع
- `core/design/Animation.kt`، `DfHaptics.kt`.
- `core/design/components/DfSkeleton.kt`, `DfStatusBanner.kt`, `DfSnackbar.kt`, `DfComponents.kt` (`DfEmptyState`,`DfPullRefresh`), `DfScreenBackground.kt`.
- State در ViewModel هر feature؛ از Compose-local fake loading در production بپرهیز.

## State contract
| نوع | نمایش | رفتار |
| --- | --- | --- |
| First load | skeleton نزدیک به layout واقعی | بدون jumping پرحجم |
| Refresh | داده قبلی باقی بماند + نشانگر کوچک | network dedupe |
| Empty | علت واقعی + یک CTA | action متصل به route معتبر |
| Filtered empty | نام فیلتر/کوئری + پاک‌کردن فیلتر | حفظ context |
| Error | خطای قابل فهم + retry | بدون پاک‌شدن داده قبلی اگر cache هست |
| Offline | برچسب واضح و قابلیت‌های موجود | state با sync سازگار |
| Saving | loading روی همان اکشن، منع کلیک تکراری | موفق/ناموفق دقیق |
| Locked | دلیل entitlement بدون تحقیر | route پلن واقعی |
| Partial data | data قابل اعتماد + هشدار کوچک | action غیرمتکی را فعال نگه دار |

## Motion principles
- کوتاه، کاربردی، predictable. Transition ظریف برای navigation/reveal؛ تغییر وضعیت فقط انیمیشنی نباشد.
- اجرای shimmer/particle/ambient background سنگین در ۲۰ آیتم list ممنوع.
- ripple/haptic در اکشن‌های مهم؛ haptic نباید به‌صورت اجباری یا روی هر اسکرول باشد.
- back gesture، scrolling، open/close sheet و bottom bar باید همزمان بدون flicker باشند.
- نمایش progress «واقعی» در استخراج؛ ETA تخیلی/ثابت تولید نکن.

## راهنمای بازبینی
1. تعیین stateهای موجود ViewModel؛ هیچ state جدیدی از تصور UX نساز.
2. messageهای user-facing را کوتاه، actionable، فارسی و specific کن.
3. recovery path برای network/permission/token/license ثبت شود.
4. از animation constants `DfAnimation` استفاده؛ Material transition را فقط در صورت سازگاری dependency.
5. تست refresh سریع، back هنگام saving، چند درخواست پیاپی، list scroll و ریترای.

## خروجی
برای هر screen فقط stateهایی را که واقعاً ساختی و اثبات کردی گزارش کن؛ phrase «تمام حالت‌ها تست شد» بدون execution log ممنوع.
