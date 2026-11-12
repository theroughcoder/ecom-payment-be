package pay.ecom_payment_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Plain (non-load-balanced) builder: clients call services directly by URL, bypassing Eureka.
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
