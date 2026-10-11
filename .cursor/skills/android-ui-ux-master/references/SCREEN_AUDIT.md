# قالب کنترل هدفمند صفحه

## 1. واقعیت محصول
- Route از NavHost، feature، state flow از ViewModel، API/Repository، permission/license gate، نقطه ورود/بازگشت را ثبت کن.
- «شخصی»، «آگهی استخراج‌شده»، «دیتاست»، «مخاطب» و «ملک/معامله» موجودیت یکسان نیستند؛ مدل‌ها را ادغام نکن.
- تجربه روزانه مشاور: «ثبت سریع»، «یافتن فایل مناسب»، «تماس/پیگیری»، «اشتراک/معرفی» را اولویت بده.

## 2. بررسی ۹ محور (فقط موارد اثرگذار گزارش شود)
| محور | سؤال اصلی |
| --- | --- |
| Information architecture | آیا اولین نگاه مهم‌ترین داده و اقدام را نشان می‌دهد؟ |
| Scanability | کارت بیش از سه سطح متن و ۵ badge بی‌اولویت ندارد؟ |
| Functionality | همه اکشن‌ها واقعاً متصل هستند یا فقط تزئینی؟ |
| Density | اطلاعات زیاد قابل آشکارسازی مرحله‌ای است؟ |
| Navigation | Back / selected tab / بازگشت از sheet درست است؟ |
| Empty/loading/error | state-specific و اقدام بازیابی روشن است؟ |
| Responsive | در 360dp، فونت بزرگ و IME محتوا نمی‌پرد؟ |
| A11y/RTL | screen reader، touch، contrast، BiDi/قیمت قابل‌فهم‌اند؟ |
| Efficiency | اسکرول، lazy item، refresh و recomposition طبیعی‌اند؟ |

## 3. Scorecard
در پایان برای UX، Visual, RTL, Accessibility, Performance, Regression فقط Pass/Needs Work/Not Tested بنویس؛ نمره ساختگی نده.

## 4. ضدالگوها
- Hero بی‌دلیل در تمام صفحات؛ هر صفحه یک «بیلبورد» نباشد.
- دو CTA یکسان (FAB + header add).
- Labelهای 10–11sp برای داده کلیدی.
- Bottom Sheet طولانی بدون progress/save draft/حفاظت از کاربر.
- دکمه‌های کم‌کنتراست به خاطر شیشه‌ای‌بودن.
- کپی کامل فرم وب داخل موبایل؛ اولویت‌بندی fields لازم است.
