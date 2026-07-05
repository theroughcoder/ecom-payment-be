package pay.ecom_payment_be.dto.request;

import lombok.Data;

@Data
public class CreatePaymentIntentRequest {
    private String orderId;
    private double amount;
    private String currency;
}
