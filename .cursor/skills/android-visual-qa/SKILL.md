---
name: android-visual-qa
description: Perform lightweight but rigorous visual regression, Compose preview and Android lint/unit/UI testing on Divar Filing, including RTL/dark mode/device widths and workflow-based acceptance.
---


# Android Visual QA & Regression Gate

## اصل
QA هدفمند، نه audit بی‌پایان. بررسی کیفیت خروجی همان slice تغییر کرده با شواهد واقعی.

## راه‌اندازی فعلی
- Android root `android/`، Kotlin/Compose/Gradle، JDK17، SDK API35.
- کد فعلی ۳۱ Unit Test در `android/app/src/test/...` دارد؛ در snapshot `androidTest` source folder پیدا نشد.
- `.github/workflows/build-apk.yml`: برای PR `lintDebug` و push به main `lintDirectRelease` + signed APK.
- موفقیت build ⇏ تأیید UX. اگر دستگاه/emulator نیست فقط static/preview/test evidence گزارش کن.

## Quality Gate ثابت
| محور | معیار پذیرش |
| --- | --- |
| Product | رفتار اصلی و ارزش صفحه حفظ شده |
| UX | اقدام اصلی پیدا و اجرا می‌شود؛ stateهای ضروری |
| Visual | hierarchy واضح، spacing و typography یکدست |
| RTL & Persian | تاریخ، قیمت، phone، mixed LTR صحیح |
| A11y | touch targets، screen reader، contrast، font scaling |
| Performance | scroll بدون jank شدید، lazy composition و state handling |
| Stability | navigation، CRUD، deep links، back/IME بدون رگرسیون |

## Gate سریع (به‌ترتیب)
1. `git diff --check`; فقط scope فعلی را review کن.
2. `cd android` و در محیط دارای SDK/JDK `./gradlew :app:compileDebugKotlin` یا `gradlew.bat :app:compileDebugKotlin`؛ متناسب با پلتفرم.
3. `./gradlew :app:testDebugUnitTest` برای unitهای مرتبط؛ در صورت تغییر shared اساسی `lintDebug` نیز.
4. Compose Previews با فارسی واقعی، تم روشن/تاریک؛ در IDE نمایش/اسکرین‌شات اگر ابزار واقعی موجود باشد.
5. UI smoke/Manual scenarios مربوط به همان صفحه؛ اگر test runner آماده نبود اضافه کردن تست instrumentation برای مسیرهای اولویت بالا را پیشنهاد یا به تدریج راه‌اندازی کن.
6. نتایج را صریح گزارش کن: PASS / FAIL / NOT RUN؛ با دلیل در NOT RUN.

## Snapshot scenarios
- 360dp/393dp/بزرگ، Dark/Light، فونت عادی/بزرگ؛ Keyboard باز، landscape در صورت اهمیت.
- ثبت/ویرایش/بازگشت، loading/error/empty, filter reset, visual selected nav, talkback labels.
- فیلد پرشده، عنوان فارسی ۸۰ کاراکتری، رقم‌ها و شماره‌های mixed-direction.
- اتصال اینترنت کند/قطع‌شده فقط در صورتی که امکان شبیه‌سازی موجود است.

## تست‌های مخصوص اپ
- تماس، باز کردن دیوار، پیوند مخاطب به ملک، ساخت یادآور، انتخاب دیتاست، ذخیره ملک، Contract PDF، push deep links، license gate، start extraction.
- به هیچ وجه واقعی بودن API، داشتن شماره مالک یا جواب AI را جعل نکن؛ از mocks فقط در test/preview استفاده کن.

## گزارش نهایی کم‌حجم
```text
Scope: ...
Changed: ...
UX fix: ...
Check: compile PASS/FAIL/NOT RUN; unit PASS/FAIL/NOT RUN; visual ...
Risks: ...
Next slice: ...
```

در صورت نیاز: `references/TEST_MATRIX.md`.
