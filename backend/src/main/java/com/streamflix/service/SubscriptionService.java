package com.streamflix.service;

import com.streamflix.dto.SubscriptionDto;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.PaymentRepository;
import com.streamflix.repository.SubscriptionPlanRepository;
import com.streamflix.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionPlanRepository  planRepository;
    private final UserSubscriptionRepository  subscriptionRepository;
    private final PaymentRepository           paymentRepository;
    private final UserService                 userService;

    @Transactional(readOnly = true)
    public List<SubscriptionDto.PlanResponse> listPlans() {
        return planRepository.findAll().stream()
                .map(SubscriptionDto.PlanResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan findPlan(Integer planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }

    public SubscriptionDto.Response subscribe(Long userId, Integer planId, int months,
                                               Payment.Method method) {
        User user = userService.findById(userId);
        SubscriptionPlan plan = findPlan(planId);

        if (months <= 0 || months > 24)
            throw new BadRequestException("Subscription must be between 1 and 24 months");

        subscriptionRepository.findFirstByUserUserIdAndStatusOrderByEndDateDesc(
                userId, UserSubscription.Status.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(UserSubscription.Status.CANCELLED);
                    subscriptionRepository.save(active);
                });

        LocalDate start = LocalDate.now();
        UserSubscription sub = UserSubscription.builder()
                .user(user).plan(plan)
                .startDate(start).endDate(start.plusMonths(months))
                .status(UserSubscription.Status.ACTIVE)
                .autoRenew(true).build();
        subscriptionRepository.save(sub);

        Payment payment = Payment.builder()
                .subscription(sub)
                .amount(plan.getPrice().multiply(java.math.BigDecimal.valueOf(months)))
                .method(method).status(Payment.Status.COMPLETED)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
        paymentRepository.save(payment);

        return SubscriptionDto.Response.fromEntity(sub);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto.Response> getMySubscriptions(Long userId) {
        return subscriptionRepository.findByUserUserId(userId).stream()
                .map(SubscriptionDto.Response::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionDto.Response> getActive(Long userId) {
        return subscriptionRepository.findFirstByUserUserIdAndStatusOrderByEndDateDesc(
                userId, UserSubscription.Status.ACTIVE)
                .map(SubscriptionDto.Response::fromEntity);
    }

    public void cancel(Long subscriptionId, Long requestingUserId) {
        UserSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));
        if (!sub.getUser().getUserId().equals(requestingUserId))
            throw new BadRequestException("Cannot cancel another user's subscription");
        sub.setStatus(UserSubscription.Status.CANCELLED);
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
    }
}
