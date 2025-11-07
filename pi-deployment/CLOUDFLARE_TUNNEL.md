# Cloudflare Tunnel Setup for LetsGoGolfing

This document describes how to set up a Cloudflare Tunnel to expose the LetsGoGolfing API securely to the internet for Stripe webhooks.

## Prerequisites

- Raspberry Pi with LetsGoGolfing deployed
- Domain in Cloudflare
- SSH access to Pi

## Installation Steps

### 1. Install cloudflared on Raspberry Pi

```bash
# SSH into your Pi
ssh rpi

# Download ARM64 version for Raspberry Pi
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-arm64.deb -o cloudflared.deb
sudo dpkg -i cloudflared.deb
```

### 2. Authenticate with Cloudflare

```bash
cloudflared tunnel login
```

This opens a browser - select domain.

### 3. Create the tunnel

```bash
cloudflared tunnel create letsgogolfing
```

Note the tunnel ID returned (e.g., `abc123-def456-...`)

### 4. Create configuration file

```bash
mkdir -p ~/.cloudflared
nano ~/.cloudflared/config.yml
```

Add the following (replace `<TUNNEL-ID>` with your actual tunnel ID):

```yaml
tunnel: <TUNNEL-ID>
credentials-file: /home/smaldd14/.cloudflared/<TUNNEL-ID>.json

ingress:
  - hostname: api.yourdomain.dev
    service: http://localhost:8082
  - service: http_status:404
```

### 5. Create DNS route

```bash
cloudflared tunnel route dns letsgogolfing api.yourdomain.dev
```

### 6. Test the tunnel

```bash
cloudflared tunnel run letsgogolfing
```

Test in browser: `https://api.yourdomain.dev/actuator/health`

Press `Ctrl+C` to stop once verified.

### 7. Install as systemd service

```bash
sudo cloudflared --config /home/pi/.cloudflared/config.yml service install
sudo systemctl start cloudflared
sudo systemctl enable cloudflared
```

### 8. Verify service is running

```bash
sudo systemctl status cloudflared
```

## Stripe Webhook Configuration

Add webhook endpoint in Stripe Dashboard:

**Webhook URL:** `https://api.yourdomain.dev/api/stripe/webhook`

**Events to listen for:**
- `checkout.session.completed`

## Management Commands

```bash
# Check tunnel status
sudo systemctl status cloudflared

# View logs
sudo journalctl -u cloudflared -f

# Restart tunnel
sudo systemctl restart cloudflared

# Stop tunnel
sudo systemctl stop cloudflared

# List all tunnels
cloudflared tunnel list

# Delete tunnel (if needed)
cloudflared tunnel delete letsgogolfing
```

## Troubleshooting

### Tunnel not connecting

```bash
# Check logs
sudo journalctl -u cloudflared -f

# Verify config file exists
cat ~/.cloudflared/config.yml

# Test manually
cloudflared tunnel run letsgogolfing
```

### DNS not resolving

- Check Cloudflare dashboard for DNS record `api.yourdomain.dev`
- Record should be CNAME pointing to `<TUNNEL-ID>.cfargotunnel.com`
- May take a few minutes to propagate

### 502 Bad Gateway

- Ensure LetsGoGolfing service is running: `docker ps`
- Verify service is on port 8082: `curl http://localhost:8082/actuator/health`
- Check tunnel config points to correct port

## Security Notes

- Tunnel provides automatic SSL/TLS encryption
- No port forwarding required on router
- DDoS protection included via Cloudflare
- Only exposes specific service (port 8082) not entire Pi
