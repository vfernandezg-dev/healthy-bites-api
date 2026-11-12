package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderCaptureResponse {
    private String status;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;
}
