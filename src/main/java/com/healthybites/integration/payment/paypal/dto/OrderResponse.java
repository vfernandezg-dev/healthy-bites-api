package com.healthybites.integration.payment.paypal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderResponse {
    private String id;
    private String status;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    private List<Link> links;
}
