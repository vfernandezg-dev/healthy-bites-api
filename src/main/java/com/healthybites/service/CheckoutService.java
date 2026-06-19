package com.healthybites.service;

import com.healthybites.dto.PaymentCaptureResponse;
import com.healthybites.dto.PaymentOrderResponse;
import jakarta.mail.MessagingException;

public interface CheckoutService {

    PaymentOrderResponse createPayment(Integer purchasesId, String returnUrl, String cancelUrl) throws MessagingException;

    PaymentCaptureResponse capturePayment(String orderId) throws MessagingException;
}
