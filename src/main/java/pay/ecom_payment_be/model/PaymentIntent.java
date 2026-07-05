package pay.ecom_payment_be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_intents")
@Data
public class PaymentIntent {

    @Id
    private String id;

    @Column(name = "order_id")
    private String orderId;

    private double amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentIntentStatus status = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD;

    private String clientSecret;
    private String cardLast4;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
