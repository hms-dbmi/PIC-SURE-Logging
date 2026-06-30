package edu.harvard.dbmi.avillach.logging.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditEvent(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("action") String action,
    @JsonProperty("client_type") String clientType,
    @JsonProperty("caller") String caller,
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("request") RequestInfo request,
    @JsonProperty("metadata") Map<String, Object> metadata,
    @JsonProperty("error") Map<String, Object> error
) {
    /**
     * Backwards-compatible constructor without {@code caller} (defaults to null). Kept so existing callers/tests that predate the
     * top-level caller field continue to compile; Jackson always uses the canonical (annotated) constructor for deserialization.
     */
    public AuditEvent(
        String eventType, String action, String clientType, String sessionId, RequestInfo request, Map<String, Object> metadata,
        Map<String, Object> error
    ) {
        this(eventType, action, clientType, null, sessionId, request, metadata, error);
    }
}
