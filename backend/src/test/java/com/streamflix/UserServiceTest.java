package com.streamflix;

import com.streamflix.dto.RegisterRequest;
import com.streamflix.entity.User;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional      // each test rolls back automatically
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserServiceTest {

    @Autowired private UserService userService;

    @Test
    void register_creates_user_and_hashes_password() {
        RegisterRequest req = new RegisterRequest("alice_us", "alice_us@x.com",
                "password123", "Alice", "VN");
        User u = userService.register(req);

        assertNotNull(u.getUserId());
        assertEquals("alice_us", u.getUsername());
        assertNotEquals("password123", u.getPasswordHash(), "Password must be hashed");
        assertTrue(u.getPasswordHash().startsWith("$2"), "BCrypt hash starts with $2");
        assertEquals(User.Role.USER, u.getRole());
    }

    @Test
    void register_with_duplicate_username_throws() {
        RegisterRequest first = new RegisterRequest("dupe_us", "a_us@x.com",
                "password123", "First", null);
        userService.register(first);

        RegisterRequest second = new RegisterRequest("dupe_us", "b_us@x.com",
                "password456", "Second", null);
        assertThrows(BadRequestException.class, () -> userService.register(second));
    }

    @Test
    void register_with_duplicate_email_throws() {
        userService.register(new RegisterRequest("u1_us", "same_us@x.com",
                "password123", null, null));
        assertThrows(BadRequestException.class, () ->
                userService.register(new RegisterRequest("u2_us", "same_us@x.com",
                        "password456", null, null)));
    }

    @Test
    void findById_unknown_throws() {
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99999L));
    }
}
