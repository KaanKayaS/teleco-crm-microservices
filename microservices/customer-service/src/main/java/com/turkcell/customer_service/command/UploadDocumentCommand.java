package com.turkcell.customer_service.command;

import com.turkcell.customer_service.cqrs.ICommand;
import com.turkcell.customer_service.dto.response.DocumentResponse;

import java.util.UUID;

/**
 * Command: müşteriye KYC belgesi yükle (multipart içeriği byte[] olarak taşınır).
 */
public record UploadDocumentCommand(
        UUID customerId,
        String type,
        String originalFilename,
        String contentType,
        byte[] fileContent
) implements ICommand<DocumentResponse> {}
