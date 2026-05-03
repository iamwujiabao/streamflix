package com.streamflix;

import com.streamflix.dto.SubscriptionDto;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.repository.*;
import com.streamflix.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionServiceTest {

    @Autowired private SubscriptionService     subscriptionService;
    @Autowired private SubscriptionPlanRepository planRepo;
    @Autowired private UserRepository          userRepo;
    @Autowired private UserSubscriptionRepository userSubRepo;
    @Autowired private PaymentRepository       paymentRepo;
    @Autowired private PasswordEncoder         encoder;

    private User user;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        user = userRepo.save(User.builder()
                .username("subtest_" + suffix).email("s_" + suffix + "@x.com")
                .passwordHash(encoder.encode("pw")).role(User.Role.USER)
                .isActive(true).build());
        plan = planRepo.save(SubscriptionPlan.builder()
                .planName("Premium_" + suffix).price(new BigDecimal("15.99"))
                .maxQuality("4K").maxDevices(4).description("4K plan").build());
    }

    @Test
    void subscribe_creates_active_subscription_and_completed_payment() {
        SubscriptionDto.Response sub = subscriptionService.subscribe(
                user.getUserId(), plan.getPlanId(), 3, Payment.Method.CREDIT_CARD);
        assertEquals(UserSubscription.Status.ACTIVE.name(), sub.status());
        assertEquals(plan.getPlanId(), sub.planId());
        List<Payment> payments = paymentRepo.findAll();
        assertFalse(payments.isEmpty());
        Payment last = payments.get(payments.size()-1);
        assertEquals(Payment.Status.COMPLETED, last.getStatus());
        assertEquals(0, new BigDecimal("47.97").compareTo(last.getAmount()));
    }

    @Test
    void second_subscribe_cancels_the_first() {
        SubscriptionDto.Response first = subscriptionService.subscribe(
                user.getUserId(), plan.getPlanId(), 1, Payment.Method.PAYPAL);
        SubscriptionDto.Response second = subscriptionService.subscribe(
                user.getUserId(), plan.getPlanId(), 6, Payment.Method.CREDIT_CARD);
        UserSubscription reloadedFirst = userSubRepo.findById(first.subscriptionId()).orElseThrow();
        assertEquals(UserSubscription.Status.CANCELLED, reloadedFirst.getStatus());
        assertEquals(UserSubscription.Status.ACTIVE.name(), second.status());
    }

    @Test
    void subscribe_with_zero_months_throws() {
        assertThrows(BadRequestException.class, () ->
                subscriptionService.subscribe(user.getUserId(), plan.getPlanId(), 0, Payment.Method.PAYPAL));
    }

    @Test
    void cancel_marks_subscription_as_cancelled() {
        SubscriptionDto.Response sub = subscriptionService.subscribe(
                user.getUserId(), plan.getPlanId(), 1, Payment.Method.MOMO);
        subscriptionService.cancel(sub.subscriptionId(), user.getUserId());
        UserSubscription reloaded = userSubRepo.findById(sub.subscriptionId()).orElseThrow();
        assertEquals(UserSubscription.Status.CANCELLED, reloaded.getStatus());
        assertFalse(reloaded.getAutoRenew());
    }

    @Test
    void cancel_anothers_subscription_throws() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User other = userRepo.save(User.builder()
                .username("other_" + suffix).email("other_" + suffix + "@x.com")
                .passwordHash(encoder.encode("pw")).role(User.Role.USER)
                .isActive(true).build());
        SubscriptionDto.Response sub = subscriptionService.subscribe(
                user.getUserId(), plan.getPlanId(), 1, Payment.Method.MOMO);
        assertThrows(BadRequestException.class, () ->
                subscriptionService.cancel(sub.subscriptionId(), other.getUserId()));
    }
}
