package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrderRequest {
    private String intent;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    @JsonProperty("application_context")
    private ApplicationContext applicationContext;
}
