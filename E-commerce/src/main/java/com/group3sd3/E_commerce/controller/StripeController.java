package com.group3sd3.E_commerce.controller;

import com.group3sd3.E_commerce.model.StripeReq;
import com.group3sd3.E_commerce.model.StripeRes;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StripeController {
    @PostMapping("/create-payment-intent")
    public StripeRes createPaymentIntent(@RequestBody StripeReq stripeReq) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (stripeReq.getTotalOrderPrice() * 100L)).setCurrency("php")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return new StripeRes(intent.getId(), intent.getClientSecret());
    }
}
