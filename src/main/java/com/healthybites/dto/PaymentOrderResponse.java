package com.healthybites.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaymentOrderResponse {
    private String paypalUrl;
}
