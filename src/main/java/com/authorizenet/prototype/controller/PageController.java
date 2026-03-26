package com.authorizenet.prototype.controller;

import com.authorizenet.prototype.config.AuthorizeNetConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final AuthorizeNetConfig config;

    public PageController(AuthorizeNetConfig config) {
        this.config = config;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("apiLoginId", config.getApiLoginId());
        model.addAttribute("clientKey", config.getClientKey());
        model.addAttribute("acceptJsUrl", config.getAcceptJsUrl());
        model.addAttribute("isSandbox", config.isSandbox());
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/pay/credit-card")
    public String creditCard(Model model) {
        addCommonAttributes(model);
        return "credit-card";
    }

    @GetMapping("/pay/apple-pay")
    public String applePay(Model model) {
        addCommonAttributes(model);
        return "apple-pay";
    }

    @GetMapping("/pay/google-pay")
    public String googlePay(Model model) {
        addCommonAttributes(model);
        return "google-pay";
    }

    @GetMapping("/pay/paypal")
    public String paypal(Model model) {
        addCommonAttributes(model);
        return "paypal";
    }

    @GetMapping("/payment/result")
    public String paymentResult() {
        return "payment-result";
    }
}
