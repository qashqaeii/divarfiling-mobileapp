# آپدیت داخلی اپ اندروید

اپ نسخهٔ جدید را از سرور Divar Filing می‌گیرد و نصب می‌کند.

## جریان کاربر

1. هنگام باز شدن اپ (حداکثر هر ۱۲ ساعت) نسخه چک می‌شود  
2. اگر نسخه جدید باشد → شیت «نسخه جدید»  
3. دانلود APK با نوار پیشرفت → نصب‌کننده سیستم  
4. در **بیشتر → بروزرسانی اپ** هم دستی قابل چک است  
5. اگر `force_update` یا نسخه زیر حداقل پشتیبانی باشد، بستن شیت ممکن نیست  

## انتشار نسخه جدید (عملیات)

1. APK امضاشده با **همان keystore** بسازید  
2. فایل را در سرور بگذارید:

```text
django/files/divar-filing-v{VERSION}-{BUILD}-release.apk
```

3. در `django/shop/services/app_downloads.py` به‌روز کنید:

- `ANDROID_APK_FILENAME`
- `ANDROID_APP_VERSION`
- `ANDROID_APP_BUILD`
- `ANDROID_RELEASE_NOTES`
- اختیاری: `ANDROID_FORCE_UPDATE = True`
- اختیاری: `ANDROID_APK_SHA256` (هش فایل برای تأیید صحت)
- در صورت نیاز: `ANDROID_MIN_SUPPORTED_BUILD`

4. جنگو را دیپلوی کنید  
5. نسخهٔ قبلی اپ روی گوشی، شیت آپدیت را نشان می‌دهد  

## API

- `GET /api/mobile/v1/app/version?current_build={versionCode}`
- دانلود: `/download/android/`

## نکات امنیتی / نصب

- مجوز `REQUEST_INSTALL_PACKAGES` در مانیفست  
- کاربر باید «نصب از این منبع» را برای اپ اجازه دهد  
- همیشه با certificate قبلی امضا کنید؛ وگرنه نصب روی نسخهٔ موجود شکست می‌خورد  
