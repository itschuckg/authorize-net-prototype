# Authorize.Net Payment Prototype

Spring Boot web application prototype integrating **Authorize.Net** payment gateway via **Accept.js** with support for:

- **Credit Card** — Custom payment form with client-side tokenization via Accept.js
- **Apple Pay** — Native Apple Pay button (Safari / iOS)
- **Google Pay** — Google Pay API integration
- **PayPal** — PayPal Express Checkout via Authorize.Net

## Architecture

```
Browser (Accept.js)          Spring Boot Backend          Authorize.Net
┌─────────────────┐         ┌──────────────────┐        ┌──────────────┐
│ Custom Card Form│──token──▶│ /api/payment/    │──API──▶│  Gateway     │
│ Apple Pay       │         │   process        │        │              │
│ Google Pay      │         │ /api/payment/    │        │              │
│ PayPal          │──redirect│   paypal/*       │        │              │
└─────────────────┘         └──────────────────┘        └──────────────┘
```

Card data **never touches your server** — Accept.js tokenizes it client-side into opaque payment data.

## Prerequisites

- Java 17+
- Maven 3.8+
- Authorize.Net sandbox account ([sign up](https://developer.authorize.net/hello_world/sandbox.html))

## Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/YOUR_USERNAME/authorize-net-prototype.git
   cd authorize-net-prototype
   ```

2. Configure your credentials in `src/main/resources/application.properties`:
   ```properties
   authorizenet.api-login-id=YOUR_API_LOGIN_ID
   authorizenet.transaction-key=YOUR_TRANSACTION_KEY
   authorizenet.client-key=YOUR_PUBLIC_CLIENT_KEY
   ```

3. Run:
   ```bash
   mvn spring-boot:run
   ```

4. Open http://localhost:8080

## Test Card Numbers (Sandbox)

| Card           | Number             |
|----------------|--------------------|
| Visa           | 4111 1111 1111 1111|
| Mastercard     | 5424 0000 0000 0015|
| Amex           | 3700 0000 0000 002 |
| Discover       | 6011 0000 0000 0012|

Use any future expiration date and any 3-digit CVV (4-digit for Amex).

## Project Structure

```
src/main/java/com/authorizenet/prototype/
├── config/          # Authorize.Net configuration
├── controller/      # Page controllers + REST API
├── model/           # Request/Response DTOs
└── service/         # Payment processing service

src/main/resources/
├── static/css/      # Styles
├── static/js/       # Shared JS utilities
├── templates/       # Thymeleaf HTML pages
└── application.properties
```

## Notes for Cloud Deployment

- Replace sandbox credentials with production credentials
- Set `authorizenet.environment=PRODUCTION`
- Enable HTTPS (required for Apple Pay and Google Pay)
- For Apple Pay: complete domain verification with Apple
- For Google Pay: register with Google Pay Business Console
- For PayPal: enable PayPal Express Checkout in your Authorize.Net merchant account
