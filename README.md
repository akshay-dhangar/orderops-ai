# orderops-ai

Investigate order failures from event timelines and cite the evidence.

**Status: initial working component; full MVP is planned, not complete.**

Implemented now: Reduce ordered domain events into an order state.

Backend: Java. This seed has no live model calls, credentials, external writes, or deployment.

## Verify

```sh
javac --release 17 -d build src/*.java test/*.java
java -cp build OrderTimeline
java -cp build TimelineFixtures
java -cp build IngestionFixtures
```

## One-week MVP

See [ROADMAP.md](ROADMAP.md) for seven bounded development days and acceptance criteria. Work is scheduled sequentially across the portfolio, not on every repository every day. The week starts when this project becomes active.

## Evidence

The current automated checks validate the small implemented component only. They do not establish end-to-end AI quality, scalability, or production readiness. Later demos and LinkedIn write-ups should cite measured behavior and disclosed limitations.

For one ordered timeline, investigate(events) returns the final state and an immutable sequence of transitions citing their original event IDs. An identical retry contributes no extra evidence. Reusing an ID with another event type rejects the timeline. This component does not infer why a failure occurred.

## Ingest an event locally

After compiling, run `java -cp build EventApi`. In another terminal:

    curl -i -X PUT --data-binary placed http://127.0.0.1:8080/orders/demo/events/checkout-1

Use a distinct event ID for each transition. IDs accept 1–64 ASCII letters, digits, underscores or hyphens. The body is exactly one of placed, reserved, shipped or failed. A new event returns 201 and the current state. An identical retry returns 200 and the latest state. Conflicting IDs or invalid transitions return 409 and preserve the timeline. IDs are scoped to an order. Restarting the local server clears its in-memory history.

For persistent storage run `java -cp build EventApi 8080 /path/to/order-data`. Reuse that directory on restart. A lock serializes updates and an atomic rename publishes each accepted timeline on the local filesystem. The default launch mode keeps orders in memory.

Read the current state and chronological evidence with `curl http://127.0.0.1:8080/orders/demo/investigation`. Each line after the state names the event ID and its before/after states. Unknown orders return 404 and unreadable timelines return 503.

Get a deterministic summary with `curl http://127.0.0.1:8080/orders/demo/summary`. The response identifies provider mock-v1 and cites an event ID for each state claim. Failed orders report that the cause is unknown from the recorded event types. Live model integration and quality evaluation remain future work.
