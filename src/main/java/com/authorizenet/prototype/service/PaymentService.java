package com.authorizenet.prototype.service;

import com.authorizenet.prototype.config.AuthorizeNetConfig;
import com.authorizenet.prototype.model.PaymentRequest;
import com.authorizenet.prototype.model.PaymentResponse;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final AuthorizeNetConfig config;

    public PaymentService(AuthorizeNetConfig config) {
        this.config = config;
    }

    /**
     * Process a payment using the opaque data (payment nonce) from Accept.js.
     * This works for Credit Card, Apple Pay, Google Pay, and PayPal —
     * Accept.js returns opaque data for all of them.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // Set up the API environment
        Environment environment = config.isSandbox() ? Environment.SANDBOX : Environment.PRODUCTION;
        ApiOperationBase.setEnvironment(environment);

        // Create merchant authentication
        MerchantAuthenticationType merchantAuth = new MerchantAuthenticationType();
        merchantAuth.setName(config.getApiLoginId());
        merchantAuth.setTransactionKey(config.getTransactionKey());
        ApiOperationBase.setMerchantAuthentication(merchantAuth);

        // Create opaque data payment type from Accept.js token
        OpaqueDataType opaqueData = new OpaqueDataType();
        opaqueData.setDataDescriptor(request.getOpaqueDataDescriptor());
        opaqueData.setDataValue(request.getOpaqueDataValue());

        PaymentType paymentType = new PaymentType();
        paymentType.setOpaqueData(opaqueData);

        // Create the transaction request
        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        transactionRequest.setAmount(request.getAmount());
        transactionRequest.setPayment(paymentType);

        // Add billing info if provided
        if (request.getFirstName() != null || request.getLastName() != null) {
            CustomerAddressType billingAddress = new CustomerAddressType();
            billingAddress.setFirstName(request.getFirstName());
            billingAddress.setLastName(request.getLastName());
            transactionRequest.setBillTo(billingAddress);
        }

        if (request.getEmail() != null) {
            CustomerDataType customerData = new CustomerDataType();
            customerData.setEmail(request.getEmail());
            transactionRequest.setCustomer(customerData);
        }

        // Build the API request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setMerchantAuthentication(merchantAuth);
        apiRequest.setTransactionRequest(transactionRequest);

        // Execute
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        CreateTransactionResponse response = controller.getApiResponse();

        if (response == null) {
            logger.error("Null response from Authorize.Net API");
            return new PaymentResponse(false, "No response from payment gateway");
        }

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK
                && response.getTransactionResponse() != null) {

            TransactionResponse txnResponse = response.getTransactionResponse();

            if ("1".equals(txnResponse.getResponseCode())) {
                logger.info("Payment successful. Transaction ID: {}", txnResponse.getTransId());
                return new PaymentResponse(
                        true,
                        txnResponse.getTransId(),
                        "Payment approved",
                        txnResponse.getResponseCode()
                );
            } else {
                String errorMsg = "Transaction declined";
                logger.warn("Transaction declined: {}", errorMsg);
                return new PaymentResponse(false, errorMsg);
            }
        } else {
            String errorMsg = "Payment failed";
            if (response.getMessages() != null && response.getMessages().getMessage() != null
                    && !response.getMessages().getMessage().isEmpty()) {
                errorMsg = response.getMessages().getMessage().get(0).getText();
            }
            logger.error("Payment error: {}", errorMsg);
            return new PaymentResponse(false, errorMsg);
        }
    }
}
