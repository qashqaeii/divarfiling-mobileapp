# راهنمای نصب اپ فایلینگ دیوار (اندروید)

## هشدار Play Protect

هنگام نصب APK از خارج Google Play ممکن است پیام زیر نمایش داده شود:

> **App blocked to protect your device**

این به معنی خراب بودن برنامه نیست. Google توسعه‌دهندگان ناشناس را هشدار می‌دهد.

### روش نصب (سایدلود)

1. فایل `app-release.apk` را از منبع رسمی دریافت کنید.
2. روی **Install anyway** (نصب در هر صورت) بزنید.
3. اگر گزینه نمایش داده نشد: **OK** → دوباره فایل APK را باز کنید.

### اگر نصب مسدود شد

- **Settings → Security → Install unknown apps** — برای Telegram یا Files اجازه نصب بدهید.
- موقتاً Play Protect را از **Google Play Store → Profile → Play Protect → Settings** می‌توانید غیرفعال کنید (پس از نصب دوباره فعال کنید).

## نسخه Release در مقابل Debug

| نوع | توضیح |
|-----|--------|
| `app-debug.apk` | برای توسعه — هشدار Play Protect محتمل‌تر |
| `app-release.apk` | امضا‌شده — برای استفاده واقعی مشاوران |

## CI / ساخت Release

متغیرهای محیطی برای امضای release در GitHub Actions:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## پس از نصب

1. با همان حساب **divarfiling.ir** وارد شوید.
2. لایسنس فعال + «استخراج سبک» لازم است.
3. پس از استخراج، فایل در **میزکار → فایلینگ دیوار** با نام استاندارد (مثل خروجی ویندوز) ظاهر می‌شود.

## پشتیبانی

- وب: https://divarfiling.ir
- تلگرام: @hosseinQashqaeii
