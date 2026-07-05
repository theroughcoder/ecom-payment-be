package pay.ecom_payment_be.util;

import java.util.Locale;

/**
 * Renders the actual card-entry UI as a self-contained HTML page that payment-be serves
 * directly. The frontend only embeds this in an iframe — it never builds the form itself,
 * mirroring how a real gateway SDK (Stripe Elements, Razorpay) injects its own hosted UI
 * into the merchant's page. The page's own JS calls back to payment-be's confirm endpoint
 * (same-origin, since the iframe's origin IS payment-be) and reports the result to the
 * parent page via postMessage.
 */
public final class PaymentWidgetHtml {

    private PaymentWidgetHtml() {
    }

    public static String render(String paymentIntentId, String clientSecret, double amount, String currency) {
        String formattedAmount = String.format(Locale.US, "%.2f", amount);
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="utf-8">
                <title>Payment</title>
                <style>
                  * { box-sizing: border-box; }
                  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 16px; background: #ffffff; color: #1a1a1a; }
                  .field { margin-bottom: 12px; }
                  label { display: block; font-size: 12px; color: #555; margin-bottom: 4px; }
                  input { width: 100%%; padding: 10px; border: 1px solid #ccd0d5; border-radius: 6px; font-size: 14px; }
                  input:focus { outline: none; border-color: #6772e5; box-shadow: 0 0 0 2px rgba(103,114,229,0.15); }
                  .row { display: flex; gap: 8px; }
                  .row .field { flex: 1; }
                  button { width: 100%%; padding: 12px; background: #6772e5; color: white; border: none; border-radius: 6px; font-size: 15px; font-weight: 600; cursor: pointer; margin-top: 4px; }
                  button:disabled { opacity: 0.6; cursor: not-allowed; }
                  .error { color: #d64039; font-size: 13px; margin-bottom: 12px; min-height: 16px; }
                  .badge { font-size: 11px; color: #8a8f98; text-align: center; margin-top: 10px; }
                </style>
                </head>
                <body>
                  <form id="payment-form">
                    <div class="error" id="error"></div>
                    <div class="field">
                      <label>Cardholder Name</label>
                      <input id="cardholderName" autocomplete="cc-name" required />
                    </div>
                    <div class="field">
                      <label>Card Number</label>
                      <input id="cardNumber" autocomplete="cc-number" placeholder="4242 4242 4242 4242" required />
                    </div>
                    <div class="row">
                      <div class="field">
                        <label>MM</label>
                        <input id="expiryMonth" autocomplete="cc-exp-month" placeholder="MM" required />
                      </div>
                      <div class="field">
                        <label>YYYY</label>
                        <input id="expiryYear" autocomplete="cc-exp-year" placeholder="YYYY" required />
                      </div>
                      <div class="field">
                        <label>CVV</label>
                        <input id="cvv" autocomplete="cc-csc" placeholder="123" required />
                      </div>
                    </div>
                    <button type="submit" id="submit-btn">Pay %s %s</button>
                    <div class="badge">Test mode &mdash; fake gateway, no real charge</div>
                  </form>
                  <script>
                    var form = document.getElementById('payment-form');
                    var btn = document.getElementById('submit-btn');
                    var errorEl = document.getElementById('error');
                    var submitLabel = btn.textContent;
                    form.addEventListener('submit', function (e) {
                      e.preventDefault();
                      errorEl.textContent = '';
                      btn.disabled = true;
                      btn.textContent = 'Processing...';
                      fetch('/api/payments/intents/%s/confirm', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                          clientSecret: '%s',
                          cardNumber: document.getElementById('cardNumber').value,
                          expiryMonth: document.getElementById('expiryMonth').value,
                          expiryYear: document.getElementById('expiryYear').value,
                          cvv: document.getElementById('cvv').value,
                          cardholderName: document.getElementById('cardholderName').value
                        })
                      }).then(function (res) {
                        return res.json().then(function (body) { return { ok: res.ok, body: body }; });
                      }).then(function (result) {
                        if (result.ok && result.body.status === 'SUCCEEDED') {
                          window.parent.postMessage({ type: 'ecom-payment-result', status: 'SUCCEEDED' }, '*');
                        } else {
                          throw new Error(result.body.message || 'Payment failed');
                        }
                      }).catch(function (err) {
                        errorEl.textContent = err.message;
                        btn.disabled = false;
                        btn.textContent = submitLabel;
                        window.parent.postMessage({ type: 'ecom-payment-result', status: 'FAILED', message: err.message }, '*');
                      });
                    });
                  </script>
                </body>
                </html>
                """.formatted(formattedAmount, currency, paymentIntentId, clientSecret);
    }
}
