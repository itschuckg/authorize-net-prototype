package com.authorizenet.prototype.model;

import java.math.BigDecimal;

public class PaymentRequest {

    private String opaqueDataDescriptor;
    private String opaqueDataValue;
    private BigDecimal amount;
    private String paymentMethod; // CREDIT_CARD, APPLE_PAY, GOOGLE_PAY, PAYPAL

    // Billing info (optional)
    private String firstName;
    private String lastName;
    private String email;

    public PaymentRequest() {}

    public String getOpaqueDataDescriptor() { return opaqueDataDescriptor; }
    public void setOpaqueDataDescriptor(String opaqueDataDescriptor) { this.opaqueDataDescriptor = opaqueDataDescriptor; }

    public String getOpaqueDataValue() { return opaqueDataValue; }
    public void setOpaqueDataValue(String opaqueDataValue) { this.opaqueDataValue = opaqueDataValue; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
