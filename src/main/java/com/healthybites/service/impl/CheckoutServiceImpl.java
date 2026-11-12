package com.healthybites.service.impl;

import com.healthybites.config.properties.AppProperties;
import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ConflictException;
import com.healthybites.exception.PaymentProviderException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.integration.payment.paypal.dto.Capture;
import com.healthybites.integration.payment.paypal.dto.Link;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.integration.payment.paypal.dto.PurchaseUnit;
import com.healthybites.integration.payment.paypal.service.PayPalService;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.EstadoPago;
import com.healthybites.repository.SuscripcionRepository;
import com.healthybites.security.CurrentUser;
import com.healthybites.service.CheckoutService;
import com.healthybites.service.checkout.SubscriptionPaid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Pago de suscripciones con PayPal.
 *
 * <ul>
 *   <li>Las URLs de vuelta las construye el servidor con {@code APP_FRONTEND_URL}. Antes las enviaba el cliente, y
 *       PayPal podía devolver al usuario a cualquier sitio (redirección abierta).</li>
 *   <li>La captura solo acepta pedidos creados para una suscripción del usuario actual, comprueba el importe y la
 *       moneda cobrados y es idempotente.</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CheckoutServiceImpl implements CheckoutService {

    static final String RETURN_PATH = "cliente/suscripciones";
    static final String CANCEL_PATH = "cliente/suscripciones?checkout=cancelado";
    private static final Set<String> APPROVAL_RELS = Set.of("approve", "payer-action");

    private final PayPalService payPalService;
    private final SuscripcionRepository suscripcionRepository;
    private final AppProperties appProperties;
    private final ApplicationEventPublisher events;

    @Override
    public PaymentOrderResponse createPayment(Integer suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .filter(CheckoutServiceImpl::ownedByCurrentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada"));
        if (suscripcion.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ConflictException("Esta suscripción ya está pagada");
        }
        if (suscripcion.getPrecio() <= 0) {
            throw new BadRequestException("La suscripción básica es gratuita: no necesita pago");
        }

        OrderResponse order = payPalService.createOrder(suscripcion,
                appProperties.frontend(RETURN_PATH), appProperties.frontend(CANCEL_PATH));
        String approvalUrl = approvalUrl(order);

        // El pedido queda ligado a la suscripción: la captura solo acepta este id (y para su dueño).
        suscripcion.setPaypalOrderId(order.getId());
        suscripcionRepository.save(suscripcion);
        return new PaymentOrderResponse(approvalUrl);
    }

    @Override
    @Transactional
    public PaymentCaptureResponse capturePayment(String orderId) {
        // Bloquea la fila: dos capturas simultáneas (doble clic, dos pestañas) se ejecutan una tras otra.
        Suscripcion suscripcion = suscripcionRepository.findByPaypalOrderIdForUpdate(orderId)
                .filter(CheckoutServiceImpl::ownedByCurrentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        PaymentCaptureResponse response = new PaymentCaptureResponse();
        response.setSuscripcionId(suscripcion.getId());
        if (suscripcion.getEstadoPago() == EstadoPago.PAGADO) {
            // Ya capturado (recarga de la página de vuelta, doble clic...): no se cobra ni se avisa otra vez.
            response.setCompleted(true);
            return response;
        }

        OrderCaptureResponse capture = payPalService.captureOrder(orderId);
        if (!"COMPLETED".equals(capture.getStatus())) {
            response.setCompleted(false);
            return response;
        }
        verifyCapture(orderId, capture, suscripcion);

        suscripcion.setEstadoPago(EstadoPago.PAGADO);
        suscripcionRepository.save(suscripcion);
        events.publishEvent(new SubscriptionPaid(suscripcion.getUsuario().getCorreo(),
                suscripcion.getTipoSuscripcion().name(), PayPalService.formatAmount(suscripcion.getPrecio())));
        response.setCompleted(true);
        return response;
    }

    private static boolean ownedByCurrentUser(Suscripcion suscripcion) {
        return suscripcion.getUsuario() != null && Objects.equals(suscripcion.getUsuario().getId(), CurrentUser.id());
    }

    /** Lo cobrado tiene que ser exactamente lo que vale la suscripción, en la moneda del pedido y para ella. */
    private static void verifyCapture(String orderId, OrderCaptureResponse capture, Suscripcion suscripcion) {
        PurchaseUnit unit = Optional.ofNullable(capture.getPurchaseUnits()).flatMap(units -> units.stream().findFirst())
                .orElseThrow(() -> mismatch(orderId, "sin purchase_units"));
        if (!suscripcion.getId().toString().equals(unit.getReferenceId())) {
            throw mismatch(orderId, "reference_id " + unit.getReferenceId() + " para la suscripción " + suscripcion.getId());
        }
        List<Capture> captures = unit.getPayments() == null ? List.of() : unit.getPayments().getCaptures();
        Capture captured = captures == null ? null : captures.stream().findFirst().orElse(null);
        String expected = PayPalService.formatAmount(suscripcion.getPrecio());
        if (captured == null || captured.getAmount() == null
                || !PayPalService.CURRENCY.equals(captured.getAmount().getCurrencyCode())
                || !expected.equals(captured.getAmount().getValue())) {
            throw mismatch(orderId, "importe cobrado distinto de " + expected + " " + PayPalService.CURRENCY);
        }
    }

    private static PaymentProviderException mismatch(String orderId, String detail) {
        // No debería pasar nunca (el importe lo fija el servidor). Si pasa, hay que revisar el cobro a mano.
        log.error("Captura de PayPal {} que no cuadra con la suscripción: {}", orderId, detail);
        return new PaymentProviderException("El pago no coincide con la suscripción. Contacta con soporte.");
    }

    /** URL de aprobación de PayPal; si no es de paypal.com por HTTPS, no se entrega al navegador. */
    private static String approvalUrl(OrderResponse order) {
        String href = Optional.ofNullable(order.getLinks()).orElse(List.of()).stream()
                .filter(link -> link.getRel() != null && APPROVAL_RELS.contains(link.getRel().toLowerCase(Locale.ROOT)))
                .map(Link::getHref)
                .findFirst()
                .orElseThrow(() -> new PaymentProviderException("PayPal no devolvió el enlace de pago."));
        if (order.getId() == null || !isPayPalUrl(href)) {
            throw new PaymentProviderException("PayPal devolvió un enlace de pago no válido.");
        }
        return href;
    }

    static boolean isPayPalUrl(String href) {
        try {
            URI uri = URI.create(href);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return "https".equalsIgnoreCase(uri.getScheme()) && (host.equals("paypal.com") || host.endsWith(".paypal.com"));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
