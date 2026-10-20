# Story 3 – Employee Search by Department

## User Story

As an API consumer, I want to filter employees by department so that I can retrieve employees belonging to a specific department.

## Requirements

1. Add `department` (`String`) to `Employee` and `CreateEmployeeRequest`.
2. Support an optional `department` query parameter in `GET /api/employees`.
3. Match department names case-insensitively.
4. Return all employees when `department` is omitted.
5. Return HTTP `200` with an empty list when no employees match.
6. Preserve existing `GET` by ID and `POST` behavior for existing clients.
7. Update Swagger/OpenAPI documentation.
8. Add focused tests and run existing regression tests.

## Acceptance Criteria

- `GET /api/employees?department=IT` returns matching employees.
- `GET /api/employees?department=it` produces equivalent results.
- `GET /api/employees` returns all employees.
- Unknown departments return HTTP `200` with `[]`.
- Existing API behavior remains compatible.
- Swagger documents the optional query parameter and updated schemas.
- All existing and new tests pass.

## Constraints

- Keep implementation minimal.
- Reuse the existing Spring Boot architecture.
- Do not add a database or new services.
- Do not modify existing Copilot agents, hooks, or Story 1/2 evidence.
- Do not implement the feature yet.
- Do not commit, push, or create a PR.
