CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    locale VARCHAR(10) DEFAULT 'tr',
    subject VARCHAR(255),
    body_template TEXT NOT NULL,
    UNIQUE(code, channel)
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    payload_json TEXT,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP
);

INSERT INTO notification_templates (id, code, channel, subject, body_template) VALUES
(uuid_generate_v4(), 'ORDER_CONFIRMED', 'EMAIL', 'Siparişiniz Alındı', 'Değerli müşterimiz, siparişiniz başarıyla alınmıştır.'),
(uuid_generate_v4(), 'SUBSCRIPTION_ACTIVATED', 'EMAIL', 'Hattınız Açıldı', 'Aboneliğiniz başlatılmış ve hattınız kullanıma açılmıştır. Bizi tercih ettiğiniz için teşekkürler!'),
(uuid_generate_v4(), 'INVOICE_GENERATED', 'EMAIL', 'Yeni Faturanız', 'Değerli müşterimiz, yeni faturanız kesilmiştir. Lütfen son ödeme tarihinden önce ödemenizi gerçekleştiriniz.'),
(uuid_generate_v4(), 'PAYMENT_SUCCESS', 'EMAIL', 'Ödemeniz Alındı', 'Ödemeniz başarıyla tahsil edilmiştir. Teşekkür ederiz.'),
(uuid_generate_v4(), 'PAYMENT_REFUNDED', 'EMAIL', 'İade İşlemi', 'Ödemeniz başarıyla iade edilmiştir.');
