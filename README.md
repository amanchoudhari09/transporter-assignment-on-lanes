# FreightFox Transporter Assignment

Java 17 Spring Boot backend that assigns every lane to exactly one transporter while respecting a maximum transporter count.

## Optimization objectives

Candidates are compared lexicographically:

1. Feasibility: every lane is covered exactly once.
2. Maximize the number of selected transporters, without exceeding `maxTransporters`.
3. Minimize total quote cost.
4. Choose the lexicographically smallest sorted transporter-ID list.

The optimizer exhaustively enumerates transporter subsets. For each feasible subset it assigns each lane its cheapest available quote, then compares the candidate using the ordering above. Complexity is `O(sum(C(T,i)) * L * T)` for `i = 1..K`; this is intentional for small assignment inputs. Larger datasets need pruning, branch-and-bound, ILP, or constraint programming.

## Architecture

```text
HTTP Request -> Controller -> Service -> Repository
                                  \-> Optimizer -> Assignment Result
```

The repository is a thread-safe in-memory store of the latest submitted dataset. No database or external service is required.

## API

### Submit input

`POST /api/v1/transporters/input`

```json
{"lanes":[{"id":1,"origin":"A","destination":"B"}],"transporters":[{"id":1,"name":"Fast","laneQuotes":[{"laneId":1,"quote":75000}]}]}
```

### Calculate assignment

`POST /api/v1/transporters/assignment`

```json
{"maxTransporters":1}
```

The response contains `totalCost`, deterministic lane-ascending assignments, and sorted `selectedTransporters`. Validation and business errors use `{status, code, message}`; impossible coverage returns HTTP 422 and assignment before input returns HTTP 404.

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Run and test

```bash
mvn clean test
mvn clean package
mvn spring-boot:run
```

Tests cover optimizer objectives, coverage, constraints, deterministic ordering, edge cases, service validation, HTTP contracts, and the supplied CSV fixture. The API is deliberately non-persistent and exhaustive enumeration is not intended for large transporter counts.
