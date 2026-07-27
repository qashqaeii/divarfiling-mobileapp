# Divar Filing Mobile — میزکار اصلی مشاور

اپ اندروید **میزکار روزانه** اکوسیستم فایلینگ دیوار است. کاربر بدون ویندوز می‌تواند CRM، فایلینگ، تحلیل، نقشه، استخراج سبک/ابری، ابزارها، AI و پشتیبانی را از موبایل انجام دهد.

```
Django (مرکز + استخراج ابری)
   ▲
   ├── Android (میزکار اصلی موبایل)
   ├── Web (فروشگاه / پرداخت / گزارش حجیم)
   └── Windows (اختیاری — قدرت‌کارها)
```

## وضعیت

| مرحله | وضعیت |
|-------|--------|
| نقشه راه v3 — میزکار اصلی | ✅ |
| اپ Kotlin/Compose (v2.5.0) | ✅ |
| API موبایل Django | ✅ |
| نقشه / insights / تطبیق ملک | ✅ |
| استخراج ابری | ✅ |
| تیم / AI / پشتیبانی / قالب‌ها | ✅ |
| انتشار کافه‌بازار / Play Protect | 📄 [راهنما](docs/CAFEBAZAAR_PLAY_PROTECT.md) |

## ساختار پروژه

```
├── android/          ← کد اپ (Kotlin + Compose)
├── docs/             ← معماری و API
├── .github/          ← CI (Build APK)
└── ROADMAP.md
```

## اجرای محلی

**توسعه (دیباگ):**
```bash
cd android
./gradlew assembleDebug
```

**Release امضا‌شده (مثل CI):**
```bash
export ANDROID_KEYSTORE_PATH=/path/to/divarfiling-release.keystore
export ANDROID_KEYSTORE_PASSWORD=...
export ANDROID_KEY_ALIAS=divarfiling
export ANDROID_KEY_PASSWORD=...
./gradlew assembleRelease
```

خروجی release: `app/build/outputs/apk/release/app-release.apk`

نیازمند: JDK 17، Android SDK (API 35)

**هرگز APK دیباگ به کافه‌بازار نفرستید.** جزئیات: [docs/CAFEBAZAAR_PLAY_PROTECT.md](docs/CAFEBAZAAR_PLAY_PROTECT.md)

## قابلیت‌های کلیدی

- CRM: مخاطبین، معاملات، املاک، امروز، تطبیق هوشمند
- فایلینگ: لیست، جستجو، export، **تحلیل**، **نقشه**
- استخراج سبک روی گوشی + **استخراج ابری** روی سرور
- ابزارهای هوشمند، قالب پیام، تقویم
- دستیار AI، تیکت پشتیبانی، آکادمی (لینک وب)، تیم (میزکار وب)
- Push / deep link

## اسناد

| سند | مسیر |
|-----|------|
| نقش اکوسیستم | [docs/ECOSYSTEM_ROLES.md](docs/ECOSYSTEM_ROLES.md) |
| قرارداد API | [docs/MOBILE_API_SPEC.md](docs/MOBILE_API_SPEC.md) |
| نصب و Play Protect | [docs/INSTALL_GUIDE_FA.md](docs/INSTALL_GUIDE_FA.md) |
| کافه‌بازار | [docs/CAFEBAZAAR_PLAY_PROTECT.md](docs/CAFEBAZAAR_PLAY_PROTECT.md) |

## پشتیبانی

- وب: https://divarfiling.ir
- حریم خصوصی: https://divarfiling.ir/privacy/
- تلگرام: @hosseinQashqaeii
