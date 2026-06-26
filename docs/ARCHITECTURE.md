# معماری فنی — Android Companion

**نسخه:** 2.0  
**اصل:** Django منبع حقیقت — اندروید Thin Client

---

## ۱. جریان داده

```
┌──────────────────────────────────────────────────────────────┐
│                     Android Application                       │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐  ┌───────────────┐ │
│  │   UI    │→ │ViewModel│→ │Repository│→ │ Remote (API)  │ │
│  └─────────┘  └─────────┘  └────┬─────┘  └───────┬───────┘ │
│                                   │                │         │
│                            ┌──────▼─────┐   ┌──────▼──────┐  │
│                            │ Room Cache │   │ Divar HTTP  │  │
│                            │  (موقت)   │   │ (extract    │  │
│                            └────────────┘   │  light only)│  │
│                                             └─────────────┘  │
└──────────────────────────────────────────────────────────────┘
                                    │
                          HTTPS REST │ (اصلی)
                                    ▼
┌──────────────────────────────────────────────────────────────┐
│                      Django Application                       │
│  mobile_api │ workspace │ crm │ notifications │ extraction   │
└──────────────────────────────┬───────────────────────────────┘
                               ▼
                        PostgreSQL
```

---

## ۲. قوانین معماری

| # | قانون |
|---|--------|
| 1 | هر **write** CRM از API سرور — نه Room |
| 2 | Room فقط **cache** + **offline queue** |
| 3 | منطق flatten آگهی، تطبیق، price watch روی **سرور** |
| 4 | استخراج سبک: Android فقط JSON خام جمع می‌کند و upload می‌کند |
| 5 | بدون export فایل Excel/CSV/JSON در اندروید |
| 6 | بدون وابستگی runtime به پروژهٔ ویندوز — مرجع HTTP فقط در `docs/reference/` |

---

## ۳. ماژول‌های Gradle

```
android/
├── app/
├── core/
│   ├── network/       # Retrofit, AuthInterceptor, ErrorMapper
│   ├── database/      # Room — CacheDao, SyncQueueDao
│   ├── datastore/     # tokens, prefs
│   └── design/        # Theme, components
├── feature-auth/
├── feature-crm/       # ★ largest module
├── feature-filing/
├── feature-map/
├── feature-extract-light/
├── feature-notifications/
└── feature-settings/
```

### وابستگی

```
app → all features → core
feature-crm → feature-filing (link listing to contact)
feature-map → feature-filing, feature-crm
feature-extract-light → feature-filing (navigate to result)
```

---

## ۴. لایه Repository

```kotlin
class ContactRepository @Inject constructor(
    private val api: MobileApi,
    private val cache: ContactCacheDao,
    private val syncQueue: SyncQueueDao,
    private val connectivity: ConnectivityMonitor,
) {
    fun contactsFlow(): Flow<List<ContactUi>> = flow {
        cache.getAll().let { if (it.isNotEmpty()) emit(it.map { c -> c.toUi() }) }
        if (connectivity.isOnline) {
            val remote = api.getContacts()
            cache.replaceAll(remote)
            emit(remote)
        }
    }

    suspend fun updateContact(id: Long, body: ContactUpdateBody) {
        if (connectivity.isOnline) {
            api.updateContact(id, body)
            cache.upsert(api.getContact(id))
        } else {
            syncQueue.enqueue(SyncOp.UpdateContact(id, body))
            cache.patchOptimistic(id, body)
        }
    }
}
```

---

## ۵. Room — فقط Cache

### Entities

| Entity | TTL پیش‌فرض | توضیح |
|--------|-------------|--------|
| `CachedContact` | sync | mirror API |
| `CachedDeal` | sync | |
| `CachedProperty` | sync | |
| `CachedDatasetSummary` | ۱h | لیست filing |
| `CachedListing` | ۲۴h | جزئیات on-demand |
| `SyncQueueItem` | تا ارسال | offline writes |
| `CachedTodayTask` | ۱۵min | |

### مایگریشن

Room version در سرور schema نسخه‌گذاری نمی‌شود — فقط cache است؛ در صورت تغییر API، `clearAll()` + full sync.

---

## ۶. استخراج سبک — `feature-extract-light`

### محدودیت در کد

