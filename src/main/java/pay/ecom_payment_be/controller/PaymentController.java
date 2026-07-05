package pay.ecom_payment_be.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pay.ecom_payment_be.dto.request.ConfirmPaymentRequest;
import pay.ecom_payment_be.dto.request.CreatePaymentIntentRequest;
import pay.ecom_payment_be.dto.response.PaymentIntentResponse;
import pay.ecom_payment_be.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intents")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentIntentResponse createIntent(@RequestBody CreatePaymentIntentRequest request) {
        return paymentService.createIntent(request);
    }

    @PostMapping("/intents/{id}/confirm")
    public PaymentIntentResponse confirmPayment(@PathVariable String id, @RequestBody ConfirmPaymentRequest request) {
        return paymentService.confirmPayment(id, request);
    }

    // Served by payment-be and embedded by the frontend in an iframe — the actual card
    // form markup comes from here, not from the frontend, mirroring a real gateway's
    // embedded SDK (e.g. Stripe Elements is itself an iframe Stripe serves).
    @GetMapping(value = "/intents/{id}/widget", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String renderWidget(@PathVariable String id, @RequestParam String clientSecret) {
        return paymentService.renderWidget(id, clientSecret);
    }
}
