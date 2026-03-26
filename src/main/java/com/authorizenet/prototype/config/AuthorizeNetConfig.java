package com.authorizenet.prototype.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorizeNetConfig {

    @Value("${authorizenet.api-login-id}")
    private String apiLoginId;

    @Value("${authorizenet.transaction-key}")
    private String transactionKey;

    @Value("${authorizenet.client-key}")
    private String clientKey;

    @Value("${authorizenet.environment}")
    private String environment;

    public String getApiLoginId() {
        return apiLoginId;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isSandbox() {
        return "SANDBOX".equalsIgnoreCase(environment);
    }

    public String getAcceptJsUrl() {
        return isSandbox()
                ? "https://jstest.authorize.net/v1/Accept.js"
                : "https://js.authorize.net/v1/Accept.js";
    }

    public String getAcceptUiUrl() {
        return isSandbox()
                ? "https://jstest.authorize.net/v3/AcceptUI.js"
                : "https://js.authorize.net/v3/AcceptUI.js";
    }
}
