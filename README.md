# Divar Filing Mobile — همراه هوشمند مشاور

اپ اندروید **یکی از سه جزء** اکوسیستم «فایلینگ دیوار» است — نه جایگزین نرم‌افزار ویندوز.

```
Django (مرکز)  ◄──  Windows (استخراج حرفه‌ای)
      ▲
      └──  Android (CRM + فایلینگ + استخراج سبک)
```

## وضعیت

| مرحله | وضعیت |
|-------|--------|
| نقشه راه v2 | ✅ |
| اسکلت اندروید (فاز ۰–۱) | ✅ |
| API موبایل Django | ⏳ باید روی سرور پیاده شود |

## ساختار پروژه

```
├── android/          ← کد اپ (Kotlin + Compose)
├── docs/             ← معماری و API
├── .github/          ← CI (Build APK)
└── ROADMAP.md
```

> این ریپو **مستقل** از پروژهٔ اصلی (Django + ویندوز) است. API سرور در ریپوی backend (`django/mobile_api`) نگهداری می‌شود.

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

## Build APK with GitHub Actions

با هر `push` روی branch اصلی (`main` / `master`) — در صورت تغییر فایل‌های داخل `android/` — workflow **Build APK** یک **`app-release.apk` امضا‌شده** می‌سازد.

روی **pull request** فقط lint اجرا می‌شود (بدون artifact).

همچنین می‌توانید دستی اجرا کنید: **Actions → Build APK → Run workflow**

### Secrets لازم در GitHub

| Secret | توضیح |
|--------|--------|
| `ANDROID_KEYSTORE_BASE64` | فایل `.keystore` به صورت base64 |
| `ANDROID_KEYSTORE_PASSWORD` | رمز keystore |
| `ANDROID_KEY_ALIAS` | نام alias (مثلاً `divarfiling`) |
| `ANDROID_KEY_PASSWORD` | رمز کلید |

ساخت base64 از keystore (PowerShell):
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("divarfiling-release.keystore"))
```

### دانلود APK

1. GitHub → **Actions**
2. workflow **Build APK**
3. آخرین **Run** موفق (job `release`)
4. بخش **Artifacts** → `divar-filing-release-apk`

## قابلیت‌های فعلی اپ

| بخش | وضعیت |
|-----|--------|
| ورود + JWT | ✅ |
| لایسنس (cache + API) | ✅ |
| CRM — مخاطبین، سرنخ سریع، امروز | ✅ |
| فایلینگ — dataset و آگهی (از سرور) | ✅ |
| **استخراج سبک** | ✅ — **فقط با لایسنس فعال** |
| Push (FCM) | ⏳ فاز بعد |
| نقشه | ⏳ فاز بعد |

### استخراج سبک — قوانین

- فقط کاربران با **لایسنس فعال** و `light_extract` / `mobile_extract_enabled`
- حداکثر **۱۰۰** آگهی، **۲** درخواست همزمان
- بدون Excel / CSV / JSON / دانلود عکس
- آپلود مستقیم به Workspace روی سرور

## مستندات

| سند | توضیح |
|-----|--------|
| [ROADMAP.md](./ROADMAP.md) | نقشه راه |
| [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) | معماری فنی |
| [docs/MOBILE_API_SPEC.md](./docs/MOBILE_API_SPEC.md) | قرارداد REST API |
| [docs/ECOSYSTEM_ROLES.md](./docs/ECOSYSTEM_ROLES.md) | نقش ویندوز / اندروید / سرور |
| [docs/reference/](./docs/reference/) | مرجع HTTP دیوار (کپی از پروژهٔ ویندوز) |

## استک

Kotlin · Jetpack Compose · Hilt · Retrofit · Room (cache) · WorkManager

---

**دامنه API:** `https://divarfiling.ir/api/mobile/v1/`

## انتشار روی GitHub

این پوشه یک ریپوی **مستقل** است (`git init` از قبل انجام شده). مراحل:

```bash
cd divar-mobile-app
git add .
git commit -m "Initial commit: Divar Filing Android companion app"
git remote add origin https://github.com/YOUR_USER/divar-mobile-app.git
git push -u origin main
```

ریپوی Django/backend جداگانه است — فقط `docs/MOBILE_API_SPEC.md` قرارداد API را اینجا نگه می‌دارد.
