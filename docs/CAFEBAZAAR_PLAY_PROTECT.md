# انتشار کافه‌بازار و رفع Play Protect

**Package:** `ir.divarfiling.mobile`  
**علت رد رایج:** `Play Protect doesn't recognize this app's developer` (دسته Uncommon)

## قوانین ساخت APK برای بازار

1. فقط `assembleRelease` با keystore رسمی (`ANDROID_KEYSTORE_*`)
2. هرگز APK دیباگ یا keystore موقت آپلود نکنید
3. همان certificate را برای همه نسخه‌های بعدی نگه دارید و بکاپ بگیرید
4. قبل از ارسال مجدد، `versionCode` را از نسخه ردشده بالاتر ببرید

## چک‌لیست ارسال مجدد

- [ ] APK = release امضاشده
- [ ] آپلود در [VirusTotal](https://www.virustotal.com) → کپی SHA-256 از Details
- [ ] ارسال [Play Protect Appeals](https://support.google.com/googleplay/android-developer/contact/protectappeals) با IP خارج از ایران
- [ ] ۷–۱۰ روز صبر و تست نصب با Play Protect روشن
- [ ] حساب demo در توضیح انتشار یا تیکت به `developers@cafebazaar.ir`
- [ ] لینک حریم خصوصی: `https://divarfiling.ir/privacy/`

## متن Appeal (انگلیسی)

```text
Hi,
This app (ir.divarfiling.mobile) is the official Android client for Divar Filing
(https://divarfiling.ir), a legitimate real-estate CRM and listing workspace for
licensed Iranian real-estate consultants.

Key features:
- Secure login to the user's existing Divar Filing account (JWT)
- CRM: contacts, deals, properties, reminders, and daily follow-ups
- View and search listing datasets synced from our servers
- Optional light/on-device listing fetch within license limits, uploaded only to
  the authenticated user's workspace on divarfiling.ir
- Push notifications for reminders and workspace events (Firebase Cloud Messaging)

Privacy & security:
- We do not request SMS, call log, contacts-book, or accessibility permissions
- User data is sent only to https://divarfiling.ir over HTTPS for the logged-in account
- No ads, no third-party APK installs, no hidden modules, no billing fraud

We comply with Google Play Protect policies. The warning appears because the app
is newly published outside Google Play and the signing certificate is not yet
widely recognized. Please review and clear the Uncommon/developer recognition flag.

Thank you,
Divar Filing
```

## قالب اطلاعات ورود ریویو کافه‌بازار

```text
حساب تست ریویو کافه‌بازار:
ایمیل/موبایل: [حساب demo]
رمز: [رمز موقت]
نکته: این حساب لایسنس فعال و داده نمونه CRM/فایلینگ دارد.
مسیر تست پیشنهادی: ورود → میزکار → امروز → مخاطبین → فایلینگ → ابزارها
لطفاً پس از ریویو رمز را عوض می‌کنیم.
```

## تست بعد از Appeal

1. Play Protect را روشن نگه دارید
2. APK همان SHA-256 را نصب کنید
3. اگر Blocked نیامد، نسخه را دوباره در پیشخان بازار در صف بگذارید
