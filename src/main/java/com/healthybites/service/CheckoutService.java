package com.healthybites.service;

import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;

public interface CheckoutService {

    PaymentOrderResponse createPayment(Integer purchasesId, String returnUrl, String cancelUrl);

    PaymentCaptureResponse capturePayment(String orderId);
}
