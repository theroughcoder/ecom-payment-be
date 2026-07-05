package pay.ecom_payment_be.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OrderServiceClient {
    private final RestClient restClient;

    public OrderServiceClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://ECOM-ORDER-BE")  // Eureka service name
                .build();
    }

    public void markOrderPaid(String orderId, String paymentIntentId) {
        restClient.put()
                .uri("/api/orders/{id}/pay", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("paymentIntentId", paymentIntentId))
                .retrieve()
                .toBodilessEntity();
    }
}
