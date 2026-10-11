# بستهٔ اسکیل‌های تخصصی Android UI/UX — فایلینگ دیوار

این بسته برای **مخزن لوکال** `qashqaeii/divarfiling-mobileapp` ساخته شده و هیچ کدی از پروژه را خودکار تغییر نمی‌دهد.

## نصب روی ویندوز — دستی و آسان
1. فایل ZIP را دانلود کن.
2. در File Explorer وارد ریشهٔ ریپوی `divarfiling-mobileapp` شو (همان پوشه‌ای که `android/`, `docs/`, `README.md` دارد).
3. ZIP را در **همین پوشه** Extract کن (Merge with existing folders). مراقب باش یک پوشهٔ اضافی `divarfiling_android_skills_pack` داخل پروژه ایجاد نشود؛ باید `.cursor/` مستقیم در ریشهٔ مخزن باشد.
4. در File Explorer گزینه **View > Show > Hidden items** را فعال کن تا پوشهٔ `.cursor` دیده شود.
5. پروژه را در Cursor باز/Refresh کن و از **Customize > Skills** مشاهده کن؛ یا در Agent با `/android-ui-ux-master` یا `@android-ui-ux-master` فراخوانی کن.
6. محتوای `CURSOR_START_PROMPT_FA.txt` را به Cursor Agent بده تا اول اسکیل‌ها را فعال و بعد مرحله ۰/۱ بازطراحی را اجرا کند.

**VPN ویندوز:** برای استفاده محلی از اسکیل‌ها نیازی به VPN نیست؛ برای Cursor/GitHub/دانلود وابستگی‌های Gradle، وضعیت اتصال بسته به شبکهٔ فعلی شماست. اگر اتصال برقرار نشد VPN را طبق تنظیمات خود بررسی کن.

## نصب از PowerShell (اختیاری)
```powershell
$project = 'D:\PATH\TO\divarfiling-mobileapp' # مسیر واقعی مخزن را جایگزین کن
$zip = "$env:USERPROFILE\Downloads\divarfiling-android-uiux-skills.zip"
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Get-ChildItem -Force "$project\.cursor\skills"
```

`-Force` اگر فایل همنامی قبلاً ساخته بودی می‌تواند overwrite کند؛ ابتدا از customizationهای خودت نسخه پشتیبان بگیر. بسته فقط مسیرهای جدید مشخص‌شده در فهرست زیر را دارد و User Rules موجود را حذف نمی‌کند.

## ساختار محتویات
```text
.cursor/
  skills/
    android-ui-ux-master/SKILL.md
    android-ui-ux-master/references/SCREEN_AUDIT.md
    android-compose-design-system/SKILL.md
    android-compose-design-system/references/COMPONENT_CONTRACTS.md
    android-navigation-ux/SKILL.md
    android-rtl-persian/SKILL.md
    android-crm-workflows/SKILL.md
    android-crm-workflows/references/CRM_SCENARIOS.md
    android-adaptive-accessibility/SKILL.md
    android-motion-states/SKILL.md
    android-visual-qa/SKILL.md
    android-visual-qa/references/TEST_MATRIX.md
  rules/
    divarfiling-android-ui.mdc
CURSOR_START_PROMPT_FA.md
CURSOR_START_PROMPT_FA.txt
CURSOR_NEXT_PAGE_PROMPT_FA.txt
README_INSTALL_FA.md
```

## مبنای مهندسی
- Snapshot ریپو: `main` در SHA `9634aa62585ba1637c1deba89ed67b456983d79d` — تاریخ ۲۹ سپتامبر ۲۰۲۶؛ `versionName=2.7.2`.
- Kotlin/Compose/Material3، `core/design`، پنج تب اصلی؛ هدف **بهبود UI بدون بازنویسی API و state**.
- نخستین scope: foundation + `feature/home` + Bottom Navigation. بقیه با پرامپت ادامه slice به slice.
- این فایل‌ها «دستورالعمل‌های Cursor Agent» هستند نه plugin اجرایی Android Studio؛ وجود اسکیل به معنی تغییر خودکار کد نیست.

## منابع فرمت Cursor Skills
- https://cursor.com/docs/skills
- https://prod.cursor.com/help/customization/skills

## عیب‌یابی
- اگر Cursor اسکیل را ندید، مسیر `ROOT/.cursor/skills/<skill>/SKILL.md`، نام دقیق فایل و YAML header را بررسی کن؛ پنجره Cursor را Refresh کن.
- از root workspace درست مطمئن شو؛ بازکردن فقط پوشهٔ `android/` در بعضی setupها باعث می‌شود rule/skillهای root یافت نشوند.
- اگر با تغییر طرح UI تست نداریم، نتیجه را صریح NOT RUN بنویس؛ ادعای «بدون باگ» نکن.
