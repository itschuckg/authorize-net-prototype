# Nginx + HTTPS Setup for Authorize.Net Payment Prototype

## What's Been Created

✅ **Self-signed SSL certificates** (valid for 365 days)
- `nginx-certs/localhost.crt` - Certificate
- `nginx-certs/localhost.key` - Private key

✅ **Nginx configuration**
- `nginx-config/nginx.conf` - Standalone config file
- `kubernetes/nginx-deployment.yaml` - Full K8s manifest (ConfigMap + Secret + Deployment + Service)

✅ **Spring Boot configuration**
- Changed `server.port=8081` (internal, private to localhost only)
- Nginx listens on 8080 (HTTP) → redirects to 8443 (HTTPS)
- Nginx proxies HTTPS traffic to Spring Boot on 8081

## Architecture

```
Browser (localhost:8080)
    ↓ HTTP
Nginx Container (Port 8080)
    ↓ Redirect to HTTPS
Nginx Container (Port 8443) ← SSL/TLS Termination
    ↓ HTTP (internally)
Spring Boot (localhost:8081)
```

## Option 1: Deploy to K8s (Recommended for Docker Desktop)

### Step 1: Apply the K8s manifests

```bash
cd C:/Users/CharlesGovindaswamy/Downloads/Authorize.net
kubectl apply -f kubernetes/nginx-deployment.yaml
```

### Step 2: Verify deployment

```bash
# Check pods
kubectl get pods -l app=nginx-payment

# Check service
kubectl get svc nginx-payment-service
```

Output should show:
```
NAME                    TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)
nginx-payment-service   NodePort   10.x.x.x        <none>        8080:30080/TCP,8443:30443/TCP
```

### Step 3: Port-forward (if needed)

If you already have something on port 8080, use port-forward:

```bash
kubectl port-forward svc/nginx-payment-service 8080:8080 8443:8443
```

### Step 4: Access the app

- **HTTP (redirects to HTTPS):** http://localhost:8080
- **HTTPS Direct:** https://localhost:8443

⚠️ **Browser warning:** You'll see a cert warning because it's self-signed. Click **"Advanced"** → **"Proceed"** (this is expected for localhost testing).

---

## Option 2: Run Nginx Locally (Docker)

If you want to test locally without K8s:

```bash
docker run -d \
  -p 8080:8080 \
  -p 8443:8443 \
  -v C:/Users/CharlesGovindaswamy/Downloads/Authorize.net/nginx-config/nginx.conf:/etc/nginx/nginx.conf:ro \
  -v C:/Users/CharlesGovindaswamy/Downloads/Authorize.net/nginx-certs:/etc/nginx/certs:ro \
  --name nginx-payment \
  nginx:latest
```

Then access: https://localhost:8443

---

## Step 5: Run Spring Boot

In a **separate terminal**:

```bash
cd C:/Users/CharlesGovindaswamy/Downloads/Authorize.net
mvn spring-boot:run
```

This starts Spring Boot on internal port 8081 (only accessible locally).

---

## Step 6: Test the Payment Gateway

1. Open https://localhost:8443 (or http://localhost:8080 which redirects)
2. Click "Credit Card" payment method
3. Use test card: **4111 1111 1111 1111** with any future expiration date
4. Submit and verify payment flow works

---

## Troubleshooting

### "Connection refused" on localhost:8080

- **K8s:** Check pod logs: `kubectl logs -l app=nginx-payment`
- **Docker:** Check container logs: `docker logs nginx-payment`
- **Ensure Spring Boot is running:** `mvn spring-boot:run` in another terminal

### Nginx can't reach Spring Boot (502 error)

**Cause:** Spring Boot not running on 8081

**Fix:**
```bash
# Terminal 1: Start Spring Boot
mvn spring-boot:run

# Terminal 2: Verify it's listening
netstat -an | grep 8081
```

### SSL certificate errors in browser

This is **normal** for self-signed certs.

**Chrome/Edge:** Click "Advanced" → "Proceed to localhost"
**Firefox:** Click "Advanced" → "Accept the Risk and Continue"

### How to regenerate certificates

If needed, regenerate self-signed certs:

```bash
cd nginx-certs
rm localhost.*
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout localhost.key -out localhost.crt \
    -subj "//CN=localhost"
```

Then re-encode and update the K8s Secret in `kubernetes/nginx-deployment.yaml`.

---

## For Production Deployment

Replace self-signed certs with real certificates from Let's Encrypt or your certificate authority:

1. Get cert from Let's Encrypt (via Certbot)
2. Update `nginx-certs/localhost.crt` and `nginx-certs/localhost.key`
3. Re-encode to base64 and update the K8s Secret
4. Change domain from `localhost` to your real domain
5. Redeploy: `kubectl apply -f kubernetes/nginx-deployment.yaml`

---

## Files Structure

```
authorize-net-prototype/
├── nginx-certs/
│   ├── localhost.crt        ← Self-signed certificate
│   └── localhost.key        ← Private key
├── nginx-config/
│   └── nginx.conf           ← Standalone config
├── kubernetes/
│   └── nginx-deployment.yaml ← K8s manifests (ConfigMap + Secret + Pod + Service)
├── src/main/resources/
│   └── application.properties ← server.port=8081 (internal)
└── pom.xml
```

---

## Next Steps

1. **Deploy nginx** (K8s or Docker)
2. **Run Spring Boot** on port 8081
3. **Test HTTPS** on `https://localhost:8443`
4. **Commit & push** to GitHub when ready
