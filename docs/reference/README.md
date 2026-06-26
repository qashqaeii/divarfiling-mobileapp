# مراجع خارج از ریپو (فقط مطالعه)

این فایل‌ها از پروژهٔ ویندوز/استخراج کپی شده‌اند تا توسعهٔ **استخراج سبک** در اندروید بدون دسترسی به پوشهٔ اصلی ممکن باشد.

| فایل | منبع | کاربرد |
|------|------|--------|
| [export_divar_items.py](./export_divar_items.py) | `src/export_divar_items.py` | منطق HTTP و پارس پاسخ‌های API دیوار (مرجع برای `DivarLightClient`) |
| [places-web.json](./places-web.json) | `config/places-web.json` | درخت شهر/محله — نسخهٔ embed در اپ: `android/app/src/main/assets/places-web.json` |

**توجه:** این فایل‌ها در بیلد اندروید استفاده نمی‌شوند. flatten و ingest روی سرور Django (`mobile_api`) انجام می‌شود.
