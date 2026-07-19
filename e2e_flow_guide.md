# Mikroservis Uçtan Uca (E2E) Test Rehberi

Bütün orkestrayı çalıştırıp bir müşterinin sisteme kayıt olmasından, faturasının kesilip ödemesinin alınmasına kadar olan gerçek senaryoyu test ediyoruz.

## 1. Servisleri Ayağa Kaldırma
Lütfen terminalinizde farklı sekmeler (tab) açın ve her sekmede ana proje dizininize (`cd /Users/subozkurt/teleco-crm-microservices/microservices`) giderek aşağıdaki servisleri sırasıyla çalıştırın.

*(Her bir servisin `Started ...` yazısını gördükten sonra diğerine geçmeniz sağlıklı olacaktır. `identity` ve `payment` zaten açıksa onları tekrar açmanıza gerek yoktur).*

1. **Sekme:** `mvn spring-boot:run -pl eureka-server`
2. **Sekme:** `mvn spring-boot:run -pl gateway-server`
3. **Sekme:** `mvn spring-boot:run -pl identity-service` *(Açıksa geçin)*
4. **Sekme:** `mvn spring-boot:run -pl customer-service`
5. **Sekme:** `mvn spring-boot:run -pl product-catalog-service`
6. **Sekme:** `mvn spring-boot:run -pl order-service`
7. **Sekme:** `mvn spring-boot:run -pl billing-service`
8. **Sekme:** `mvn spring-boot:run -pl payment-service` *(Açıksa geçin)*

---

## 2. Uçtan Uca Akış Senaryosu
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

### Adım 7: Bildirimlerin Doğrulanması (Notification Verification)

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

## Sonuç

Bu rehber, Telco CRM sistemindeki ana iş akışını (Müşteri -> Sipariş -> Abonelik -> Faturalandırma -> Ödeme -> Bildirim) uçtan uca test etmenizi sağlar. Tüm sistem olay güdümlü (event-driven) yapıda birbirini otomatik tetikleyecek şekilde tasarlanmıştır.
