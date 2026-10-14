# Employee API

## `GET /api/employees`

Returns the employee collection.

### Responses

- `200 OK`: JSON array of employees. An empty collection is `[]`.

```json
[
  { "id": 1, "name": "John Doe", "email": "john@example.com" }
]
```

## `GET /api/employees/{id}`

Returns the employee with the requested ID.

### Responses

- `200 OK`: Employee JSON with `id`, `name`, and `email`.
- `404 Not Found`: `{"error":"Employee not found"}` when the ID is unknown.

```json
{ "id": 1, "name": "John Doe", "email": "john@example.com" }
```

## `POST /api/employees`

Creates an employee. The server assigns an incrementing ID starting at `1`.

### Request

`name` is required and non-blank. `email` is required and must be a valid email address.

```json
{ "name": "John Doe", "email": "john@example.com" }
```

### Responses

- `201 Created`: Created Employee JSON, including its server-generated ID.
- `400 Bad Request`: `{"error":"Invalid employee data"}` for invalid or missing fields or malformed JSON.

```json
{ "id": 1, "name": "John Doe", "email": "john@example.com" }
```