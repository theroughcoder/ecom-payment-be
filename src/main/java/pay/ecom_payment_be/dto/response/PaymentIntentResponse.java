package pay.ecom_payment_be.dto.response;

import lombok.Data;
import pay.ecom_payment_be.model.PaymentIntentStatus;

import java.time.LocalDateTime;

@Data
public class PaymentIntentResponse {
    private String id;
    private String orderId;
    private double amount;
    private String currency;
    private PaymentIntentStatus status;
    private String clientSecret;
    private String cardLast4;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
