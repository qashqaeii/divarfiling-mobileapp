# قرارداد REST API — اپ اندروید

**نسخه:** 2.0  
**Base URL:** `https://divarfiling.ir/api/mobile/v1`  
**اصل:** تمام منطق کسب‌وکار روی سرور — اپ فقط consumer

> پیاده‌سازی در ریپوی backend Django — اپ `mobile_api`

---

## قرارداد عمومی

### هدرها

```http
Authorization: Bearer {access_token}
Content-Type: application/json
Accept: application/json
X-App-Version: 1.0.0
X-Device-Id: {sha256}
X-Platform: android
```

### پاسخ استاندارد

```json
{
  "ok": true,
  "data": { },
  "meta": { "page": 1, "total": 120 }
}
```

### خطا

```json
{
  "ok": false,
  "error": "پیام فارسی",
  "code": "VALIDATION_ERROR",
  "fields": { "phone": ["شماره نامعتبر"] }
}
```

| HTTP | code نمونه |
|------|------------|
| 400 | `VALIDATION_ERROR` |
| 401 | `AUTH_EXPIRED` |
| 403 | `LICENSE_REQUIRED`, `FORBIDDEN` |
| 404 | `NOT_FOUND` |
| 409 | `SYNC_CONFLICT` |
| 429 | `RATE_LIMIT` |

### Pagination

```
?page=1&page_size=20
```

---

## ۱. Auth — احراز هویت

### ورود

```http
POST /auth/login
```

```json
{ "username": "09121234567", "password": "***" }
```

```json
{
  "ok": true,
  "data": {
    "access": "eyJ...",
    "refresh": "eyJ...",
    "expires_in": 900,
    "user": {
      "id": 42,
      "full_name": "علی محمدی",
      "phone": "09121234567",
      "agency_name": "آژانس نمونه"
    }
  }
}
```

### تازه‌سازی

```http
POST /auth/refresh
{ "refresh": "eyJ..." }
```

### خروج

```http
POST /auth/logout
{ "refresh": "eyJ..." }
```

---

## ۲. Device — ثبت دستگاه

### ثبت / به‌روزرسانی

```http
POST /devices/register
```

```json
{
  "device_id": "a1b2c3...",
  "device_model": "Samsung A54",
  "os_version": "14",
  "app_version": "1.0.0",
  "fcm_token": "firebase_token..."
}
```

```json
{
  "ok": true,
  "data": {
    "device_id": "a1b2c3...",
    "registered_at": "2026-06-25T10:00:00Z",
    "license": {
      "valid": true,
      "plan": "professional",
      "mobile_extract_enabled": true,
      "expires_at": "2027-01-01T00:00:00Z"
    }
  }
}
```

### به‌روزرسانی FCM

```http
PATCH /devices/me
{ "fcm_token": "new_token..." }
```

---

## ۳. Settings — تنظیمات

### پروفایل

```http
GET /settings/profile
PATCH /settings/profile
```

```json
{ "full_name": "...", "phone": "...", "timezone": "Asia/Tehran" }
```

### ترجیحات اعلان

```http
GET /settings/notifications
PATCH /settings/notifications
```

```json
{
  "crm_reminders": true,
  "today_digest": true,
  "new_dataset": true,
  "price_drop": true,
  "customer_match": true,
  "extract_complete": true,
  "overdue_followup": true,
  "digest_hour": 8
}
```

---

## ۴. Sync — همگام‌سازی

### Pull delta

```http
GET /sync?since=2026-06-01T10:00:00Z&entities=contacts,deals,properties,reminders,activities
```

```json
{
  "ok": true,
  "data": {
    "server_time": "2026-06-25T12:00:00Z",
    "contacts": { "upserted": [...], "deleted_ids": [99] },
    "deals": { "upserted": [...], "deleted_ids": [] },
    "properties": { "upserted": [...], "deleted_ids": [] },
    "reminders": { "upserted": [...], "deleted_ids": [] },
    "activities": { "upserted": [...], "deleted_ids": [] }
  }
}
```

### Push offline changes

```http
POST /sync/push
```

```json
{
  "operations": [
    {
      "op_id": "uuid-local-1",
      "entity": "contact",
      "action": "create",
      "client_updated_at": "2026-06-25T11:00:00Z",
      "payload": { "full_name": "...", "phone": "0912..." }
    }
  ]
}
```

```json
{
  "ok": true,
  "data": {
    "mapped": [{ "op_id": "uuid-local-1", "server_id": 102 }],
    "conflicts": []
  }
}
```

---

## ۵. CRM — مخاطبین (Contacts)

