# Mobile Daily Workspace — پلن رسمی محصول

> هدف اندروید: مشاور بتواند **۸۰٪ کار روزانه** را بدون لپ‌تاپ، در کمتر از **۳۰ ثانیه** انجام دهد.  
> کپی کامل وب هدف نیست. گزارش‌های سنگین، آژانس کامل، webhook، Audit، Import انبوه → وب.

منبع اولویت‌بندی: تصمیم محصولی تیر ۱۴۰۵ (اصلاح تحلیل شکاف).

---

## فاز A — ترتیب اجرا

1. **تقویم و یادآور کامل** — روز/هفته/ماه، CRUD، recurrence، لینک موجودیت، Snooze، انجام، ساخت از جزئیات مخاطب/ملک  
2. **تیکت پشتیبانی کامل** — Thread، پاسخ، فایل، وضعیت، Close/Reopen، Push  
3. **فیلتر حرفه‌ای فایلینگ** — Bottom Sheet مرحله‌ای (محله، قیمت، متراژ، خواب، سال، امکانات، نوع آگهی‌دهنده، Value، جدید/بدون‌تکرار، Sort)  
4. **Saved Filters** — فقط **فایلینگ + مخاطبین** (نه ملک شخصی)؛ Chip، Pin، Badge `+N جدید`  
5. **AI کاربردی** — سه Action (خلاصه آگهی / متن معرفی ملک / پیام پیگیری مخاطب) + سهمیه؛ بدون چت عمومی  

### اسپرینت‌ها

| Sprint | تحویل | وضعیت |
|--------|--------|--------|
| 1 | Ticket Thread · Reminder CRUD/recurrence · Calendar · Push deep-link | ✅ انجام‌شده |
| 2 | فیلتر عمیق فایلینگ · Saved Filters · Chip/Badge · Sort/Reset | ✅ انجام‌شده |
| 3 | AI quota · خلاصه آگهی/ملک · پیش‌نویس مخاطب · copy/regenerate/tone | ✅ انجام‌شده |
| 4 | قالب integration · Global Search · CRM/support/settings polish | ✅ انجام‌شده |

---

## فاز B / C / D (خلاصه)

- **B:** قالب CRUD + متغیر · بازدید ساخت‌یافته · Global Search · Merge سبک · تصاویر/نقشه ملک · Saved Filters ملک شخصی  
- **C:** پاکسازی دیتاست با پیش‌نمایش · Smart Tools کارت‌محور · زمان‌بندی استخراج کاربرپسند + Push  
- **D تیم MVP:** Inbox · Assign · انتقال · اعلان · عملکرد امروز من · اعلامیه — بقیه وب  

## اجرای انجام‌شده تا اینجا

- Reminder lifecycle در `Calendar` و `Contact Detail` تا سطح create/edit/delete/snooze/complete بالا رفت.
- Support ticket flow برای `Thread`, `reply attachment`, `close/reopen`, `category/priority`, filter list و metadata detail کامل‌تر شد.
- AI از placeholder به flow واقعی `quota + draft + summarize` تبدیل شد.
- Saved Filters علاوه بر فایلینگ/مخاطبین برای `Properties` و `Deals` هم پیاده‌سازی شد.
- Share/Public Share/Social Sharing در `Listing` و `Property` کامل‌تر شد و Telegram/SMS هم اضافه شد.
- Templateها از حالت copy-only خارج شدند و داخل flowهای واقعی CRM مصرف می‌شوند.
- Team از redirect خام به native bridge معنادار رسید.
- Install / Update help به‌صورت native screen وارد `Settings` و `More` شد.
- Owner-as-contact برای `مالکین` از داخل `CRM hub` به یک ورودی نیتیو و server-backed تبدیل شد.
- `Global Filing Search` از `More` هم discoverable شد و فقط داخل shell فایلینگ نماند.
- `Save as Personal Property` بعد از create موفق، مستقیم کاربر را به `Property Detail` همان فایل تازه‌ساخته‌شده می‌برد.
- Sprint 7 (gap-audit): AI Workspace با `quota/enabled`, `tone`, `fallback labeling`, `regenerate/copy` و entry point از `Contact Detail` / `Listing Detail` production-ready شد.
- Sprint 8 (gap-audit): Team native module برای overview، messages/thread، members، announcements، lead inbox، unread badge و assign/transfer مخاطب اضافه شد.

## ساخته نمی‌شود (فعلاً)

Product Hub کامل · تیم کامل · Compare/Heatmap · Import Excel پیچیده · داشبورد آماری بزرگ · Workspace settings کامل · اتوماسیون پیشرفته · Academy کامل · چت عمومی AI

## موفقیت فاز A

| شاخص | هدف |
|------|-----|
| ساخت یادآور هفتگی | > ۳۰٪ کاربران اندروید |
| Saved Filter / هفته / فعال | > ۲ |
| فیلتر حرفه‌ای فایلینگ (لایسنس) | > ۴۰٪ |
| استفاده AI | > ۲۰٪ فعال |
| پیگیری تیکت داخل اپ | > ۷۰٪ |
| رشد بازگشت هفتگی | ≥ ۱۵٪ |

Canvas زنده: `canvases/android-django-gap-plan.canvas.tsx`
