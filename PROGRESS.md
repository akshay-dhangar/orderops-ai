## 14 November — order-state evidence

Day 1 complete. A failed inventory fixture replays an earlier checkout event and verifies both the final state and the exact evidence trail. A conflicting ID is rejected. Java 17 compilation, seed checks and domain fixtures are the acceptance checks for this milestone. Next: event ingestion API with duplicate handling. No persistence or model summaries yet.

## 14 November — ingestion API

Day 2 complete. Java 17 loopback HTTP fixtures cover accepted events, retry after a later transition, conflicting IDs, invalid transitions followed by a corrected request, per-order ID reuse, and invalid or oversized bodies. The next milestone is persistence across restarts. [Java CI history](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)

## 15 November — restart persistence

Day 3 adds optional local file storage. The HTTP fixture restarts the server, retries events from two orders and advances the restored reservation to shipped. Next is read-only investigation. [Java checks](https://github.com/akshay-dhangar/orderops-ai/actions/workflows/ci.yml)
