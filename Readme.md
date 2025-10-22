# 🐦 Finch Backend

**Finch**, sosyal medya tarzında içerik paylaşımı, takip etme, beğeni, yeniden paylaşım (repost) ve etkileşim özellikleri sunan bir backend uygulamasıdır.
Proje, **Spring Boot (Java 21)** tabanlıdır ve modüler, güvenlik ve ölçeklenebilirlik odaklı bir mimariyle inşa edilmiştir.

---

## 🚀 Teknolojiler ve Araçlar

| Katman                   | Teknoloji / Araç            |
| ------------------------ |-----------------------------|
| **Dil & Framework**      | Java 21, Spring Boot 3      |
| **Veritabanı**           | MySQL                       |
| **ORM**                  | Spring Data JPA (Hibernate) |
| **Güvenlik**             | Spring Security, JWT        |
| **Depolama**             | ImageKit SDK                |
| **E-posta**              | Spring Mail (SMTP / Gmail)  |
| **Zamanlanmış Görevler** | Spring Scheduler            |
| **Validasyon**           | Jakarta Validation          |
| **Logging**              | Logback (Console + File)    |
| **Bağımlılık**           | Maven                       |

---

## 📂 Proje Yapısı

```
src/
 └── main/
     ├── java/com/rjhtctn/finch_backend/
     │    ├── controller/     → REST API endpoint'leri
     │    ├── dto/            → Veri transfer objeleri (DTO)
     │    ├── exception/      → Özel hata sınıfları
     │    ├── handler/        → Global hata yakalayan ControllerAdvice
     │    ├── mapper/         → Entity ↔ DTO dönüştürücüler
     │    ├── model/          → Entity sınıfları (JPA)
     │    ├── repository/     → JPA repository arayüzleri
     │    ├── security/       → JWT ve kullanıcı kimlik doğrulama
     │    ├── service/        → İş mantığı (business logic)
     │    └── task/           → Zamanlanmış görevler (token temizleme)
     └── resources/
          ├── application.properties
          └── logback-spring.xml
```

---

## 🔐 Kimlik Doğrulama Akışı

* JWT tabanlı kimlik doğrulama sistemi.
* Kullanicı kaydı sonrası e-posta doğrulaması.
* Aktif tokenlar `valid_token` tablosunda tutulur.
* Token geçerliliği `ValidTokenService` tarafından denetlenir.
* `logout` çağrısı token'ı iptal eder.

---

## ✉️ E-Posta Sistemi

`MailService`, aşağıdaki durumlarda HTML formatlı mailler gönderir:

* Hesap doğrulama
* Şifre sıfırlama
* E-posta adresi değiştirme

SMTP ayarları `.env` dosyasında belirtilir:

```env
MAIL_USERNAME=example@gmail.com
MAIL_PASSWORD=secretpassword
FRONTEND_URL=http://localhost:3000
```

---

## 🖼️ Görsel Yükleme (ImageKit)

* Finch gönderileri: `/finch/{username}/posts/{finchId}`
* Profil resmi: `/finch/{username}/ProfileImages`
* Banner: `/finch/{username}/BannerImages`

Silme durumunda ilgili klasör de temizlenir.
Tüm yüklemeler `ImageKitService` aracılığıyla gerçekleştirilir.

---

## 🧬 Ana Servisler

| Servis                 | Açıklama                                           |
| ---------------------- | -------------------------------------------------- |
| `AuthService`          | Kullanicı kaydı, giriş, doğrulama, şifre sıfırlama |
| `UserService`          | Profil güncelleme, e-posta/değişim, silme          |
| `FinchService`         | Gönderi CRUD, reply, quote                         |
| `LikeService`          | Beğeni ekleme/geri alma                            |
| `RefinchService`       | Repost (yeniden paylaşma)                          |
| `FollowService`        | Takip etme/bırakma, takipçi listeleri              |
| `FollowRequestService` | Gizli hesaplara takip isteği                       |
| `BookmarkService`      | Gönderi kaydetme                                   |
| `FeedService`          | Ana sayfa akışı (global & takip edilenler)         |
| `SearchService`        | Kullanıcı & gönderi arama                          |
| `MailService`          | E-posta gönderimleri                               |
| `ImageKitService`      | Görsel yükleme/silme                               |

---

## ⚙️ Zamanlanmış Görev

Her gece saat **04:00**'te süresi dolan JWT tokenlar otomatik olarak silinir.

```java
@Scheduled(cron = "0 0 4 * * ?")
```

---

## 🔧 Örnek `.env`

```env
DB_URL=jdbc:mysql://localhost:3306/finch_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
SECRET_KEY=base64_jwt_secret
MAIL_USERNAME=example@gmail.com
MAIL_PASSWORD=secret
FRONTEND_URL=http://localhost:5173
BASE_URL=http://localhost:8080
IMAGEKIT_PUBLIC_KEY=...
IMAGEKIT_PRIVATE_KEY=...
IMAGEKIT_URL_ENDPOINT=https://ik.imagekit.io/...
```

---

## 🖥️ API Endpoint Örnekleri

| Metod    | Endpoint                     | Açıklama                 |
| -------- | ---------------------------- | ------------------------ |
| `POST`   | `/api/auth/register`         | Yeni kullanıcı kaydı     |
| `POST`   | `/api/auth/login`            | JWT token döner          |
| `POST`   | `/api/finch`                 | Yeni gönderi oluşturur   |
| `PUT`    | `/api/finch/{id}`            | Gönderi günceller        |
| `DELETE` | `/api/finch/{id}`            | Gönderi siler            |
| `GET`    | `/api/search/users?q=`       | Kullanıcı arar           |
| `POST`   | `/api/follow/{username}`     | Kullanıcıyı takip eder   |
| `POST`   | `/api/refinch/{finchId}`     | Gönderiyi yeniden paylaş |
| `POST`   | `/api/finch/{finchId}/likes` | Gönderiyi beğen          |
| `DELETE` | `/api/finch/{finchId}/likes` | Beğeniyi kaldır          |

---

## 📈 Loglama

`logback-spring.xml` ile hem **konsol** hem **dosya** loglama yapılır.

```
15:42:31.241 INFO  [http-nio-8080-exec-1] c.r.f.s.AuthService : User registered successfully
──────────────────────────────────────────────────────────────
```

---

## 🪟 Güvenlik & Temizlik

* Tokenlar ve resim klasörleri otomatik temizlenir.
* Kullanıcı silindiğinde ilgili ImageKit klasörü de silinir.
* Tüm `/api/**` endpointleri JWT koruması altındadır.
* `auth` ve `public` dışı endpointler kimlik doğrulama ister.

---