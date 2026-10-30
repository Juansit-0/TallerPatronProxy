# Taller Patron Proxy - Backend

Microservice monitoring system with an audit and logging proxy, built with Spring Boot.

Three microservices (Inventory, Orders, Payments) sit behind a `LoggingProxy` that implements the same
`MicroserviceProxy<T>` interface. Every call is intercepted to record usage, response time and errors.

## Proxy pattern

| Role | Class |
| --- | --- |
| Subject | `proxy.MicroserviceProxy<T>` |
| Real subjects | `service.InventoryService`, `service.OrderService`, `service.PaymentService` |
| Proxy | `proxy.LoggingProxy<T>` |
| Client | `web.ServiceController`, `web.MetricsController` |

Each log contains `requestId` (UUID), `serviceId`, `operation`, `durationMs`, `status` (SUCCESS/ERROR),
`timestamp`, the input params, the response and, on failure, the error with a short stack trace.
`PaymentService` fails 10% of its calls on purpose.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/services/inventory/{operation}` | `list`, `check`, `restock` |
| POST | `/api/services/orders/{operation}` | `create`, `list`, `cancel` |
| POST | `/api/services/payments/{operation}` | `charge`, `refund`, `status` |
| GET | `/api/metrics/summary` | Total calls, error rate and average time per service |
| GET | `/api/metrics/logs?service=&status=&from=&to=&page=&size=` | Filtered, paginated logs |
| POST | `/api/metrics/simulate-load` | Fires 50 random calls |

## Run

```bash
./mvnw spring-boot:run
./mvnw test
```

Environment: `PORT` (default 8080), `ALLOWED_ORIGIN` (default `*`).

## Production

- Frontend (Vercel): https://taller-patron-proxy-frontend-theta.vercel.app
- Backend (Railway): https://taller-patron-proxy-production.up.railway.app/api/metrics/summary
