package com.streamflix;

import com.streamflix.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret",
                "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtMzItYnl0ZXMtbG9uZyEhIQ==");
        ReflectionTestUtils.setField(provider, "expirationMs", 60_000L);
        provider.init();
    }

    @Test
    void generated_token_is_valid_and_round_trips_username_and_role() {
        String token = provider.generateToken("alice", "USER");
        assertTrue(provider.isValid(token));
        assertEquals("alice", provider.getUsername(token));
        assertEquals("USER",  provider.getRole(token));
    }

    @Test
    void garbage_token_is_rejected() {
        assertFalse(provider.isValid("not-a-token"));
        assertFalse(provider.isValid(""));
    }

    @Test
    void expired_token_is_rejected() throws InterruptedException {
        // Set expiration to 1 ms — token expires immediately
        ReflectionTestUtils.setField(provider, "expirationMs", 1L);
        String token = provider.generateToken("alice", "USER");
        Thread.sleep(50);
        assertFalse(provider.isValid(token));
    }
}
