package com.healthybites.billing.web;

import com.healthybites.billing.Tier;
import com.healthybites.billing.app.BillingAdminService;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.billing.web.BillingDtos.AdminPaymentResponse;
import com.healthybites.billing.web.BillingDtos.AdminSubscriptionResponse;
import com.healthybites.billing.web.BillingDtos.BillingSummaryResponse;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin · Facturación", description = "Suscripciones, pagos y resumen de ingresos (solo ADMIN)")
class AdminBillingController {

    private static final Set<String> SUBSCRIPTION_SORT = Set.of("createdAt", "currentPeriodEnd");
    private static final Set<String> PAYMENT_SORT = Set.of("createdAt", "capturedAt");

    private final BillingAdminService billing;

    AdminBillingController(BillingAdminService billing) {
        this.billing = billing;
    }

    @Operation(summary = "Suscripciones por estado y nivel")
    @GetMapping("/subscriptions")
    PagedModel<AdminSubscriptionResponse> subscriptions(
            @RequestParam(required = false) @Nullable SubscriptionStatus status,
            @RequestParam(required = false) @Nullable Tier tier,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(billing.subscriptions(status, tier, Sorting.restrict(pageable, SUBSCRIPTION_SORT))
                .map(AdminSubscriptionResponse::of));
    }

    @Operation(summary = "Pagos por estado")
    @GetMapping("/payments")
    PagedModel<AdminPaymentResponse> payments(
            @RequestParam(required = false) @Nullable PaymentStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(billing.payments(status, Sorting.restrict(pageable, PAYMENT_SORT))
                .map(AdminPaymentResponse::of));
    }

    @Operation(summary = "Suscripciones vigentes por nivel, ingreso mensual recurrente e ingresos de 30 días")
    @GetMapping("/billing/summary")
    BillingSummaryResponse summary() {
        return BillingSummaryResponse.of(billing.summary());
    }
}