```http
GET    /crm/contacts?q=&status=&type=&page=1
GET    /crm/contacts/{id}
POST   /crm/contacts
PATCH  /crm/contacts/{id}
DELETE /crm/contacts/{id}
POST   /crm/contacts/{id}/status     { "status": "در حال پیگیری" }
POST   /crm/contacts/quick-lead      { "full_name", "phone", "source" }
POST   /crm/contacts/merge           { "primary_id", "duplicate_id" }
GET    /crm/contacts/{id}/matches    # فایل‌های پیشنهادی (سرور محاسبه)
```

### Contact object

```json
{
  "id": 101,
  "full_name": "رضا احمدی",
  "phone": "09121111111",
  "customer_type": "خریدار",
  "status": "در حال پیگیری",
  "source": "دیوار",
  "priority": "بالا",
  "budget": 5000000000,
  "notes": "",
  "address": "تهران، ونک",
  "latitude": 35.75,
  "longitude": 51.41,
  "next_follow_up_at": "2026-06-26T10:00:00Z",
  "updated_at": "2026-06-25T11:30:00Z"
}
```

---

## ۶. CRM — معاملات (Deals)

```http
GET    /crm/deals?stage=&page=1
GET    /crm/deals/{id}
POST   /crm/deals
PATCH  /crm/deals/{id}
POST   /crm/deals/{id}/stage       { "stage": "negotiation" }
GET    /crm/deals/pipeline
GET    /crm/deals/stages             # مراحل سفارشی کاربر
GET    /crm/deals/{id}/checklist
POST   /crm/deals/{id}/checklist/{item_id}/toggle
```

---

## ۷. CRM — املاک (Properties)

```http
GET    /crm/properties
GET    /crm/properties/{id}
POST   /crm/properties
PATCH  /crm/properties/{id}
DELETE /crm/properties/{id}
POST   /crm/properties/{id}/status   { "transaction_status": "در مذاکره" }
POST   /crm/properties/{id}/contacts  { "contact_id", "role" }
```

---

## ۸. CRM — فعالیت و یادداشت

```http
GET    /crm/contacts/{id}/activities
POST   /crm/contacts/{id}/activities
```

```json
{ "activity_type": "تماس", "note": "توافق بازدید فردا", "occurred_at": "..." }
```

```http
GET    /crm/contacts/{id}/notes
POST   /crm/contacts/{id}/notes      { "body": "..." }
```

---

## ۹. CRM — یادآور (Reminders)

```http
GET    /crm/reminders?due_from=&due_to=&done=false
POST   /crm/reminders
PATCH  /crm/reminders/{id}
POST   /crm/reminders/{id}/complete
DELETE /crm/reminders/{id}
```

```json
{
  "id": 55,
  "title": "تماس با رضا احمدی",
  "contact_id": 101,
  "due_at": "2026-06-26T09:00:00Z",
  "done": false,
  "reminder_type": "call"
}
```

---

## ۱۰. CRM — امروز (Today)

```http
GET /crm/today
```

```json
{
  "ok": true,
  "data": {
    "date": "2026-06-25",
    "overdue": [{ "type": "follow_up", "contact": {...} }],
    "today": [{ "type": "reminder", "reminder": {...} }],
    "visits": [{ "type": "visit", "deal_id": 12 }],
    "stats": { "total": 8, "done": 3 }
  }
}
```

---

## ۱۱. CRM — پیوند آگهی

```http
POST /crm/contacts/{id}/listings
{ "listing_token": "abc123", "role": "پیشنهادی", "note": "" }

DELETE /crm/contacts/{id}/listings/{token}

GET /crm/contacts/{id}/listings
```

---

## ۱۲. Filing — فایلینگ (Workspace)

### مجموعه‌ها (Dataset)

```http
GET /filing/datasets?page=1&source=windows|mobile|all
GET /filing/datasets/{id}
```

```json
{
  "id": 456,
  "name": "ونک فروش ۱۴۰۴/۰۴/۰۵",
  "source": "windows",
  "transaction_type": "فروش مسکونی",
  "city": "تهران",
  "district": "ونک",
  "item_count": 287,
  "created_at": "2026-06-20T08:00:00Z"
}
```

### آگهی‌ها

```http
GET /filing/datasets/{id}/listings?q=&price_min=&price_max=&area_min=&rooms=&page=1
GET /filing/listings/{token}
GET /filing/search?q=ونک+سه+خوابه&dataset_id=456
```

### Listing object (خلاصه)

