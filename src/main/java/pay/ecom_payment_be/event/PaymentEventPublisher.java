package pay.ecom_payment_be.event;

import com.yourapp.events.payment.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    public static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, event.orderId(), event);
        log.info("Published PaymentProcessedEvent {} for order {} (status={})",
                event.eventId(), event.orderId(), event.status());
    }
}
