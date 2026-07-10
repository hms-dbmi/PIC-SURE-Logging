package edu.harvard.dbmi.avillach.logging.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditJsonConfig {

    private static final int MAX_NESTING_DEPTH = 10;
    private static final int MAX_STRING_LENGTH = 10_240;

    /**
     * A dedicated mapper for parsing /audit bodies. Declaring an ObjectMapper bean here
     * trips Boot's JacksonAutoConfiguration @ConditionalOnMissingBean, so Boot backs off
     * and this bean becomes the sole ObjectMapper in the context, including the one
     * MappingJackson2HttpMessageConverter uses for response serialization. That is safe:
     * StreamReadConstraints govern reading only, so responses are unaffected, and the only
     * other Jackson read path (AuditController) qualifies this bean explicitly by name.
     */
    @Bean("auditObjectMapper")
    public ObjectMapper auditObjectMapper() {
        JsonFactory factory = JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder()
                .maxNestingDepth(MAX_NESTING_DEPTH)
                .maxStringLength(MAX_STRING_LENGTH)
                .build())
            .build();
        return new ObjectMapper(factory);
    }
}
