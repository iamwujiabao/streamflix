# 🎬 StreamFlix — Video Streaming Database

> **IT079IU — Principles of Database Management**
> Vietnam National University – International University, HCMC
> Instructor: Assoc. Prof. Nguyen Thi Thuy Loan, PhD.
> Source code: <https://github.com/iamwujiabao/streamflix>

A production-grade, end-to-end video streaming platform built around a
normalised MySQL schema, a Spring Boot REST API, and a JavaFX desktop
client.

---

## 👥 Group Members

| Name | Student ID | Role |
| ---- | ---------- | ---- |
| Võ Gia Bảo                | ITCSIU24010 | Team Leader |
| Nguyễn Quốc Trung Kiên    | ITCSIU24046 | Team Member |
| Trần Đức Mạnh             | ITCSIU22303 | Team Member |
| Nguyễn Minh Nhật          | ITITWE24060 | Team Member |
| Trần Đức Phong            | ITITIU22123 | Team Member |
| Dương Hoàng Thiên Phúc    | ITITIU24046 | Team Member |
| Nguyễn Hà An Thạnh        | ITITWE22051 | Team Member |
| Võ Ngọc Anh Thư           | ITITIU23036 | Team Member |

---

## 🧱 Architecture

```
┌────────────────────┐   HTTPS / JSON   ┌────────────────────┐   JDBC   ┌────────────┐
│  JavaFX Client     │ ───────────────► │  Spring Boot API   │ ───────► │  MySQL 8   │
│  (desktop app)     │   Bearer JWT      │  (port 8080/api)   │           │  Schema +   │
└────────────────────┘                   │  Hibernate / JPA   │           │  Triggers   │
                                          │  Flyway / Actuator │           └────────────┘
                                          └────────────────────┘
```

Key technical choices:

- **Backend** — Spring Boot 3.3, Java 21, Spring Security with stateless JWT
  (HS256), BCrypt-12 password hashing, Hibernate, Flyway migrations,
  springdoc OpenAPI, Actuator. Builds an executable fat-jar.
- **Frontend** — JavaFX 21 desktop client packaged as a shaded jar.
  Uses `java.net.http.HttpClient` to talk to the backend.
- **Database** — MySQL 8.4 with sixteen tables in BCNF/3NF, triggers for
  counter maintenance, views for trending content, FULLTEXT search.
- **Deployment** — Multi-stage Dockerfile + `docker-compose.yml` bringing up
  MySQL and the backend together.

---

## 🗂 Project Layout

```
video-streaming-db/
├── database/                      # MySQL DDL, sample data, queries
│   ├── 01_schema.sql
│   ├── 02_sample_data.sql
│   └── 03_queries.sql
├── backend/                       # Spring Boot REST API
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/streamflix/
│       ├── controller/   service/   repository/
│       ├── entity/       dto/       exception/
│       ├── security/     config/
│       └── StreamflixApplication.java
├── frontend/                      # JavaFX desktop client
│   ├── pom.xml
│   └── src/main/java/com/streamflix/client/
│       ├── api/   model/   ui/   util/
│       ├── StreamflixApp.java   (JavaFX entry)
│       └── Launcher.java        (plain-main entry for fat jar)
├── docs/
│   ├── ERD.md
│   ├── RELATIONAL_SCHEMA.md
│   ├── NORMALIZATION.md
│   ├── API.md
│   └── StreamFlix_Final_Report.docx
├── docker-compose.yml             # MySQL + backend stack
├── .env.example                   # Sample env for compose
└── README.md
```

---

## 🔐 Demo Accounts

The sample data ships with ten pre-loaded users. **All of them share the same password** so you don't need to register manually:

| Username | Role     | Password      |
| -------- | -------- | ------------- |
| `alice`   | CREATOR | `password123` |
| `bob`     | CREATOR | `password123` |
| `charlie` | CREATOR | `password123` |
| `david`   | USER    | `password123` |
| `eve`     | USER    | `password123` |
| `frank`   | USER    | `password123` |
| `grace`   | USER    | `password123` |
| `henry`   | USER    | `password123` |
| `ivy`     | USER    | `password123` |
| `admin`   | ADMIN   | `password123` |

> **For demonstration only.** In production, every account should have a unique
> strong password set by the user during registration.

---

## 🚀 Quick Start

### 1. With Docker (recommended)

```bash
cp .env.example .env                           # then edit secrets / host ports
docker compose up -d                           # builds backend, starts MySQL
# Backend → http://localhost:8081/api          # change BACKEND_HOST_PORT in .env
# Swagger → http://localhost:8081/api/swagger-ui.html
# MySQL   → localhost:3307                     # change MYSQL_HOST_PORT in .env
```

> **Port conflicts?** The compose file defaults to non-standard host ports
> (3307 for MySQL, 8081 for the backend) so it doesn't clash with a local
> MySQL or Spring dev server. Override them in `.env` if needed.

### 2. Manually (development)

**Database**
```bash
mysql -u root -p < database/01_schema.sql      # creates streamflix_db
mysql -u root -p streamflix_db < database/02_sample_data.sql
```

**Backend**
```bash
cd backend
mvn spring-boot:run                            # dev profile, in-memory H2
# or for the real DB:
SPRING_PROFILES_ACTIVE=prod \
DB_HOST=localhost DB_NAME=streamflix_db \
DB_USERNAME=root  DB_PASSWORD=… \
mvn spring-boot:run
```

**Desktop client**
```bash
cd frontend
mvn javafx:run                                 # opens the JavaFX window
# or build a runnable jar:
mvn package
java -jar target/streamflix-client-1.0.0.jar
```

You can point the client at a non-default backend with
`-Dstreamflix.api.url=http://my-host:8080/api`.

---

## 🧪 Tests

| Suite                          | Tests | Layer         |
| ------------------------------ | ----- | ------------- |
| `AuthControllerTest`           | 6     | integration   |
| `JwtTokenProviderTest`         | 3     | unit          |
| `UserServiceTest`              | 4     | service       |
| `VideoServiceTest`             | 4     | service       |
| `SubscriptionServiceTest`      | 5     | service       |
| `PlaylistServiceTest`          | 3     | service       |
| `ApiClientTest` (frontend)     | 4     | client        |
| `VideoModelTest` (frontend)    | 3     | model         |
| **Total**                      | **32** |              |

```bash
cd backend  && mvn test
cd frontend && mvn test
```

---

## 🔐 Security & Production Readiness

- Stateless JWT (HS256), 24h expiry, secret loaded from `APP_JWT_SECRET`
- BCrypt password hashing with work factor 12
- CORS open to a configurable origin list
- CSRF disabled (token-only authentication, no cookies)
- All secrets read from env vars in the `prod` profile
- Flyway-managed schema migrations, Hibernate set to `validate`
- HikariCP connection pooling
- Spring Boot Actuator (`/actuator/health`, `/info`, `/metrics`)
- Multi-stage Docker build with non-root user and HEALTHCHECK

---

## 📚 Documentation

- `docs/ERD.md` — Entity-Relationship diagram + cardinality reasoning
- `docs/RELATIONAL_SCHEMA.md` — ER → relational translation
- `docs/NORMALIZATION.md` — Per-table 1NF→4NF analysis
- `docs/API.md` — REST endpoint reference
- Live docs: `http://localhost:8081/api/swagger-ui.html`

---

## 📄 License

Educational use only — IT079IU final project, May 2026.
