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

```bash
cd android
./gradlew assembleDebug
```

خروجی: `app/build/outputs/apk/debug/app-debug.apk`

نیازمند: JDK 17، Android SDK (API 34)

## Build APK with GitHub Actions

با هر `push` یا `pull_request` روی branch اصلی (`main` / `master`) — در صورت تغییر فایل‌های داخل `android/` — workflow **Build APK** اجرا می‌شود.

همچنین می‌توانید دستی اجرا کنید: **Actions → Build APK → Run workflow**

### دانلود APK

1. GitHub → **Actions**
2. workflow **Build APK**
3. آخرین **Run** موفق
4. بخش **Artifacts** → `divar-filing-debug-apk`

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