```json
{
  "token": "abc123",
  "title": "آپارتمان ۱۲۰ متری",
  "price": 15000000000,
  "area": 120,
  "rooms": 3,
  "district": "ونک",
  "latitude": 35.75,
  "longitude": 51.41,
  "thumbnail_url": "https://...",
  "share_link": "https://divar.ir/v/...",
  "advertiser_type": "مشاور",
  "scraped_at": "2026-06-20T08:00:00Z"
}
```

---

## ۱۳. Map — نقشه

```http
GET /map/listings?dataset_id=456&north=&south=&east=&west=
GET /map/contacts?north=&south=&east=&west=
GET /map/nearby?lat=35.75&lng=51.41&radius_km=2&dataset_id=456
```

```json
{
  "ok": true,
  "data": {
    "markers": [
      { "type": "listing", "token": "abc", "lat": 35.75, "lng": 51.41, "label": "۱۲م" },
      { "type": "contact", "id": 101, "lat": 35.76, "lng": 51.42, "label": "رضا" }
    ]
  }
}
```

---

## ۱۴. Extraction — استخراج سبک

### آپلود نتیجه (مسیر اصلی)

```http
POST /extractions/upload
Content-Type: application/json
```

```json
{
  "filters": {
    "city_id": "1",
    "city_name": "تهران",
    "district_ids": ["123"],
    "district_names": ["استاد معین"],
    "category": "apartment-rent",
    "category_label": "آپارتمان",
    "transaction_type_label": "فروش مسکونی",
    "output_name_hint": "ostad-moein_20260626_115900",
    "advertiser_filter": "all",
    "source_client": "android_light",
    "sort": "sort_date",
    "max_items": 50
  },
  "started_at": "2026-06-25T10:00:00Z",
  "finished_at": "2026-06-25T10:04:00Z",
  "items": [
    { "token": "...", "raw": { } },
    { "token": "...", "raw": { } }
  ]
}
```

**اعتبارسنجی سرور:**

- `items.length <= 100` — در غیر این صورت `400`
- ingest → `Dataset` + `Listing` records
- trigger `notification: extract_complete` + `new_dataset`

```json
{
  "ok": true,
  "data": {
    "dataset_id": 789,
    "ingested_count": 50,
    "skipped_count": 2,
    "workspace_url": "/workspace/datasets/789/"
  }
}
```

### وضعیت (اختیاری — اگر upload چندمرحله‌ای شود)

```http
GET /extractions/limits
```

```json
{
  "max_items": 100,
  "max_concurrent_hint": 2,
  "extractions_today": 1,
  "extractions_daily_limit": 5
}
```

---

## ۱۵. Notifications — مدیریت اعلان

```http
GET  /notifications?page=1          # تاریخچه in-app
POST /notifications/{id}/read
GET  /notifications/unread-count
```

Push payload — [NOTIFICATIONS.md](./NOTIFICATIONS.md)

---

## ۱۶. License — لایسنس (موبایل)

```http
GET /license/status
```

```json
{
  "valid": true,
  "plan": "professional",
  "features": {
    "crm_mobile": true,
    "filing_view": true,
    "light_extract": true,
    "map": true,
    "push": true
  },
  "expires_at": "2027-01-01T00:00:00Z"
}
```

> لایسنس ویندوز و موبایل **جدا** نیستند — همان حساب کاربر؛ feature flag per plan.

---

## ۱۷. چک‌لیست پیاده‌سازی Django

### فاز Backend 1 (هفته ۱–۴)

- [ ] `mobile_api` app + JWT
- [ ] Auth + Device + FCM token
- [ ] CRM Contacts CRUD
- [ ] CRM Today
- [ ] Filing datasets list + listings read

### فاز Backend 2 (هفته ۵–۸)

- [ ] Deals + Properties + Reminders
- [ ] Sync delta + push
- [ ] Activities + notes
- [ ] Contact ↔ listing link

### فاز Backend 3 (هفته ۹–۱۲)

- [ ] `extraction_ingest` service (shared with Windows upload path)
- [ ] Map geo endpoints
- [ ] Price watch + match jobs → FCM
- [ ] Notification preferences + Celery tasks

---

## ۱۸. تفاوت با APIهای فعلی

| موجود | Mobile API v1 |
|--------|---------------|
| Session cookie (وب CRM) | JWT Bearer |
| `X-Bot-Api-Key` (ویندوز لایسنس) | JWT + device |
| CRM HTML views | REST JSON |
| Windows: فایل Excel upload | Mobile: JSON raw upload |
| بدون FCM | Device register + push |

---

*نسخه ۲.۰ — بدون endpoint export فایل در موبایل*
