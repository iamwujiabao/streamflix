package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.SubscriptionDto;
import com.streamflix.service.SubscriptionService;
import com.streamflix.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions and Plans")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService         userService;

    @GetMapping("/plans")
    public ApiResponse<List<SubscriptionDto.PlanResponse>> plans() {
        return ApiResponse.ok(subscriptionService.listPlans());
    }

    @PostMapping("/subscribe")
    public ApiResponse<SubscriptionDto.Response> subscribe(
            @Valid @RequestBody SubscriptionDto.SubscribeRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok("Subscribed",
                subscriptionService.subscribe(userId, req.planId(),
                        req.months() != null ? req.months() : 1,
                        req.paymentMethod()));
    }

    @GetMapping("/me")
    public ApiResponse<List<SubscriptionDto.Response>> mySubscriptions(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(subscriptionService.getMySubscriptions(userId));
    }

    @GetMapping("/me/active")
    public ApiResponse<SubscriptionDto.Response> active(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(subscriptionService.getActive(userId).orElse(null));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Object> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        subscriptionService.cancel(id, userId);
        return ApiResponse.ok("Cancelled", null);
    }
}
