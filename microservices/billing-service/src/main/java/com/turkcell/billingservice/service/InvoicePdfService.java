package com.turkcell.billingservice.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.turkcell.billingservice.model.entity.Invoice;
import com.turkcell.billingservice.model.entity.InvoiceLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    public byte[] generatePdf(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1254", false, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1254", false, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, "Cp1254", false, 12);

            Paragraph title = new Paragraph("TELEKOM FATURASI (INVOICE)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Fatura No (Invoice ID): " + invoice.getId(), normalFont));
            document.add(new Paragraph("Müşteri No (Customer ID): " + invoice.getCustomerId(), normalFont));
            document.add(new Paragraph("Fatura Tarihi (Issued At): " + (invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : "N/A"), normalFont));
            document.add(new Paragraph("Son Ödeme (Due Date): " + invoice.getDueDate(), normalFont));
            document.add(new Paragraph(" ")); // Bosluk

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 2f});

            addTableHeader(table, headerFont, "Açıklama (Description)");
            addTableHeader(table, headerFont, "Miktar (Qty)");
            addTableHeader(table, headerFont, "Birim Fiyat (Unit Price)");
            addTableHeader(table, headerFont, "Toplam (Total)");

            for (InvoiceLine line : invoice.getLines()) {
                table.addCell(new Phrase(line.getDescription(), normalFont));
                table.addCell(new Phrase(line.getQuantity().toString(), normalFont));
                table.addCell(new Phrase(line.getUnitPrice() + " TL", normalFont));
                table.addCell(new Phrase(line.getLineTotal() + " TL", normalFont));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Paragraph subTotal = new Paragraph("Ara Toplam (Subtotal): " + invoice.getSubTotal() + " TL", normalFont);
            subTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(subTotal);

            Paragraph tax = new Paragraph("KDV (%20) (Tax): " + invoice.getTax() + " TL", normalFont);
            tax.setAlignment(Element.ALIGN_RIGHT);
            document.add(tax);

            Paragraph grandTotal = new Paragraph("Genel Toplam (Grand Total): " + invoice.getGrandTotal() + " TL", headerFont);
            grandTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(grandTotal);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Fatura PDF uretilirken hata olustu: " + e.getMessage(), e);
        }
    }

    private void addTableHeader(PdfPTable table, Font font, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingBottom(5);
        table.addCell(cell);
    }
}
