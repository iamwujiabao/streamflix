package com.streamflix.dto;

import com.streamflix.entity.Payment;
import com.streamflix.entity.SubscriptionPlan;
import com.streamflix.entity.UserSubscription;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionDto {

    public record PlanResponse(
            Integer planId, String name, BigDecimal price,
            String maxQuality, Integer maxDevices, String description) {
        public static PlanResponse fromEntity(SubscriptionPlan p) {
            return new PlanResponse(p.getPlanId(), p.getPlanName(), p.getPrice(),
                    p.getMaxQuality(), p.getMaxDevices(), p.getDescription());
        }
    }

    public record SubscribeRequest(
            @NotNull Integer planId,
            @Min(1) @Max(24) Integer months,
            @NotNull Payment.Method paymentMethod) {}

    public record Response(
            Long subscriptionId, Integer planId, String planName,
            LocalDate startDate, LocalDate endDate, String status, Boolean autoRenew) {
        public static Response fromEntity(UserSubscription s) {
            return new Response(s.getSubscriptionId(),
                    s.getPlan().getPlanId(),
                    s.getPlan().getPlanName(),
                    s.getStartDate(), s.getEndDate(),
                    s.getStatus().name(), s.getAutoRenew());
        }
    }
}
