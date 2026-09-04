# Transporter Assignment on Lanes

Java 17 Spring Boot backend for deterministic transporter-to-lane optimization.

## Run

```bash
mvn spring-boot:run
```

Submit data with `POST /api/v1/transporters/input`, then calculate with `POST /api/v1/transporters/assignment` and body `{ "maxTransporters": 3 }`. OpenAPI is available at `/swagger-ui.html`.

The optimizer enumerates every transporter subset of size `1..K`. A subset is feasible only when every lane has a quote from a selected transporter; each lane is assigned exactly once to its cheapest selected quote. Candidates are ordered lexicographically by maximum selected transporter count, minimum aggregate quote cost, then ascending transporter IDs. Complexity is `O(sum(C(T,i)) * L * K)` for `i=1..K`, appropriate for the small assignment inputs; larger datasets should use ILP/constraint optimization or additional pruning.

Input is held in a thread-safe in-memory repository and replaced atomically on each successful submission. It is intentionally not durable.
