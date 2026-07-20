# Telco CRM Platform - Microservices Architecture

## Geliştirici Ekip (Pair 4)
| İsim | GitHub |
| :--- | :--- |
| **Mert Kırtı** | [![GitHub](https://img.shields.io/badge/GitHub-Mert--KiRTi-181717?style=flat&logo=github)](https://github.com/Mert-KiRTi) |
| **Nisasu Bozkurt** | [![GitHub](https://img.shields.io/badge/GitHub-nisasubozkurt-181717?style=flat&logo=github)](https://github.com/nisasubozkurt) |
| **Kaan Kaya** | [![GitHub](https://img.shields.io/badge/GitHub-KaanKayaS-181717?style=flat&logo=github)](https://github.com/KaanKayaS) |

---

## Proje Vizyonu ve Özeti

**Telco CRM Platform**, telekomünikasyon sektörüne yönelik müşteri deneyimini ve operasyonel süreçleri dijitalleştirmeyi amaçlayan kapsamlı bir MVP (Minimum Viable Product) projesidir. Sistem; **KYC (Müşterini Tanı)** süreçleri, **abonelik yönetimi**, **faturalandırma** ve **kota takibi** gibi kritik iş süreçlerini modern, ölçeklenebilir ve olay güdümlü (event-driven) bir altyapı üzerinde birleştirmektedir.

---

## Mimari ve Teknoloji Yığını (Tech Stack)

Projemiz en güncel teknolojiler ve mimari desenler (pattern) kullanılarak geliştirilmiştir. Dağıtık veri tutarlılığını sağlamak amacıyla **Saga Pattern** ve güvenilir mesaj iletimi için **Outbox Pattern** gibi ileri düzey mimari yaklaşımlar benimsenmiştir.

*   **Backend:** [![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=java)]() [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat&logo=spring-boot)]()
*   **Frontend:** [![Angular](https://img.shields.io/badge/Angular-DD0031?style=flat&logo=angular)]()
*   **Veritabanı ve Önbellek:** [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql)]() [![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis)]()
*   **Mesajlaşma ve Event Streaming:** [![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apache-kafka)]()
*   **Altyapı ve Dağıtım:** [![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker)]() [![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat&logo=kubernetes)]() (Minikube)

---

## Mikroservisler ve Görevleri

Sistemimiz bağımsız olarak ölçeklenebilen, iş ve altyapı sorumluluklarına göre ayrılmış 10'dan fazla mikroservisten oluşmaktadır.

### Altyapı Servisleri (Infrastructure Services)
| Servis Adı | Port | Görevi |
| :--- | :---: | :--- |
| **BFF Server** | `9090` | Frontend ile backend arasında köprü görevi gören Backend-for-Frontend katmanı. İstek birleştirme ve dönüştürme işlemlerini yürütür. |
| **API Gateway** | `9000` | Tüm istemci isteklerini karşılayan, yönlendiren ve merkezi güvenlik (CORS, JWT doğrulama vb.) sağlayan ana giriş kapısı. |
| **Eureka Server** | `8761` | Servislerin birbirini dinamik olarak bulmasını sağlayan Service Registry (Servis Kayıt) noktası. |
| **Config Server** | `8888` | Mikroservislerin yapılandırma (konfigürasyon) dosyalarını Git üzerinden merkezi olarak yöneten ve dağıtan servis. |
| **Identity Service** | `9001` | Keycloak entegrasyonu ile merkezi kimlik doğrulama, yetkilendirme (Auth) ve JWT token yönetimi servisi. |

### Geliştirici Araçları (Developer Tools)
| Servis Adı | Port | Görevi |
| :--- | :---: | :--- |
| **pgAdmin** | `5050` | Veritabanlarını tarayıcı üzerinden (UI) yönetmek ve sorgulamak için kullanılan yönetim aracı. (Giriş: `admin@teleco.com` / `admin123`) |

### İş Servisleri (Business Services)
| Servis Adı | Port | Görevi |
| :--- | :---: | :--- |
| **Customer Service** | `8081` | Müşteri profili oluşturma, yönetme ve KYC süreçlerinin yürütüldüğü ana servis. Kubernetes üzerinde çalışır. |
| **Product Catalog Service** | `8083` | Telekom tarifeleri, eklentiler (addon) ve ürünlerin tanımlandığı katalog yönetimi servisi. |
| **Order Service** | `8085` | Müşteri siparişlerini alan ve Saga orchestration pattern'ı başlatarak tüm zincirleme reaksiyonu yöneten servis. |
| **Subscription Service** | `8086` | Onaylanan siparişler sonrası abonelik kaydı oluşturan ve abonelik yaşam döngüsünü yöneten servis. |
| **Billing Service** | `8087` | Abonelik aktivasyonu sonrası müşteri faturalarını ve fatura döngülerini (Bill Cycle) oluşturan servis. |
| **Payment Service** | `9008` | Kesilen faturaların ödeme işlemlerini, iadeleri ve tahsilat durumlarını yöneten servis. |
| **Usage Service** | `9006` | Müşterilerin arama, SMS ve veri kullanımı (CDR) kayıtlarını takip eden ve raporlayan servis. |
| **Notification Service** | `8088` | Kafka üzerinden olayları dinleyerek müşterilere SMS/E-Posta gönderim simülasyonu yapan servis. |
| **Ticket Service** | `9010` | Müşteri destek taleplerini (support ticket) oluşturan, atayan ve yaşam döngüsünü yöneten servis. |

---

## Mimari Kararlar ve Tasarım Desenleri (Architectural Decisions)

Geleneksel monolitik yapılardan kaçınılarak, yüksek erişilebilirlik ve bağımsız ölçeklenebilirlik sunan mikroservis mimarisi tercih edilmiştir. Geliştirme sürecinde alınan temel mühendislik kararları şunlardır:

* **Database-per-Service (Servis Başına Veritabanı):** Her mikroservis kendi PostgreSQL veritabanına sahiptir. Servisler arası veri tabanı paylaşımı kesinlikle yasaklanmış olup, veriye erişim yalnızca ilgili servisin API'leri üzerinden sağlanmaktadır. Bu sayede servislerin "Loose Coupling" (Gevşek Bağlılık) ilkesine uyması garantilenmiştir.
* **Saga Orchestration Pattern:** Sipariş geçilmesi (Order) gibi birden fazla servisi (Subscription, Billing) ilgilendiren dağıtık transaction işlemlerinde, veri tutarlılığını sağlamak için Saga Orchestrator deseni kullanılmıştır. Herhangi bir adımda hata alınması durumunda, başarılı olan önceki adımlar "Compensating Transactions" (Telafi İşlemleri) ile geri alınmaktadır.
* **Event-Driven Architecture (Olay Güdümlü Mimari):** Servisler arası asenkron iletişim ve bildirim süreçleri için Apache Kafka tercih edilmiştir. Sistemin bir noktasında meydana gelen durum değişiklikleri (örn. `OrderConfirmed`), diğer servisler tarafından dinlenerek anında aksiyon alınmasını sağlamaktadır.

## Güvenlik ve Kimlik Doğrulama (Security Architecture)

Sistemin dış dünyaya açılan tek kapısı **API Gateway** olarak konumlandırılmıştır. Dışarıdan gelen hiçbir istek, doğrudan mikroservislere ulaşamaz.

* **Merkezi Kimlik Yönetimi:** Güvenlik altyapısı **Keycloak** üzerine inşa edilmiştir. Kullanıcı doğrulamaları (Authentication) ve yetkilendirmeleri (Authorization) OAuth2.0 ve OpenID Connect standartlarına uygun olarak JWT (JSON Web Token) ile sağlanmaktadır.
* **Gateway Seviyesinde Güvenlik:** API Gateway, kendisine gelen isteklerdeki JWT token'ları doğrular. Sadece geçerli ve yetkili token'a sahip istekler (örn. `Authorization: Bearer <token>`) iç ağdaki (Eureka üzerindeki) mikroservislere yönlendirilir.

## Hata Yönetimi ve Dayanıklılık (Resilience)

Dağıtık sistemlerin doğası gereği oluşabilecek ağ veya donanım hatalarına karşı sistemin bütünlüğünü korumak adına çeşitli önlemler alınmıştır:

* **Global Exception Handling:** Her serviste Spring tabanlı `@ControllerAdvice` yapıları kurularak, hataların istemciye (Frontend/Gateway) standart, anlamlı ve temiz bir JSON formatında (Problem Details standardı) dönmesi sağlanmıştır.
* **Dağıtık Loglama Altyapısına Hazırlık:** Her istek için benzersiz korelasyon kimlikleri (Correlation ID) üretilerek, bir isteğin farklı mikroservisler üzerindeki yolculuğu izlenebilir hale getirilmiştir.

---

## Gelecek Planları ve Yol Haritası (Roadmap)

MVP aşaması başarıyla tamamlanan platformun bir sonraki fazında devreye alınması planlanan geliştirmeler şunlardır:

1. **Observability (Gözlemlenebilirlik):** Prometheus ve Grafana entegrasyonu ile servislerin anlık donanım/performans metriklerinin izlenmesi.
2. **Centralized Logging (Merkezi Loglama):** ELK (Elasticsearch, Logstash, Kibana) stack kullanılarak tüm servis loglarının tek bir merkezde toplanması ve analiz edilmesi.
3. **CI/CD Süreçleri:** GitHub Actions veya Jenkins pipeline'ları kurularak, kod değişikliklerinin otomatik test edilip Docker imajlarına dönüştürülmesi ve Kubernetes ortamına (Continuous Deployment) aktarılması.

---

## Kurulum ve Çalıştırma

Projeyi yerel ortamınızda ayağa kaldırmak için aşağıdaki bileşenlerin sisteminizde yüklü olması gerekmektedir.

### Ön Koşullar
*   **Docker & Docker Compose**
*   **Minikube & kubectl**
*   **Java 21**
*   **Node.js & npm**

### Çalıştırma Scriptleri

Geliştirici deneyimini artırmak adına tüm sistemi tek komutla yönetebileceğiniz scriptler hazırlanmıştır. İşletim sisteminize göre aşağıdaki komutları kullanabilirsiniz:

**1. Sistemi Ayağa Kaldırmak:**
Altyapıyı, Kubernetes ortamını, mikroservisleri ve frontend uygulamasını sırasıyla başlatır.
```bash
# Linux / macOS
bash scripts/start-all.sh

# Windows (PowerShell)
.\scripts\start-all.ps1
```

**2. Sistemi Durdurmak:**
Çalışan servisleri ve container'ları geçici olarak durdurur. Verileriniz korunur.
```bash
# Linux / macOS
bash scripts/stop-all.sh

# Windows (PowerShell)
.\scripts\stop-all.ps1
```

**3. Sistemi Tamamen Sıfırlamak:**
Tüm container'ları, portları ve verileri temizleyip projeyi başlangıç durumuna getirir.
```bash
# Linux / macOS
bash scripts/teardown-all.sh

# Windows (PowerShell)
.\scripts\teardown-all.ps1
```

### Servisleri Manuel Ayağa Kaldırma (Alternatif Yöntem)

Script kullanmak istemiyorsanız veya servisleri geliştirme amaçlı izole olarak test etmek isterseniz, terminalinizde farklı sekmeler açıp proje kök dizininde sırasıyla aşağıdaki komutları çalıştırabilirsiniz.

*(Her bir servisin loglarında başarı mesajını gördükten sonra diğerine geçmeniz önerilir. Daha önce çalıştırılmış servisleri tekrar başlatmanıza gerek yoktur).*

1. **Sekme:** `mvn spring-boot:run -pl eureka-server`
2. **Sekme:** `mvn spring-boot:run -pl gateway-server`
3. **Sekme:** `mvn spring-boot:run -pl identity-service`
4. **Sekme:** `mvn spring-boot:run -pl customer-service`
5. **Sekme:** `mvn spring-boot:run -pl product-catalog-service`
6. **Sekme:** `mvn spring-boot:run -pl order-service`
7. **Sekme:** `mvn spring-boot:run -pl billing-service`
8. **Sekme:** `mvn spring-boot:run -pl payment-service`

---

## Uçtan Uca (E2E) Test Rehberi (Detaylı Akış)

Sistemdeki tüm servisler başlatıldıktan sonra, ana erişim noktası olan **Gateway (Port: 9000)** kullanılmalıdır. Testleri Postman veya Swagger arayüzü (`http://localhost:9000/swagger-ui/index.html`) üzerinden gerçekleştirebilirsiniz.

Tüm API isteklerinde Header kısmına `Authorization: Bearer <TOKEN>` eklenmesi zorunludur.

### Adım 1: Sisteme Giriş ve Yetkilendirme (`identity-service`)
Sistemde işlem yapabilmek adına öncelikle yetkili bir JWT token alınmalıdır.

**Endpoint:** `POST http://localhost:9000/api/v1/auth/login`

**Request Body:**
```json
{
  "username": "admin@telco.com",
  "password": "admin123"
}
```
* **Aksiyon:** Dönen yanıt içerisindeki token değerini kopyalayınız. Sonraki tüm API çağrılarında bu token kullanılacaktır.

### Adım 2: Müşteri Profili Oluşturma (`customer-service`)
KYC (Know Your Customer) standartlarına uygun olarak yeni bir müşteri profili oluşturulması gerekmektedir.

**Endpoint:** `POST http://localhost:9000/api/v1/customers`

**Request Body:**
```json
{
  "firstName": "Nisasu",
  "lastName": "Bozkurt",
  "email": "nisasu@example.com",
  "identityNumber": "12345678901",
  "phone": "05321234567",
  "birthDate": "1990-01-01"
}
```
* **Aksiyon:** Dönen yanıttaki **`id`** (Müşteri ID) değerini kaydediniz.

### Adım 3: Tarife Tanımlama (`product-catalog-service`)
Sistemde müşteriye sunulacak bir ürün veya tarife kaydı oluşturulması gerekmektedir.

**Endpoint:** `POST http://localhost:9000/api/v1/tariffs`

**Request Body:**
```json
{
  "code": "POSTPAID-STD-001",
  "name": "Standart Postpaid 5GB",
  "type": "POSTPAID",
  "monthlyFee": 299.90,
  "minutesIncluded": 500,
  "smsIncluded": 250,
  "dataMbIncluded": 5120,
  "status": "ACTIVE",
  "effectiveFrom": "2026-01-01"
}
```
* **Aksiyon:** Dönen yanıttaki **`id`** veya **`code`** bilgisini (Tarife ID) kaydediniz.

### Adım 4: Sipariş İşleminin Başlatılması (`order-service`)
Müşteri için seçilen tarife üzerinden sipariş işlemi başlatılmaktadır. Bu adım ile birlikte sistemdeki dağıtık Saga mimarisi tetiklenir.

**Endpoint:** `POST http://localhost:9000/api/v1/orders`

**Request Body:**
```json
{
  "customerId": "ADIM-2-DEKI-MUSTERI-ID",
  "totalAmount": 299.90,
  "currency": "TRY",
  "items": [
    {
      "productCode": "POSTPAID-STD-001",
      "productType": "POSTPAID_TARIFF",
      "quantity": 1,
      "unitPrice": 299.90
    }
  ]
}
```
* **Sistem Arka Plan Akışı:**
  1. `order-service` sipariş kaydını oluşturur.
  2. Message broker (Kafka) üzerinden `OrderConfirmed` olayı yayınlanır.
  3. `subscription-service` ilgili olayı dinleyerek müşteriye MSISDN tahsis eder ve aboneliği başlatır.
  4. `billing-service` aboneliğin aktifleştiğini algılayarak ilk fatura döngüsünü oluşturur.

### Adım 5: Fatura Görüntüleme (`billing-service`)
Sistem tarafından otomatik oluşturulan fatura, müşteri ID'si üzerinden sorgulanabilir.

**Endpoint:** `GET http://localhost:9000/api/v1/invoices?customerId=ADIM-2-DEKI-MUSTERI-ID`

* **Aksiyon:** Yanıtta durumu `ISSUED` olan faturanın **`id`** değerini kaydediniz.

> [!TIP]
> Gerekli durumlarda `GET http://localhost:9000/api/v1/invoices/{fatura_id}/pdf` adresi üzerinden faturayı PDF formatında indirebilirsiniz.

### Adım 6: Fatura Ödeme İşlemi (`payment-service`)
Oluşturulan fatura üzerinden tahsilat işlemi gerçekleştirilmektedir.

**Endpoint:** `POST http://localhost:9000/api/v1/payments`

**Request Body:**
```json
{
  "invoiceId": "ADIM-5-TEKI-FATURA-ID",
  "amount": 299.90,
  "method": "CREDIT_CARD",
  "paymentRequestId": "web-odeme-1001"
}
```
* **Aksiyon:** İşlem başarılı olduğunda faturanın durumu `PAID` olarak güncellenecek ve bilgilendirme süreçleri tetiklenecektir.

### Adım 7: Müşteri Destek Kaydı Oluşturma (`ticket-service`)
Müşteri hizmetleri modülü üzerinden teknik destek veya şikayet kaydı (Ticket) oluşturma adımıdır.

**Endpoint:** `POST http://localhost:9000/api/v1/tickets`

**Request Body:**
```json
{
  "customerId": "ADIM-2-DEKI-MUSTERI-ID",
  "category": "FAULT",
  "priority": "HIGH",
  "description": "2 saattir internet bağlantım yok, lütfen acil destek."
}
```
* **Aksiyon:** Destek bileti sisteme kaydedilir ve SLA (Servis Seviyesi Anlaşması) kuralları işletilmeye başlar.

### Adım 8: Sistem Bildirimlerinin Doğrulanması (`notification-service`)
Tüm bu uçtan uca akış boyunca üretilen sistem olaylarının, müşteriye iletişim kanalları üzerinden başarılı bir şekilde iletilip iletilmediği kontrol edilmektedir.

**Endpoint:** `GET http://localhost:9000/api/v1/notifications/users/ADIM-2-DEKI-MUSTERI-ID/history`

* **Beklenen Sonuç:** İşlem geçmişinde aşağıdaki bildirim şablonlarının `SENT` statüsünde yer alması beklenmektedir:
  * `ORDER_CONFIRMED` (Sipariş onayı)
  * `SUBSCRIPTION_ACTIVATED` (Abonelik başlatılması)
  * `INVOICE_GENERATED` (Fatura oluşturulması)
  * `PAYMENT_SUCCESS` (Ödeme tahsilatı)
  * `TICKET_OPENED` (Destek talebi kaydı)

Yukarıdaki adımların tamamlanmasıyla birlikte; Müşteri Yönetimi, Sipariş, Abonelik, Faturalandırma, Ödeme, Destek ve Bildirim modüllerinden oluşan tüm CRM yaşam döngüsü başarıyla test edilmiş olur.

## Ekran Görüntüleri ve Diyagramlar

> **Projeye ait veritabanı şemaları ve mimari diyagramlar ilgili dizinlerde mevcuttur.**
*   [**Mikroservis ER Diyagramları (PDF)**](./Pair%20Ödevi-Mikroservis%20ER%20Diagramlar-Pair%204.pdf) - Tüm mikroservislerin veritabanı tablolarını ve ilişkisel (Entity-Relationship) yapılarını detaylıca inceleyebilirsiniz.