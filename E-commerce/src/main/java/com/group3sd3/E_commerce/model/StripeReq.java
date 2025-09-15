package com.group3sd3.E_commerce.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StripeReq {
    @Email
    private String email;

    @NotNull
    private Double totalOrderPrice;

    private Integer userId;

}
