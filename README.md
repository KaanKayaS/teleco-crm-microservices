#  Telco CRM Platform - Microservices Architecture

##  Geliştirici Ekip (Pair 4)
| İsim | GitHub |
| :--- | :--- |
| **Mert Kırtı** | [![GitHub](https://img.shields.io/badge/GitHub-Mert--KiRTi-181717?style=flat&logo=github)](https://github.com/Mert-KiRTi) |
| **Nisasu Bozkurt** | [![GitHub](https://img.shields.io/badge/GitHub-nisasubozkurt-181717?style=flat&logo=github)](https://github.com/nisasubozkurt) |
| **Kaan Kaya** | [![GitHub](https://img.shields.io/badge/GitHub-KaanKayaS-181717?style=flat&logo=github)](https://github.com/KaanKayaS) |

---

##  Proje Vizyonu ve Özeti

**Telco CRM Platform**, telekomünikasyon sektörüne yönelik müşteri deneyimini ve operasyonel süreçleri dijitalleştirmeyi amaçlayan kapsamlı bir MVP projesidir. Sistem; **KYC (Müşterini Tanı)** süreçleri, **abonelik yönetimi**, **faturalandırma** ve **kota takibi** gibi kritik iş süreçlerini modern, ölçeklenebilir ve olay güdümlü (event-driven) bir altyapı üzerinde birleştirir.

---

##  Mimari ve Teknoloji Yığını (Tech Stack)

Projemiz en güncel teknolojiler ve mimari desenler (pattern) kullanılarak geliştirilmiştir. Dağıtık veri tutarlılığını sağlamak amacıyla **Saga Pattern** ve güvenilir mesaj iletimi için **Outbox Pattern** gibi ileri düzey mimari yaklaşımlar benimsenmiştir.

*   **Backend:** ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=java) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat&logo=spring-boot)
*   **Frontend:** ![Angular](https://img.shields.io/badge/Angular-DD0031?style=flat&logo=angular)
*   **Veritabanı ve Önbellek:** ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis)
*   **Mesajlaşma ve Event Streaming:** ![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apache-kafka)
*   **Altyapı ve Dağıtım:** ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker) ![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat&logo=kubernetes) (Minikube)

---

##  Mikroservisler ve Görevleri

Sistemimiz bağımsız olarak ölçeklenebilen, iş ve altyapı sorumluluklarına göre ayrılmış 10'dan fazla mikroservisten oluşmaktadır.

###  Altyapı Servisleri (Infrastructure Services)
| Servis Adı | Görevi |
| :--- | :--- |
| **API Gateway** | Tüm istemci isteklerini karşılayan, yönlendiren ve merkezi güvenlik (CORS, vb.) sağlayan ana giriş kapısı. |
| **Eureka Server** | Servislerin birbirini bulmasını sağlayan Service Registry noktası. |
| **Config Server** | Mikroservislerin yapılandırma (konfigürasyon) dosyalarını merkezi olarak yöneten servis. |
| **Identity Service** | Keycloak entegrasyonu ile merkezi kimlik doğrulama, yetkilendirme (Auth) ve token yönetimi servisi. |

###  İş Servisleri (Business Services)
| Servis Adı | Görevi |
| :--- | :--- |
| **Customer Service** | Müşteri profili oluşturma, yönetme ve KYC süreçlerinin yürütüldüğü ana servis. |
| **Product Catalog Service**| Telekom tarifeleri, paketler ve ürünlerin tanımlandığı katalog yönetimi servisi. |
| **Order Service** | Müşteri siparişlerini alan ve Saga pattern'ı başlatarak zincirleme reaksiyonu yöneten servis. |
| **Billing Service** | Abonelik aktivasyonu sonrası müşteri faturalarını ve döngülerini (Bill Cycle) oluşturan servis. |
| **Payment Service** | Kesilen faturaların ödeme işlemlerini, iadeleri ve tahsilat durumlarını yöneten servis. |
| **Notification Service** | Kafka üzerinden olayları dinleyerek SMS/E-Posta gönderim simülasyonu yapan servis. |

---

##  Kurulum ve Çalıştırma (How to Run)

Projeyi yerel ortamınızda ayağa kaldırmak için bazı ön koşulların sisteminizde yüklü olması gerekmektedir. 

###  Ön Koşullar
*   **Docker & Docker Compose**
*   **Minikube & kubectl**
*   **Java 21**
*   **Node.js & npm**

###  Çalıştırma Scriptleri

Geliştirici deneyimini artırmak adına tüm sistemi tek tıkla yönetebileceğiniz scriptler hazırlanmıştır. İşletim sisteminize göre aşağıdaki komutları kullanabilirsiniz:

**1. Sistemi Ayağa Kaldırmak:**
Altyapıyı, Kubernetes (K8s) ortamını, mikroservisleri ve frontend uygulamasını sırasıyla başlatır.
```bash
# Linux / macOS
./start-all.sh

# Windows (PowerShell)
.\start-all.ps1
```

**2. Sistemi Durdurmak:**
Çalışan servisleri ve container'ları geçici olarak durdurur. Verileriniz kaybolmaz.
```bash
# Linux / macOS
./stop-all.sh

# Windows (PowerShell)
.\stop-all.ps1
```

**3. Sistemi Tamamen Sıfırlamak:**
Tüm container'ları, portları ve verileri temizleyip projeyi ilk günkü sıfır haline getirir.
```bash
# Linux / macOS
./teardown-all.sh

# Windows (PowerShell)
.\teardown-all.ps1
```

###  Servisleri Manuel Ayağa Kaldırma (Alternatif Yöntem)

Script kullanmak istemiyorsanız veya servisleri geliştirme amaçlı izole olarak test etmek isterseniz, terminalinizde farklı sekmeler (tab) açıp ana proje dizininize (`cd <proje_dizini>`) giderek aşağıdaki servisleri sırasıyla çalıştırabilirsiniz.

*(Her bir servisin loglarda `Started ...` yazısını gördükten sonra diğerine geçmeniz sağlıklı olacaktır. `identity` ve `payment` zaten açıksa onları tekrar açmanıza gerek yoktur).*

1. **Sekme:** `mvn spring-boot:run -pl eureka-server`
2. **Sekme:** `mvn spring-boot:run -pl gateway-server`
3. **Sekme:** `mvn spring-boot:run -pl identity-service` *(Açıksa geçin)*
4. **Sekme:** `mvn spring-boot:run -pl customer-service`
5. **Sekme:** `mvn spring-boot:run -pl product-catalog-service`
6. **Sekme:** `mvn spring-boot:run -pl order-service`
7. **Sekme:** `mvn spring-boot:run -pl billing-service`
8. **Sekme:** `mvn spring-boot:run -pl payment-service` *(Açıksa geçin)*

---

## 🧪 Uçtan Uca (E2E) Test Rehberi (Detaylı Akış)

Bütün servisler birbirini tanıyıp `eureka-server` üzerinde ayağa kalktıktan sonra, artık her şeye ana kapımız olan **Gateway (Port: 9000)** üzerinden erişeceğiz. (Tarayıcıda her servis için ayrı port ezberlemenize gerek kalmayacak).

### Adım 1: Sisteme Giriş ve Token Alma
1. **`http://localhost:9000/api/v1/auth/login`** ucuna POST isteği atarak veya doğrudan Keycloak üzerinden token alacağız.
2. Swagger arayüzü seviyorsanız: **`http://localhost:9001/swagger-ui/index.html`** adresine gidin. `POST /api/v1/auth/login` kısmından `admin`/`admin` ile giriş yapıp token'ı tırnak işaretleri olmadan (**sadece uzun metni**) kopyalayın.

> [!WARNING]
> Kopyaladığınız token'ı bundan sonraki adımlarda tüm servislerin Swagger arayüzlerindeki "Authorize" butonuna tıklayıp **sadece token metnini (başında Bearer vb. kelimeler OLMADAN)** yapıştıracaksınız.

### Adım 2: Müşteri Profili Oluşturma (`customer-service`)
1. **`http://localhost:8081/swagger-ui/index.html`** adresine gidin. (Authorize butonuna token'ı yapıştırın).
2. **`POST /api/v1/customers`** ucuna girip şu bilgileri yollayın:
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
3. Dönen cevaptaki **`id`** (Örn: `1` veya `uuid`) değerini bir yere not edin. Bu müşterimizin ID'sidir.

### Adım 3: Tarife Seçme (`product-catalog-service`)
*(Sistemde zaten tarifelerinizin eklendiğini varsayıyoruz. Eğer yoksa önce POST ile bir tarife eklemelisiniz).*
1. **`http://localhost:8083/swagger-ui/index.html`** adresinden **`GET /api/v1/catalog/tariffs`** ucunu çağırın.
2. Karşınıza çıkan tarifelerden birinin **`id`** sini kopyalayın. (Örn: `"Premium 50GB" ` tarifesinin ID'si).

### Adım 4: Sipariş Geçme (`order-service`)
İşte işlerin koptuğu nokta burası! Siparişi verdiğimiz an sistem zincirleme reaksiyona başlayacak.
1. **`http://localhost:8085/swagger-ui/index.html`** adresine gidin. (Authorize kısmına token'ı yapıştırın).
2. **`POST /api/v1/orders`** sekmesini açın.
3. Şu JSON verisini kullanın (Adım 2 ve 3'te not ettiğiniz ID'leri buraya yazın):
```json
{
  "customerId": "Adım-2-deki-Musteri-ID",
  "totalAmount": 450,
  "currency": "TRY",
  "items": [
    {
      "productCode": "Adım-3-teki-Tarife-ID",
      "productType": "POSTPAID_TARIFF",
      "quantity": 1,
      "unitPrice": 450
    }
  ]
}
```
4. **Execute** butonuna basın. (Sipariş "CREATED" statüsünde oluşacak).

### Adım 5: Zincirleme Reaksiyonu (Saga) İzleme
Siparişi verdiğiniz an arkada şunlar yaşanacak:

1. **Sipariş ve Abonelik**
   - Müşteri aboneliği aktifleşir.
   - Kafka'ya `SubscriptionActivated` (Abonelik Aktifleşti) mesajı fırlatılır.

2. **Fatura (Billing Service)**
   - `billing-service`, bu `SubscriptionActivated` mesajını dinler ve müşteri için bir **Fatura Döngüsü (Bill Cycle)** oluşturur. 

3. **Ödeme Beklentisi (Payment Service)**
   - `payment-service` faturanın kesildiğini duyduğunda loglara yazar ve tahsilat sürecini başlatmak için bekler.

---

### Adım 6: Faturayı Ödeme
Faturanız kesildikten sonra (`GET /api/v1/invoices` ile kontrol ettiğinizde durumu `ISSUED` ise):
1. **`http://localhost:9008/swagger-ui/index.html`** adresine gidin.
2. **`POST /api/v1/payments`** sekmesini açın.
3. Aşağıdaki JSON formatında, kesilen faturanızın ID'sini girerek ödeme işlemini yapın:
```json
{
  "invoiceId": "KESİLEN_FATURA_ID_BURAYA",
  "amount": 450,
  "method": "CREDIT_CARD",
  "paymentRequestId": "test-odeme-1234"
}
```

4. **Ödeme Kontrolü:**
    *   Ödeme işlemi başarılı olursa sipariş süreci tamamlanır, müşteri bildirimleri gönderilir.
    *   `GET /api/v1/payments/{paymentId}` veya Faturaya gidip statünün `PAID` olduğunu kontrol edebilirsiniz.

İşte bu kadar! Uçtan uca tüm telekom süreçleri tamamlandı.

---

### Ek Adım: Bildirimlerin Doğrulanması (Notification Verification)

Müşteri uçtan uca (sipariş, abonelik, fatura, ödeme) tüm döngüyü tamamladıktan sonra, bu olayların hepsi Kafka'ya düşer ve `notification-service` bunları yakalayarak SMS/E-posta gönderim simülasyonu yapar. Tüm e-postaların müşteriye ulaşıp ulaşmadığını kontrol edin.

**Endpoint:** `GET /api/v1/notifications/users/{customerId}/history` (veya port 8088'den Swagger)

**Parametreler:**
*   `id`: Müşteri ID (`d80189e0-62e0-4141-bd9e-662fc4119f7a` gibi)

**Doğrulama (Validation):**
*   Geçmişte müşteriye `SENT` (Gönderildi) durumunda aşağıdaki şablonlar (templateCode) iletilmiş mi kontrol edin:
    *   `ORDER_CONFIRMED`
    *   `SUBSCRIPTION_ACTIVATED`
    *   `INVOICE_GENERATED`
    *   `PAYMENT_SUCCESS`
*   Eğer iade işlemi varsa `PAYMENT_REFUNDED` kaydı görülmelidir.

---

##  Ekran Görüntüleri ve Diyagramlar

>  **Projeye ait ER diyagramları ve mimari şemalar klasörlerde mevcuttur.**
*  [**Mikroservis ER Diyagramları (PDF)**](./Pair%20Ödevi-Mikroservis%20ER%20Diagramlar-Pair%204.pdf) - Tüm mikroservislerin veritabanı tablolarını ve ilişkilerini (Entity-Relationship) detaylıca inceleyebilirsiniz.