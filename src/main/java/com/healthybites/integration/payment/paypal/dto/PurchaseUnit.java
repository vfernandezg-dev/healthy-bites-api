package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PurchaseUnit {
    @JsonProperty("reference_id")
    private String referenceId;
    private Amount amount;
    private Payments payments;
}
