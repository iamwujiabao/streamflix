# StreamFlix REST API Reference

Base URL: `http://<host>:8080/api`
All responses are wrapped in a uniform `ApiResponse<T>` envelope:

```json
{ "success": true, "message": "...", "data": { ... }, "timestamp": "2026-..." }
```

For interactive exploration, the running backend serves Swagger UI at
`/swagger-ui.html` and the OpenAPI 3 spec at `/v3/api-docs`.

---

## Authentication

StreamFlix uses **stateless JWT** auth. The `/auth/register` and `/auth/login`
endpoints return a token; subsequent calls must include it in the
`Authorization: Bearer <token>` header.

| Method | Path             | Auth | Body / Params                                                          | Description                          |
| ------ | ---------------- | ---- | ----------------------------------------------------------------------- | ------------------------------------ |
| POST   | `/auth/register` | none | `{ username, email, password, fullName?, country? }`                    | Create account, returns JWT          |
| POST   | `/auth/login`    | none | `{ username, password }`                                                | Authenticate, returns JWT            |
| GET    | `/auth/me`       | JWT  | —                                                                       | Profile of the current user          |
| GET    | `/auth/health`   | none | —                                                                       | Liveness probe                       |

**Successful auth response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "user": { "userId": 1, "username": "alice", "email": "...", "role": "USER" }
  }
}
```

---

## Videos

| Method | Path                              | Auth     | Description                                              |
| ------ | --------------------------------- | -------- | -------------------------------------------------------- |
| GET    | `/videos?page&size`               | public   | List published videos (newest first)                     |
| GET    | `/videos/search?q&page&size`      | public   | Keyword search across title and description              |
| POST   | `/videos/filter?page&size`        | public   | Multi-criteria filter (see below)                        |
| GET    | `/videos/trending?limit`          | public   | Trending by engagement-decay score                       |
| GET    | `/videos/category/{categoryId}`   | public   | Videos in a category                                     |
| GET    | `/videos/{videoId}`               | public   | Single video details                                     |
| GET    | `/videos/recommendations?limit`   | JWT      | Personalised recommendations                             |
| POST   | `/videos/channel/{channelId}`     | JWT      | Upload video metadata                                    |
| DELETE | `/videos/{videoId}`               | JWT      | Soft-delete (sets status=REMOVED)                        |
| POST   | `/videos/{videoId}/watch`         | JWT      | Record a watch event `{ secondsWatched, completed }`     |
| POST   | `/videos/{videoId}/like`          | JWT      | Toggle like                                              |
| POST   | `/videos/{videoId}/dislike`       | JWT      | Toggle dislike                                           |

### `POST /videos/filter` body

All fields are optional. Empty/null fields are skipped.

```json
{
  "keyword":         "spring",
  "categoryId":      3,
  "tags":            ["tutorial", "java"],
  "minDurationSec":  60,
  "maxDurationSec":  3600,
  "resolution":      "FHD",
  "isPremium":       false,
  "channelId":       42,
  "uploadedAfter":   "2026-01-01T00:00:00",
  "uploadedBefore":  "2026-12-31T23:59:59",
  "sortBy":          "views",
  "sortDirection":   "desc"
}
```

`sortBy` accepts: `upload_date` (default), `views`, `likes`, `duration`, `title`.

---

## Comments

| Method | Path                                    | Auth | Description                  |
| ------ | --------------------------------------- | ---- | ---------------------------- |
| GET    | `/comments/video/{videoId}?page&size`   | pub  | List comments on a video     |
| GET    | `/comments/{commentId}/replies`         | pub  | Replies to a comment         |
| POST   | `/comments/video/{videoId}`             | JWT  | Post comment `{ content, parentCommentId? }` |
| DELETE | `/comments/{commentId}`                 | JWT  | Soft-delete comment          |

---

## Channels

| Method | Path                                  | Auth | Description                         |
| ------ | ------------------------------------- | ---- | ----------------------------------- |
| GET    | `/channels`                           | pub  | List all channels                   |
| GET    | `/channels/{channelId}`               | pub  | Channel details                     |
| GET    | `/channels/{channelId}/videos`        | pub  | Channel's videos                    |
| POST   | `/channels`                           | JWT  | Create channel `{ name, description }` |
| POST   | `/channels/{channelId}/subscribe`     | JWT  | Subscribe                           |
| DELETE | `/channels/{channelId}/subscribe`     | JWT  | Unsubscribe                         |

---

## Playlists

| Method | Path                                       | Auth | Description                                     |
| ------ | ------------------------------------------ | ---- | ----------------------------------------------- |
| POST   | `/playlists`                               | JWT  | Create playlist `{ title, description?, isPublic? }` |
| GET    | `/playlists/me`                            | JWT  | My playlists (public + private)                 |
| GET    | `/playlists/user/{userId}`                 | pub  | A user's public playlists                       |
| GET    | `/playlists/{id}`                          | pub  | Playlist metadata                               |
| GET    | `/playlists/{id}/videos`                   | pub  | Videos in the playlist (ordered)                |
| POST   | `/playlists/{id}/videos/{videoId}`         | JWT  | Add video (owner only)                          |
| DELETE | `/playlists/{id}/videos/{videoId}`         | JWT  | Remove video (owner only)                       |
| DELETE | `/playlists/{id}`                          | JWT  | Delete playlist (owner only)                    |

---

## Subscriptions and Plans

| Method | Path                            | Auth | Description                                                    |
| ------ | ------------------------------- | ---- | -------------------------------------------------------------- |
| GET    | `/subscriptions/plans`          | pub  | Available plans (Basic / Standard / Premium)                   |
| POST   | `/subscriptions/subscribe`      | JWT  | `{ planId, months, paymentMethod }` → cancels prior active sub |
| GET    | `/subscriptions/me`             | JWT  | All subscriptions for current user                             |
| GET    | `/subscriptions/me/active`      | JWT  | Currently active sub, or null                                  |
| POST   | `/subscriptions/{id}/cancel`    | JWT  | Cancel a subscription                                          |

`paymentMethod` accepts: `CREDIT_CARD`, `PAYPAL`, `MOMO`, `ZALOPAY`, `BANK_TRANSFER`.

---

## Notifications

| Method | Path                            | Auth | Description                       |
| ------ | ------------------------------- | ---- | --------------------------------- |
| GET    | `/notifications?page&size`      | JWT  | List my notifications             |
| GET    | `/notifications/unread-count`   | JWT  | Count of unread notifications     |
| POST   | `/notifications/{id}/read`      | JWT  | Mark one as read                  |
| POST   | `/notifications/read-all`       | JWT  | Mark all as read                  |

---

## Categories and Users

| Method | Path                  | Auth | Description                       |
| ------ | --------------------- | ---- | --------------------------------- |
| GET    | `/categories`         | pub  | All categories                    |
| GET    | `/users/{userId}`     | pub  | Public profile                    |

---

## Health, Metrics, Docs

| Path                 | Description                          |
| -------------------- | ------------------------------------ |
| `/actuator/health`   | Spring Boot health check             |
| `/actuator/info`     | Build info                           |
| `/actuator/metrics`  | JVM and HTTP metrics                 |
| `/swagger-ui.html`   | Interactive API explorer             |
| `/v3/api-docs`       | OpenAPI 3 JSON spec                  |

---

## Error format

All errors come back as:

```json
{
  "success": false,
  "message": "Username already taken",
  "data": null,
  "timestamp": "2026-05-02T10:00:00Z"
}
```

Common HTTP statuses:

| Status | Meaning                                        |
| ------ | ---------------------------------------------- |
| 400    | Validation error or bad request                |
| 401    | Missing or invalid JWT                         |
| 403    | Authenticated but not authorised               |
| 404    | Resource does not exist                        |
| 409    | Conflict (e.g., duplicate username)            |
| 500    | Server error                                   |
