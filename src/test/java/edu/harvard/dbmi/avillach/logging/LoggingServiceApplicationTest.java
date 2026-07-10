package edu.harvard.dbmi.avillach.logging;

import edu.harvard.dbmi.avillach.logging.config.JwtClaimMappingConverter;
import edu.harvard.dbmi.avillach.logging.config.LoggingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "picsure.logging.api-key=test-key")
class LoggingServiceApplicationTest {

    @Autowired
    private LoggingProperties properties;

    @Test
    void contextLoadsAndBindsProperties() {
        assertThat(properties.apiKey()).isEqualTo("test-key");
        assertThat(properties.app()).isEqualTo("unknown");
        assertThat(properties.allowedOrigin()).isEqualTo("*");
    }

    @Test
    void blankJwtClaimMappingEnvBindsToTheDefaultMap() {
        // application.yml supplies "" via ${JWT_CLAIM_MAPPING:}; the converter turns it
        // into the default map. This proves the @ConfigurationPropertiesBinding wiring works.
        assertThat(properties.jwtClaimMapping()).isEqualTo(JwtClaimMappingConverter.DEFAULT_MAPPING);
    }
}
