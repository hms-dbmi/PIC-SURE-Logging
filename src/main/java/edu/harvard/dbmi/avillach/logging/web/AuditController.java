package edu.harvard.dbmi.avillach.logging.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.dbmi.avillach.logging.model.AuditEvent;
import edu.harvard.dbmi.avillach.logging.service.AuditLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuditController {

    private static final int MAX_METADATA_KEYS = 50;
    private static final int MAX_ERROR_KEYS = 20;

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditController(
        AuditLogService auditLogService,
        @Qualifier("auditObjectMapper") ObjectMapper objectMapper
    ) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * The body is bound as String, not AuditEvent: Spring would return 415 when
     * Content-Type is absent, whereas the Javalin service returned 400.
     */
    @PostMapping("/audit")
    public ResponseEntity<Map<String, String>> audit(
        @RequestBody String body,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestHeader(value = "X-Request-Id", required = false) String requestIdHeader
    ) {
        AuditEvent event;
        try {
            event = objectMapper.readValue(body, AuditEvent.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON: " + e.getMessage());
        }

        if (event.metadata() != null && event.metadata().size() > MAX_METADATA_KEYS) {
            throw new BadRequestException("metadata must not exceed " + MAX_METADATA_KEYS + " keys");
        }
        if (event.error() != null && event.error().size() > MAX_ERROR_KEYS) {
            throw new BadRequestException("error must not exceed " + MAX_ERROR_KEYS + " keys");
        }
        if (event.eventType() == null || event.eventType().isBlank()) {
            throw new BadRequestException("event_type is required");
        }

        auditLogService.logEvent(event, authorizationHeader, requestIdHeader);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "accepted"));
    }
}
