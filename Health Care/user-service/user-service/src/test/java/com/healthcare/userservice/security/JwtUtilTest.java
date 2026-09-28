package com.healthcare.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "TestSecretKeyForHs256SigningThatIsAtLeastThirtyTwoBytesLong!!");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3_600_000L);
        jwtUtil.init();
    }

    @Test
    void generatedTokenCarriesUsernameAndRole() {
        String token = jwtUtil.generateToken("dr.smith", "DOCTOR");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("dr.smith");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("DOCTOR");
    }

    @Test
    void tokenIsValidForItsOwnSubjectOnly() {
        String token = jwtUtil.generateToken("dr.smith", "DOCTOR");

        assertThat(jwtUtil.isTokenValid(token, "dr.smith")).isTrue();
        assertThat(jwtUtil.isTokenValid(token, "nurse.amy")).isFalse();
    }
}
