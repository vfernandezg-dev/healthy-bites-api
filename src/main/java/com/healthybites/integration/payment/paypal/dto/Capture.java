package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Un cobro capturado: lo que de verdad se ha cobrado (importe y moneda). */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Capture {
    private String id;
    private String status;
    private Amount amount;
}
