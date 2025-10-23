# Let's Go Golfing

A resilient tee time monitoring system built with Temporal.io that searches GolfNow for available tee times and sends email notifications when new slots appear.

## Prerequisites

- Java 21+
- PostgreSQL
- [Temporal CLI](https://docs.temporal.io/cli)
- Maven

## Local Setup

### 1. Start PostgreSQL

```bash
# Using Docker
docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres

# Or use your local PostgreSQL installation
```

### 2. Create Database Schema

The schema is automatically created on startup via `src/main/resources/schema.sql`.

### 3. Start Temporal Dev Server

```bash
temporal server start-dev
```

### 4. Set Environment Variables

Create a `.env` file or export these variables:

```bash
# Required
export BB_API_KEY=your_browserbase_api_key
export BB_PROJECT_ID=your_project_id
export BB_EMAIL=your_email
export BB_PASSWORD=your_password

export GOLFNOW_EMAIL=your_golfnow_email
export GOLFNOW_PASSWORD=your_golfnow_password

export AWS_ACCESS_KEY=your_aws_access_key
export AWS_SECRET_KEY=your_aws_secret_key
export AWS_FROM_EMAIL=noreply@yourdomain.com
export AWS_FROM_NAME="Let's Go Golfing"
export AWS_REGION=us-east-1
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

## API Endpoints

### Create a Search Preference

```bash
curl -X POST http://localhost:8080/api/user/searches \
  -H "Content-Type: application/json" \
  -d '{
    "email": "you@example.com",
    "searchCriteria": {
      "latitude": 40.9176,
      "longitude": -74.1718,
      "radiusMiles": 15,
      "searchDate": "Oct 25 2025",
      "numberOfPlayers": 2,
      "preferredTimeStart": 8,
      "preferredTimeEnd": 14,
      "maxPrice": 80,
      "hotDealsOnly": false,
      "holes": 2,
      "priorityCourses": [5212, 1649]
    },
    "paymentEnabled": false,
    "notifyEnabled": true,
    "scheduleInterval": "PT5M"
  }'
```

### List User Searches

```bash
curl "http://localhost:8080/api/user/searches?email=you@example.com"
```

### Get Search by ID

```bash
curl http://localhost:8080/api/user/searches/{id}
```

### Delete a Search

```bash
curl -X DELETE http://localhost:8080/api/user/searches/{id}
```

### Manually Search Tee Times

```bash
curl -X POST http://localhost:8080/api/tee-times/search \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.9176,
    "longitude": -74.1718,
    "radiusMiles": 15,
    "searchDate": "Oct 25 2025",
    "numberOfPlayers": 2,
    "preferredTimeStart": 8,
    "preferredTimeEnd": 14,
    "priorityCourses": [5212, 1649]
  }'
```

### Search Facilities

```bash
curl -X POST http://localhost:8080/api/facilities/search \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.9176,
    "longitude": -74.1718,
    "radiusMiles": 15,
    "searchDate": "Oct 25 2025"
  }'
```

## How It Works

1. **Create a search preference** via POST `/api/user/searches`
2. **Temporal schedule** automatically starts, triggering `TeeTimeMonitorWorkflow` at the specified interval
3. **Workflow executes**: Searches GolfNow → Filters new results → Saves to DB → Sends email notifications
4. **Email notifications** sent via AWS SES when new tee times match your criteria

## Architecture

- **Spring Boot** - Application framework
- **Temporal.io** - Workflow orchestration and scheduling
- **PostgreSQL** - Database for search criteria and results
- **AWS SES** - Email notifications

Read the full blog post: [Monitoring GolfNow Tee Times with Temporal.io](https://smaldore.dev/blog/temporal/tee-time-monitoring)
