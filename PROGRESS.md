## 14 November — order-state evidence

Day 1 complete. A failed inventory fixture replays an earlier checkout event and verifies both the final state and the exact evidence trail. A conflicting ID is rejected. Java 17 compilation, seed checks and domain fixtures are the acceptance checks for this milestone. Next: event ingestion API with duplicate handling. No persistence or model summaries yet.

## 14 November — ingestion API

Day 2 complete. Java 17 loopback HTTP fixtures cover accepted events, retry after a later transition, conflicting IDs, invalid transitions followed by a corrected request, per-order ID reuse, and invalid or oversized bodies. The next milestone is persistence across restarts. [Java CI history](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 15 November — restart persistence

Day 3 adds optional local file storage. The HTTP fixture restarts the server, retries events from two orders and advances the restored reservation to shipped. Next is read-only investigation. [Java checks](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 16 November — read-only investigations

Day 4 exposes state and ordered event evidence through GET. The restart fixture checks the exact history after duplicate and rejected writes, unknown-order handling, and method rejection. Next is an evidence-linked summary with a mock provider. [Java checks](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 17 November — cited mock summaries

Day 5 adds a deterministic summary provider through the GET summary endpoint. The HTTP fixture verifies exact failure-summary text and citations from restored history and rejects writes to the endpoint. Next is failure-scenario evaluation. [Java checks](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 18 November — failure evaluation

Day 6 evaluates terminal-state preservation, malformed/duplicate/invalid-transition/empty persisted histories, retry after storage repair and missing-order summaries over HTTP. Corrupt history remains intact after rejected writes. All fixtures passed locally. Packaged demo, limitations and a local LinkedIn draft remain for Day 7. [Java checks](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 18 November — packaged local MVP

Added one-command demo and CI execution, printed the checked mock summary, documented reproduction and practical limits, and saved the LinkedIn draft. All scoped acceptance artifacts are present. Completion is conditional on remote CI passing.