```kotlin
object ExtractLightLimits {
    const val MAX_ITEMS = 100
    const val MAX_CONCURRENT = 2
    const val MIN_DELAY_MS = 1000L
}
```

### مسئولیت Android

1. UI فیلتر
2. `DivarLightClient` — search + detail (OkHttp)
3. جمع‌آوری `List<RawPostJson>`
4. `POST /api/mobile/v1/extractions/upload`
5. نمایش progress — **بدون** ذخیره دائمی لیست کامل در Room

### مسئولیت Django (`extraction_ingest`)

1. اعتبارسنجی سقف ۱۰۰
2. `flatten_post_detail` (منطق مشابه ویندوز — **یک‌بار روی سرور**)
3. ساخت/به‌روزرسانی `Dataset` + `Listing`
4. trigger اعلان «فایل جدید»
5. اجرای price watch / matching در صورت نیاز

---

## ۷. CRM — Thin Client

Android **پیاده نمی‌کند:**

- قوانین اتوماسیون CRM
- merge مخاطب تکراری (UI درخواست می‌دهد، سرور اجرا می‌کند)
- محاسبه تطبیق امتیازدار
- تقسیم کمیسیون معامله

Android **پیاده می‌کند:**

- فرم‌ها و validation سطح UI (تلفن ایران)
- نمایش pipeline از stages سرور
- Intent واتساپ / تماس
- optimistic UI + sync queue

---

## ۸. فایلینگ — Read Model

```kotlin
interface FilingApi {
    @GET("filing/datasets")
    suspend fun datasets(@Query("page") page: Int): PagedDatasets

    @GET("filing/datasets/{id}/listings")
    suspend fun listings(
        @Path("id") datasetId: Long,
        @QueryMap filters: Map<String, String>,
    ): PagedListings

    @GET("filing/listings/{token}")
    suspend fun listingDetail(@Path("token") token: String): ListingDetail
}
```

همه query/filter روی **سرور** — اپ pagination و cache.

---

## ۹. نقشه — `feature-map`

| API | کاربرد |
|-----|--------|
| `GET /map/listings?dataset_id=&bounds=` | marker آگهی |
| `GET /map/contacts?bounds=` | marker مشتری |
| `GET /map/nearby?lat=&lng=&radius=` | فایل اطراف |

مسیریابی: `geo:` intent — بدون منطق routing در اپ.

---

## ۱۰. احراز هویت

```
Login → access (۱۵min) + refresh (۳۰d)
       → register device + fcm_token
       → Authorization: Bearer {access}
```

Encrypted DataStore برای refresh token.

---

## ۱۱. Sync Protocol

### Delta pull

```
GET /sync?since={iso8601}&entities=contacts,deals,properties,reminders,activities
```

### Push queue (offline)

```
POST /sync/push { operations: [...] }
```

سرور: `updated_at` + conflict → server-wins برای فیلدهای حساس.

---

## ۱۲. امنیت

| مورد | روش |
|------|-----|
| JWT | Encrypted storage |
| Divar calls | بدون credential کاربر دیوار |
| SSL | certificate pinning اختیاری |
| PII cache | Room بدون encrypt در v1؛ SQLCipher در v1.1 |

---

## ۱۳. Navigation & Deep Links

```
divarfiling://home
divarfiling://crm/contacts/{id}
divarfiling://crm/deals/{id}
divarfiling://crm/today
divarfiling://filing/datasets/{id}
divarfiling://filing/listings/{token}
divarfiling://extract
divarfiling://settings/notifications
```

---

## ۱۴. تست

| لایه | ابزار |
|------|-------|
| Repository | MockWebServer + fake Room |
| ViewModel | Turbine + coroutines test |
| UI | Compose UI Test — CRM flows |
| Contract | Pact یا fixture JSON از `MOBILE_API_SPEC` |

---

## ۱۵. CI — GitHub Actions

Workflow: `.github/workflows/build-apk.yml`

- Trigger: `push` / `pull_request` روی `main`/`master` + `workflow_dispatch`
- مسیر کار: `android/`
- مراحل: `lintDebug` → `assembleDebug` → artifact `divar-filing-debug-apk`

جزئیات دانلود APK در [README.md](../README.md#build-apk-with-github-actions).

---

*مرجع اکوسیستم:* [ECOSYSTEM_ROLES.md](./ECOSYSTEM_ROLES.md)
