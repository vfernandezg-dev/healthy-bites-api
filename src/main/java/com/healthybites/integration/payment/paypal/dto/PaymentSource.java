package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PaymentSource {
    private Paypal paypal;
}
