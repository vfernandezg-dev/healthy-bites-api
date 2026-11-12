package com.healthybites.billing.web;

import com.healthybites.billing.Tier;
import com.healthybites.billing.app.BillingAdminService;
import com.healthybites.billing.domain.Payment;
import com.healthybites.billing.domain.PaymentStatus;
import com.healthybites.billing.domain.Subscription;
import com.healthybites.billing.domain.SubscriptionStatus;
import com.healthybites.billing.web.BillingDtos.AdminPaymentResponse;
import com.healthybites.billing.web.BillingDtos.AdminSubscriptionResponse;
import com.healthybites.billing.web.BillingDtos.BillingSummaryResponse;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
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
    private final UserAccounts accounts;

    AdminBillingController(BillingAdminService billing, UserAccounts accounts) {
        this.billing = billing;
        this.accounts = accounts;
    }

    @Operation(summary = "Suscripciones por estado y nivel")
    @GetMapping("/subscriptions")
    PagedModel<AdminSubscriptionResponse> subscriptions(
            @RequestParam(required = false) @Nullable SubscriptionStatus status,
            @RequestParam(required = false) @Nullable Tier tier,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Subscription> page = billing.subscriptions(status, tier, Sorting.restrict(pageable, SUBSCRIPTION_SORT));
        Map<UUID, UserAccount> people = people(page, Subscription::getUserId);
        return new PagedModel<>(page.map(subscription -> AdminSubscriptionResponse.of(subscription, people)));
    }

    @Operation(summary = "Pagos por estado")
    @GetMapping("/payments")
    PagedModel<AdminPaymentResponse> payments(
            @RequestParam(required = false) @Nullable PaymentStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Payment> page = billing.payments(status, Sorting.restrict(pageable, PAYMENT_SORT));
        Map<UUID, UserAccount> people = people(page, Payment::getUserId);
        return new PagedModel<>(page.map(payment -> AdminPaymentResponse.of(payment, people)));
    }

    @Operation(summary = "Suscripciones vigentes por nivel, ingreso mensual recurrente e ingresos de 30 días")
    @GetMapping("/billing/summary")
    BillingSummaryResponse summary() {
        return BillingSummaryResponse.of(billing.summary());
    }

    /** Titulares de una página de resultados, en una sola consulta. */
    private <T> Map<UUID, UserAccount> people(Page<T> page, Function<T, UUID> userId) {
        return accounts.findAllById(
                page.getContent().stream().map(userId).distinct().toList());
    }
}
