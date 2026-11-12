package pay.ecom_payment_be.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OrderServiceClient {
    private final RestClient restClient;

    public OrderServiceClient(RestClient.Builder restClientBuilder,
                              @Value("${services.order.url}") String orderServiceUrl) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(orderServiceUrl)  // direct URL, no Eureka lookup
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
