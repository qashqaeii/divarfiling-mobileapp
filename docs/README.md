# فهرست مستندات — Divar Filing Mobile v2

| سند | مخاطب | توضیح |
|-----|--------|--------|
| [../ROADMAP.md](../ROADMAP.md) | همه | **شروع از اینجا** — فازها و اولویت CRM |
| [ECOSYSTEM_ROLES.md](./ECOSYSTEM_ROLES.md) | Product | تفکیک سرور / ویندوز / اندروید |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Android dev | Thin client، Room=cache |
| [MOBILE_API_SPEC.md](./MOBILE_API_SPEC.md) | Backend + Android | REST کامل |
| [NOTIFICATIONS.md](./NOTIFICATIONS.md) | همه | FCM و رویدادها |
| [reference/](./reference/) | Android dev | مرجع HTTP دیوار (کپی از ویندوز) |

## اصل معماری v2

```
Android → REST API → Django → PostgreSQL → Workspace / CRM / Notifications
```

- **ویندوز** = استخراج حرفه‌ای (جدا می‌ماند)
- **اندروید** = CRM + Push + فایلینگ + استخراج سبک (≤۱۰۰)
- **Room** = فقط cache — نه منبع حقیقت

## ترتیب مطالعه

1. ECOSYSTEM_ROLES — مرز مسئولیت‌ها
2. ROADMAP — برنامه اجرا
3. MOBILE_API_SPEC — قرارداد قبل از کدنویسی
4. ARCHITECTURE — ساختار ماژول اندروید
5. NOTIFICATIONS — فاز Push

## حذف شده (v1)

- `FEATURE_PARITY.md` — مبتنی بر «جایگزینی ویندوز» بود؛ با `ECOSYSTEM_ROLES.md` جایگزین شد.
