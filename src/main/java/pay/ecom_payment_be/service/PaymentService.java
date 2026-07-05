package pay.ecom_payment_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pay.ecom_payment_be.client.OrderServiceClient;
import pay.ecom_payment_be.dto.request.ConfirmPaymentRequest;
import pay.ecom_payment_be.dto.request.CreatePaymentIntentRequest;
import pay.ecom_payment_be.dto.response.PaymentIntentResponse;
import pay.ecom_payment_be.model.PaymentIntent;
import pay.ecom_payment_be.model.PaymentIntentStatus;
import pay.ecom_payment_be.repository.PaymentIntentRepository;
import pay.ecom_payment_be.util.PaymentWidgetHtml;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("\\d{13,19}");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\d{3,4}");
    private static final String SECRET_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentIntentRepository paymentIntentRepository;
    private final OrderServiceClient orderServiceClient;

    public PaymentIntentResponse createIntent(CreatePaymentIntentRequest request) {
        PaymentIntent intent = new PaymentIntent();
        intent.setId(UUID.randomUUID().toString());
        intent.setOrderId(request.getOrderId());
        intent.setAmount(request.getAmount());
        intent.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        intent.setClientSecret(intent.getId() + "_secret_" + randomAlphanumeric(24));

        PaymentIntent saved = paymentIntentRepository.save(intent);
        log.info("Created payment intent {} for order {}", saved.getId(), saved.getOrderId());
        return mapToResponse(saved);
    }

    public PaymentIntentResponse confirmPayment(String id, ConfirmPaymentRequest request) {
        PaymentIntent intent = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment intent not found"));

        if (!intent.getClientSecret().equals(request.getClientSecret())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid client secret");
        }

        if (intent.getStatus() == PaymentIntentStatus.SUCCEEDED) {
            return mapToResponse(intent);
        }

        validateCardShape(request);

        intent.setStatus(PaymentIntentStatus.SUCCEEDED);
        intent.setPaidAt(LocalDateTime.now());
        intent.setCardLast4(lastFourDigits(request.getCardNumber()));
        PaymentIntent saved = paymentIntentRepository.save(intent);

        try {
            orderServiceClient.markOrderPaid(saved.getOrderId(), saved.getId());
        } catch (Exception e) {
            log.error("Payment {} succeeded but failed to notify order-service for order {}", saved.getId(), saved.getOrderId(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Payment captured but order confirmation failed; please refresh the order page");
        }

        log.info("Payment intent {} confirmed for order {}", saved.getId(), saved.getOrderId());
        return mapToResponse(saved);
    }

    public String renderWidget(String id, String clientSecret) {
        PaymentIntent intent = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment intent not found"));
        if (!intent.getClientSecret().equals(clientSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid client secret");
        }
        return PaymentWidgetHtml.render(intent.getId(), intent.getClientSecret(), intent.getAmount(), intent.getCurrency());
    }

    private void validateCardShape(ConfirmPaymentRequest request) {
        String cardNumber = request.getCardNumber() != null ? request.getCardNumber().replace(" ", "") : "";
        if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number is invalid");
        }
        if (request.getCvv() == null || !CVV_PATTERN.matcher(request.getCvv()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CVV is invalid");
        }
        if (request.getCardholderName() == null || request.getCardholderName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cardholder name is required");
        }
        YearMonth expiry = parseExpiry(request.getExpiryMonth(), request.getExpiryYear());
        if (expiry.isBefore(YearMonth.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card has expired");
        }
    }

    private YearMonth parseExpiry(String month, String year) {
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            if (y < 100) {
                y += 2000;
            }
            return YearMonth.of(y, m);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry date is invalid");
        }
    }

    private String lastFourDigits(String cardNumber) {
        String digits = cardNumber.replace(" ", "");
        return digits.substring(digits.length() - 4);
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECRET_CHARS.charAt(RANDOM.nextInt(SECRET_CHARS.length())));
        }
        return sb.toString();
    }

    private PaymentIntentResponse mapToResponse(PaymentIntent intent) {
        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setId(intent.getId());
        response.setOrderId(intent.getOrderId());
        response.setAmount(intent.getAmount());
        response.setCurrency(intent.getCurrency());
        response.setStatus(intent.getStatus());
        response.setClientSecret(intent.getClientSecret());
        response.setCardLast4(intent.getCardLast4());
        response.setCreatedAt(intent.getCreatedAt());
        response.setPaidAt(intent.getPaidAt());
        return response;
    }
}
