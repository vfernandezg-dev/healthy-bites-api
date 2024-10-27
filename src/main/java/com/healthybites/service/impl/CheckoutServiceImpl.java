package com.healthybites.service.impl;

import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.integration.payment.paypal.dto.OrderCaptureResponse;
import com.healthybites.integration.payment.paypal.dto.OrderResponse;
import com.healthybites.integration.payment.paypal.service.PayPalService;
import com.healthybites.service.CheckoutService;
import com.healthybites.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final PayPalService payPalService;
    private final SuscripcionService suscripcionService;

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
    public PaymentCaptureResponse capturePayment(String orderId) {
        OrderCaptureResponse orderCaptureResponse = payPalService.captureOrder(orderId);
        boolean completed = orderCaptureResponse.getStatus().equals("COMPLETED");

        PaymentCaptureResponse paypalCaptureResponse = new PaymentCaptureResponse();
        paypalCaptureResponse.setCompleted(completed);

        if (completed) {
            String purchaseIdStr = orderCaptureResponse.getPurchaseUnits().get(0).getReferenceId();
            SuscripcionDetailsDTO suscripcionDetailsDTO = suscripcionService.confirmSuscripcion(Integer.parseInt(purchaseIdStr));
            paypalCaptureResponse.setSuscripcionId(suscripcionDetailsDTO.getId());

        }
        return paypalCaptureResponse;
    }
}
