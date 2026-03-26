package com.authorizenet.prototype.model;

public class PaymentResponse {

    private boolean success;
    private String transactionId;
    private String message;
    private String responseCode;

    public PaymentResponse() {}

    public PaymentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentResponse(boolean success, String transactionId, String message, String responseCode) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
        this.responseCode = responseCode;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
}
