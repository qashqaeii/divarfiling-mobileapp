# راهنمای نصب اپ فایلینگ دیوار (اندروید)

## هشدار Play Protect

هنگام نصب APK از خارج Google Play ممکن است پیام زیر نمایش داده شود:

> **App blocked to protect your device**  
> Play Protect doesn't recognize this app's developer

این به معنی خراب بودن برنامه نیست. گوگل توسعه‌دهندگان تازه‌وارد / کم‌نصب را هشدار می‌دهد (دسته Uncommon).

### رفع هشدار Play Protect

جزئیات نصب: همین راهنما.

خلاصه:
1. فقط APK **release** امضاشده با keystore رسمی بسازید (`assembleDirectRelease`)
2. SHA-256 را از VirusTotal بگیرید و فرم Play Protect Appeals را ارسال کنید
3. ۷–۱۰ روز صبر کنید و با Play Protect روشن تست کنید

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
| `app-direct-release.apk` | امضا‌شده — انتشار مستقیم برای مشاوران |

## CI / ساخت Release

Workflow فقط **`app-direct-release.apk` امضا‌شده** می‌سازد.

Secrets در GitHub → Settings → Secrets and variables → Actions:

| Secret | توضیح |
|--------|--------|
| `ANDROID_KEYSTORE_BASE64` | فایل keystore به صورت base64 |
| `ANDROID_KEYSTORE_PASSWORD` | رمز keystore |
| `ANDROID_KEY_ALIAS` | alias کلید |
| `ANDROID_KEY_PASSWORD` | رمز کلید |

متغیرهای محیطی برای ساخت محلی release:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

**بکاپ keystore اجباری است.** از دست دادن کلید یعنی عدم امکان به‌روزرسانی همان اپ در بازار.

## پس از نصب

1. با همان حساب **divarfiling.ir** وارد شوید.
2. لایسنس فعال + «استخراج سبک» لازم است.
3. پس از استخراج، فایل در **میزکار → فایلینگ دیوار** ظاهر می‌شود.

## حریم خصوصی و قوانین

- حریم خصوصی: https://divarfiling.ir/privacy/
- قوانین: https://divarfiling.ir/terms/

## پشتیبانی

- وب: https://divarfiling.ir
- تلگرام: @hosseinQashqaeii
