package pay.ecom_payment_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pay.ecom_payment_be.model.PaymentIntent;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, String> {
}
