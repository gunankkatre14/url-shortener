# URL Shortener Service

A production-grade URL shortening service built with Spring Boot, PostgreSQL, Redis, and Docker.

## Tech Stack
- **Backend**: Spring Boot 3.2.5 (Java 17)
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Containerization**: Docker + Docker Compose
- **API Docs**: Swagger UI

## Features
- Shorten long URLs with Base62 encoding
- Redis caching for sub-millisecond redirects (cache-aside pattern)
- Click analytics — IP, browser, timestamp tracking
- Rate limiting — 10 requests/min per IP (Redis counter)
- URL expiry with TTL support
- Custom aliases
- Auto cleanup of expired URLs (@Scheduled)
- Swagger UI for API documentation

## Getting Started

### Prerequisites
- Java 17
- Docker Desktop

### Run locally

```bash
# Step 1: Start PostgreSQL + Redis
docker-compose up -d

# Step 2: Run the app
mvn spring-boot:run

# Step 3: Open Swagger UI
http://localhost:8080/swagger-ui.html
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/shorten | Shorten a URL |
| GET | /{code} | Redirect to original URL |
| GET | /api/stats/{code} | Get click analytics |
| DELETE | /api/urls/{code} | Delete a short URL |

## Request Examples

### Shorten a URL
```json
POST /api/shorten
{
  "originalUrl": "https://www.google.com",
  "customAlias": "mygoogle",
  "expiryHours": 24
}
```

### Response
```json
{
  "shortUrl": "http://localhost:8080/mygoogle"
}
```

## Architecture