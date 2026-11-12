package com.healthybites.service.impl;

import com.healthybites.security.CurrentUser;
import com.healthybites.config.properties.AppProperties;
import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.integration.notification.email.dto.Mail;
import com.healthybites.integration.notification.email.service.EmailService;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.integration.payment.paypal.service.PayPalService;
import com.healthybites.service.CheckoutService;
import com.healthybites.service.SuscripcionService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final PayPalService payPalService;
    private final SuscripcionService suscripcionService;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public PaymentOrderResponse createPayment(Integer purchasesId, String returnUrl, String cancelUrl) {
        OrderResponse orderResponse = payPalService.createOrder(purchasesId, returnUrl, cancelUrl);

        String paypalUrl = orderResponse
                .getLinks()
                .stream()
                .filter(link -> link.getRel().equals("approve"))
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getHref();

        return new PaymentOrderResponse(paypalUrl);
    }

    @Override
    public PaymentCaptureResponse capturePayment(String orderId) throws MessagingException {
        OrderCaptureResponse orderCaptureResponse = payPalService.captureOrder(orderId);
        boolean completed = orderCaptureResponse.getStatus().equals("COMPLETED");

        PaymentCaptureResponse paypalCaptureResponse = new PaymentCaptureResponse();
        paypalCaptureResponse.setCompleted(completed);

        if (completed) {
            String purchaseIdStr = orderCaptureResponse.getPurchaseUnits().get(0).getReferenceId();
            SuscripcionDetailsDTO suscripcionDetailsDTO = suscripcionService.confirmSuscripcion(Integer.parseInt(purchaseIdStr));
            paypalCaptureResponse.setSuscripcionId(suscripcionDetailsDTO.getId());

            sendPurchaseConfirmationEmail(suscripcionDetailsDTO);
        }
        return paypalCaptureResponse;
    }

    private void sendPurchaseConfirmationEmail(SuscripcionDetailsDTO suscripcionDetailsDTO) throws MessagingException {

        // El nombre del principal ahora es el id del usuario; el correo viaja en el access token.
        String userEmail = CurrentUser.email();


        Map<String, Object> model = new HashMap<>();
        model.put("user", userEmail);
        model.put("total", suscripcionDetailsDTO.getPrecio());
        // El web-app muestra la suscripción en el perfil del cliente.
        model.put("orderUrl", appProperties.frontend("cliente/perfil"));

        Mail mail = emailService.createMail(
                userEmail,
                "Confirmación de Compra",
                model,
                mailFrom
        );
        emailService.sendEmail(mail,"email/subscription-confirmation-template");
    }
}
