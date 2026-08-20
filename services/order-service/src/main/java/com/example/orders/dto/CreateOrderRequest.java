package com.example.orders.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "customerId must not be blank")
    private String customerId;

    @NotNull(message = "totalAmount must not be null")
    @DecimalMin(value = "0.01", message = "totalAmount must be positive")
    private BigDecimal totalAmount;

    @NotBlank(message = "currency must not be blank")
    private String currency;
}
