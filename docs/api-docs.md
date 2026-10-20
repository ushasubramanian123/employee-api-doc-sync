# Employee API

Interactive API documentation is available at `/swagger-ui/index.html`, with the OpenAPI document at `/v3/api-docs`.

## `GET /api/employees`

Returns the employee collection, optionally filtered by department.

### Query parameters

- `department` (string, optional):
  - Omitted: returns all employees.
  - Provided: the value is trimmed, then matched case-insensitively and exactly against each employee's department (no partial matching). Employees without a department never match.
  - Blank or whitespace-only (for example `?department=`): `200 OK` with `[]`.
  - Unknown department: `200 OK` with `[]`.

### Responses

- `200 OK`: JSON array of employees. An empty result is `[]`.

```json
[
  { "id": 1, "name": "John Doe", "email": "john@example.com", "department": "IT" }
]
```

## `GET /api/employees/{id}`

Returns the employee with the requested ID.

### Responses

- `200 OK`: Employee JSON with `id`, `name`, `email`, and `department` (null when not supplied).
- `404 Not Found`: `{"error":"Employee not found"}` when the ID is unknown.

```json
{ "id": 1, "name": "John Doe", "email": "john@example.com", "department": "IT" }
```

## `POST /api/employees`

Creates an employee. The server assigns an incrementing ID starting at `1`.

### Request

`name` is required and non-blank. `email` is required and must be a valid email address. `department` is optional and stored as given (no trimming or case change).

```json
{ "name": "John Doe", "email": "john@example.com", "department": "IT" }
```

Without department:

```json
{ "name": "John Doe", "email": "john@example.com" }
```

### Responses

- `201 Created`: Created Employee JSON, including its server-generated ID. `department` is null when omitted.
- `400 Bad Request`: `{"error":"Invalid employee data"}` for invalid or missing fields or malformed JSON.

```json
{ "id": 1, "name": "John Doe", "email": "john@example.com", "department": "IT" }
```
