package com.healthybites.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/** La aplicación no arranca sin un secreto JWT válido (fail fast). */
class JwtPropertiesTests {

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(JwtProperties.class)
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues("healthybites.security.jwt.access-token-ttl=PT15M",
                    "healthybites.security.jwt.issuer=healthy-bites-api",
                    "healthybites.security.jwt.audience=healthy-bites-web");

    private static String randomBase64(int bytes) {
        byte[] key = new byte[bytes];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @Test
    void failsWithoutSecret() {
        runner.run(context -> assertThat(context).hasFailed()
                .getFailure().rootCause().hasMessageContaining("JWT_SECRET"));
    }

    @Test
    void failsWithASecretTooShortForHs512() {
        runner.withPropertyValues("healthybites.security.jwt.secret=" + randomBase64(32))
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("64 bytes"));
    }

    @Test
    void failsWithASecretThatIsNotBase64() {
        runner.withPropertyValues("healthybites.security.jwt.secret=esto no es base64 !!")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void startsWithAStrongSecret() {
        String secret = randomBase64(64);
        runner.withPropertyValues("healthybites.security.jwt.secret=" + secret)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(JwtProperties.class).secret()).isEqualTo(secret);
                });
    }
}
