package com.healthybites.billing.web;

import com.healthybites.billing.app.SubscriptionService;
import com.healthybites.billing.web.BillingDtos.CaptureRequest;
import com.healthybites.billing.web.BillingDtos.CheckoutRequest;
import com.healthybites.billing.web.BillingDtos.CheckoutResponse;
import com.healthybites.billing.web.BillingDtos.CurrentSubscriptionResponse;
import com.healthybites.billing.web.BillingDtos.PaymentResponse;
import com.healthybites.billing.web.BillingDtos.PlanResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Suscripción", description = "Planes, checkout con PayPal y pagos del usuario")
class SubscriptionController {

    private static final Set<String> PAYMENT_SORT = Set.of("createdAt", "capturedAt");

    private final SubscriptionService subscriptions;

    SubscriptionController(SubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Operation(summary = "Planes disponibles con su precio")
    @SecurityRequirements
    @GetMapping("/subscription-plans")
    List<PlanResponse> plans() {
        return subscriptions.plans().stream().map(PlanResponse::of).toList();
    }

    @Operation(summary = "Nivel vigente del usuario y la suscripción que lo da")
    @GetMapping("/me/subscription")
    CurrentSubscriptionResponse current() {
        return CurrentSubscriptionResponse.of(subscriptions.current(CurrentUser.id()));
    }

    @Operation(
            summary = "Empieza el pago de un plan",
            description = "Devuelve la URL de PayPal donde el usuario aprueba el pago. PayPal vuelve a "
                    + "/client/subscription/checkout/return (o /cancel) del web-app.")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/me/subscription/checkout")
    CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest body) {
        return new CheckoutResponse(subscriptions.checkout(CurrentUser.id(), body.tier()));
    }

    @Operation(summary = "Captura el pago aprobado y activa la suscripción (idempotente)")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/me/subscription/checkout/capture")
    CurrentSubscriptionResponse capture(@Valid @RequestBody CaptureRequest body) {
        return CurrentSubscriptionResponse.of(subscriptions.capture(CurrentUser.id(), body.orderId()));
    }

    @Operation(summary = "Cancela la renovación; el acceso sigue hasta el fin del período pagado")
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/me/subscription/cancel")
    CurrentSubscriptionResponse cancel() {
        return CurrentSubscriptionResponse.of(subscriptions.cancel(CurrentUser.id()));
    }

    @Operation(summary = "Pagos del usuario")
    @GetMapping("/me/payments")
    PagedModel<PaymentResponse> payments(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        return new PagedModel<>(subscriptions
                .payments(CurrentUser.id(), Sorting.restrict(pageable, PAYMENT_SORT))
                .map(PaymentResponse::of));
    }
}
