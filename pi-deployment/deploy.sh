#!/bin/bash

# LetsGoGolfing Pi deployment script
set -e

echo "⛳ Deploying LetsGoGolfing to Raspberry Pi..."

# Check if running on Pi
if ! grep -q "Raspberry Pi" /proc/cpuinfo 2>/dev/null; then
    echo "⚠️  Warning: This doesn't appear to be a Raspberry Pi"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Auto-detect Pi IP address
PI_IP=$(hostname -I | awk '{print $1}')
echo "🌐 Detected Pi IP: $PI_IP"

# Update .env file with detected IP
if [ -f ".env" ]; then
    sed -i "s/PI_IP=.*/PI_IP=$PI_IP/" .env
    echo "📝 Updated .env with Pi IP address"
fi

# Check if shared infrastructure is running
echo "🔍 Checking for shared infrastructure..."
if ! docker network ls | grep -q "shared-infra"; then
    echo "❌ ERROR: Shared infrastructure network not found!"
    echo "   Please deploy shared-infrastructure first:"
    echo "   cd ~/shared-infrastructure && ./deploy.sh"
    exit 1
fi

if ! docker ps | grep -q "postgresql"; then
    echo "⚠️  Warning: PostgreSQL container not running"
    echo "   Shared infrastructure may not be started"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check GitLab registry authentication
echo "🔐 Checking Docker registry authentication..."
if [ -f ~/.docker/config.json ] && grep -q "registry.gitlab.com" ~/.docker/config.json; then
    echo "✅ GitLab registry credentials found"
else
    echo "⚠️  Warning: GitLab registry credentials not found"
    echo "💡 Run: docker login registry.gitlab.com"
    echo "   Username: your-gitlab-username"
    echo "   Password: your-gitlab-access-token"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Please login to GitLab registry first:"
        echo "docker login registry.gitlab.com"
        exit 1
    fi
fi

# Stop existing containers
echo "🛑 Stopping existing LetsGoGolfing containers..."
docker compose down 2>/dev/null || true

# Clean up old images
echo "🧹 Cleaning up old images..."
docker image prune -f

# Pull latest image
echo "📥 Pulling latest LetsGoGolfing image..."
if ! docker compose pull; then
    echo "❌ Failed to pull image. Check registry authentication:"
    echo "docker login registry.gitlab.com"
    exit 1
fi

# Start service
echo "🚀 Starting LetsGoGolfing service..."
docker compose up -d --remove-orphans

# Wait for service to be healthy
echo "⏳ Waiting for service to start..."
for i in {1..30}; do
    if docker compose ps | grep -q "Up"; then
        break
    fi
    echo "   Waiting... ($i/30)"
    sleep 2
done

# Check service status
echo ""
echo "📊 Service Status:"
docker compose ps

# Show resource usage
echo ""
echo "📈 Current Resource Usage:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || echo "Resource stats unavailable"

# Show URLs
echo ""
echo "🎉 Deployment complete!"
echo "📱 API:        http://$PI_IP:8082"
echo "🏥 Health:     http://$PI_IP:8082/actuator/health"
echo "📚 API Docs:   http://$PI_IP:8082/swagger-ui.html"
echo ""
echo "🔧 Management commands:"
echo "   Monitor logs:    docker compose logs -f"
echo "   Stop service:    docker compose down"
echo "   Restart:         docker compose restart"
echo "   Update:          docker compose pull && docker compose up -d"
