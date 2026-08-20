# فهرست مستندات — Divar Filing Mobile v3

| سند | مخاطب | توضیح |
|-----|--------|--------|
| [../ROADMAP.md](../ROADMAP.md) | همه | **شروع از اینجا** — میزکار اصلی موبایل |
| [ECOSYSTEM_ROLES.md](./ECOSYSTEM_ROLES.md) | Product | نقش اندروید / وب / ویندوز / سرور (v3) |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Android dev | Thin client، Room=cache |
| [MOBILE_API_SPEC.md](./MOBILE_API_SPEC.md) | Backend + Android | REST کامل (+ insights/map/cloud/AI) |
| [NOTIFICATIONS.md](./NOTIFICATIONS.md) | همه | FCM و رویدادها |
| [INSTALL_GUIDE_FA.md](./INSTALL_GUIDE_FA.md) | انتشار | نصب و Play Protect |
| [IN_APP_UPDATE.md](./IN_APP_UPDATE.md) | انتشار | آپدیت داخلی APK |
| [reference/](./reference/) | Android dev | مرجع HTTP دیوار |

## اصل معماری v3

```
Android → REST API → Django → PostgreSQL → Workspace / CRM / Cloud Extract / AI
```

- **اندروید** = میزکار روزانه کامل (CRM + فایلینگ + تحلیل + نقشه + استخراج سبک/ابری)
- **وب** = فروشگاه / پرداخت / گزارش حجیم
- **ویندوز** = اختیاری برای قدرت‌کارها
- **Room** = فقط cache — نه منبع حقیقت

## ترتیب مطالعه

1. ECOSYSTEM_ROLES — مرز مسئولیت‌ها (v3)
2. ROADMAP — برنامه اجرا
3. MOBILE_API_SPEC — قرارداد API
4. INSTALL_GUIDE_FA / IN_APP_UPDATE — قبل از انتشار
5. ARCHITECTURE / NOTIFICATIONS
