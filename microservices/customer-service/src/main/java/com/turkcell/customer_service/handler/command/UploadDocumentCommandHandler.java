package com.turkcell.customer_service.handler.command;

import com.turkcell.customer_service.command.UploadDocumentCommand;
import com.turkcell.customer_service.cqrs.ICommandHandler;
import com.turkcell.customer_service.dto.response.DocumentResponse;
import com.turkcell.customer_service.entity.Customer;
import com.turkcell.customer_service.entity.Document;
import com.turkcell.customer_service.exception.CustomerNotFoundException;
import com.turkcell.customer_service.repository.CustomerRepository;
import com.turkcell.customer_service.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Müşteriye ait KYC belgesini diske kaydeder ve Document kaydı oluşturur.
 * <p>
 * Dosyalar: {@code <upload.dir>/<customerId>/<originalFilename>} yoluna yazılır.
 * </p>
 */
@Component
public class UploadDocumentCommandHandler
        implements ICommandHandler<UploadDocumentCommand, DocumentResponse> {

    private final CustomerRepository customerRepository;
    private final DocumentRepository documentRepository;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    public UploadDocumentCommandHandler(CustomerRepository customerRepository,
                                        DocumentRepository documentRepository) {
        this.customerRepository = customerRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public DocumentResponse handle(UploadDocumentCommand command) {
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        String fileRef = saveFile(command);

        Document document = new Document();
        document.setCustomer(customer);
        document.setType(command.type());
        document.setFileRef(fileRef);

        document = documentRepository.save(document);

        return new DocumentResponse(
                document.getId(),
                customer.getId(),
                document.getType(),
                document.getFileRef(),
                document.getVerifiedAt()
        );
    }

    private String saveFile(UploadDocumentCommand command) {
        try {
            Path dir = Paths.get(uploadDir, command.customerId().toString());
            Files.createDirectories(dir);

            String safeFilename = sanitizeFilename(command.originalFilename());
            Path destination = dir.resolve(safeFilename);

            Files.write(destination, command.fileContent(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return destination.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new UncheckedIOException("Dosya kaydedilemedi: " + command.originalFilename(), e);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document";
        }
        return filename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}
