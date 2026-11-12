package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** {@code purchase_units[].payments} de la respuesta de captura. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payments {
    private List<Capture> captures;
}
