# orderops-ai

Investigate order failures from event timelines and cite the evidence.

**Status: initial working component; full MVP is planned, not complete.**

Implemented now: Reduce ordered domain events into an order state.

Backend: Java. This seed has no live model calls, credentials, external writes, or deployment.

## Verify

```sh
javac --release 17 -d build src/OrderTimeline.java
java -cp build OrderTimeline
```

## One-week MVP

See [ROADMAP.md](ROADMAP.md) for seven bounded development days and acceptance criteria. Work is scheduled sequentially across the portfolio, not on every repository every day. The week starts when this project becomes active.

## Evidence

The current automated checks validate the small implemented component only. They do not establish end-to-end AI quality, scalability, or production readiness. Later demos and LinkedIn write-ups should cite measured behavior and disclosed limitations.
