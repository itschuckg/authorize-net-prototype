package com.authorizenet.prototype.controller;

import com.authorizenet.prototype.config.AuthorizeNetConfig;
import com.authorizenet.prototype.model.PaymentResponse;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/paypal")
public class PayPalController {

    private static final Logger logger = LoggerFactory.getLogger(PayPalController.class);

    private final AuthorizeNetConfig config;

    public PayPalController(AuthorizeNetConfig config) {
        this.config = config;
    }

    /**
     * Step 1: Initiate PayPal Express Checkout — creates an auth-only transaction
     * and returns the PayPal redirect URL.
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiate(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String returnUrl = (String) request.get("returnUrl");
        String cancelUrl = (String) request.get("cancelUrl");

        Environment environment = config.isSandbox() ? Environment.SANDBOX : Environment.PRODUCTION;

        MerchantAuthenticationType merchantAuth = new MerchantAuthenticationType();
        merchantAuth.setName(config.getApiLoginId());
        merchantAuth.setTransactionKey(config.getTransactionKey());

        // PayPal payment type
        PayPalType payPal = new PayPalType();
        payPal.setSuccessUrl(returnUrl);
        payPal.setCancelUrl(cancelUrl);
        payPal.setPayflowcolor("00C0FF");

        PaymentType paymentType = new PaymentType();
        paymentType.setPayPal(payPal);

        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_ONLY_TRANSACTION.value());
        transactionRequest.setAmount(amount);
        transactionRequest.setPayment(paymentType);

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setMerchantAuthentication(merchantAuth);
        apiRequest.setTransactionRequest(transactionRequest);

        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();

        if (response != null
                && response.getMessages().getResultCode() == MessageTypeEnum.OK
                && response.getTransactionResponse() != null) {

            TransactionResponse txnResponse = response.getTransactionResponse();
            // The secure acceptance URL from PayPal is in the secureAcceptance field
            String redirectUrl = null;
            if (txnResponse.getSecureAcceptance() != null) {
                redirectUrl = txnResponse.getSecureAcceptance().getSecureAcceptanceUrl();
            }

            if (redirectUrl != null) {
                return ResponseEntity.ok(Map.of(
                        "redirectUrl", redirectUrl,
                        "transactionId", txnResponse.getTransId()
                ));
            }
        }

        String errorMsg = "Failed to initiate PayPal checkout";
        if (response != null && response.getMessages() != null) {
            errorMsg = response.getMessages().getMessage().get(0).getText();
        }
        logger.error("PayPal initiation failed: {}", errorMsg);
        return ResponseEntity.badRequest().body(Map.of("message", errorMsg));
    }

    /**
     * Step 2: Complete PayPal payment after user returns from PayPal authorization.
     * Captures the previously authorized transaction.
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentResponse> complete(@RequestBody Map<String, Object> request) {
        String payerID = (String) request.get("payerID");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        Environment environment = config.isSandbox() ? Environment.SANDBOX : Environment.PRODUCTION;

        MerchantAuthenticationType merchantAuth = new MerchantAuthenticationType();
        merchantAuth.setName(config.getApiLoginId());
        merchantAuth.setTransactionKey(config.getTransactionKey());

        PayPalType payPal = new PayPalType();
        payPal.setPayerID(payerID);

        PaymentType paymentType = new PaymentType();
        paymentType.setPayPal(payPal);

        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        transactionRequest.setAmount(amount);
        transactionRequest.setPayment(paymentType);

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setMerchantAuthentication(merchantAuth);
        apiRequest.setTransactionRequest(transactionRequest);

        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();

        if (response != null
                && response.getMessages().getResultCode() == MessageTypeEnum.OK
                && response.getTransactionResponse() != null) {

            TransactionResponse txnResponse = response.getTransactionResponse();
            logger.info("PayPal payment completed. Transaction ID: {}", txnResponse.getTransId());
            return ResponseEntity.ok(new PaymentResponse(
                    true, txnResponse.getTransId(), "PayPal payment approved", txnResponse.getResponseCode()
            ));
        }

        String errorMsg = "PayPal payment failed";
        if (response != null && response.getMessages() != null) {
            errorMsg = response.getMessages().getMessage().get(0).getText();
        }
        logger.error("PayPal completion failed: {}", errorMsg);
        return ResponseEntity.badRequest().body(new PaymentResponse(false, errorMsg));
    }
}
