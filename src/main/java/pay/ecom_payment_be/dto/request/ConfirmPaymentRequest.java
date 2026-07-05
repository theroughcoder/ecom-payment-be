package pay.ecom_payment_be.dto.request;

import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    private String clientSecret;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardholderName;
}
