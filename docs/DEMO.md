# Reproduce OrderOps locally

With a JDK supporting Java 17 and a POSIX shell, run from the checkout:

```sh
sh scripts/demo.sh
```

The script compiles the application and fixtures, starts ephemeral loopback servers, sends real HTTP requests and cleans up fixture data. It prints the following summary from a restored failed order:

```text
provider mock-v1
Order B is FAILED [event 1].
State changed from NEW to FAILED [event 1].
The failure cause is unknown from the recorded event types.
```

It then prints passing checks for terminal-order evidence, four corrupt-history variants, a retry after storage repair and an unknown-order summary. The final line confirms local ingestion, restart, cited mock summary and failure checks. Any failed assertion exits nonzero. CI runs the same script.

## Scope and limits

The local MVP accepts four ordered event types, validates state transitions, stores optional per-order files and exposes evidence and summaries through GET. Identical retries retain one transition. Conflicting IDs and invalid transitions return 409.

The summary provider is a deterministic fixture. Live LLM adapters, model-quality evaluation and actual failure-cause inference remain future work. Events contain IDs and types only, so delay analysis needs additional timestamps and service context.

Storage has been tested across application restarts and uses atomic rename on a local filesystem. Power-loss durability, network filesystems and multi-host deployment remain unverified. The service binds to loopback, has no authentication, reads each order history into memory and uses the JDK default executor. Production deployment requires resource limits, authentication and operational hardening.

The completed scope uses Java 17 and the JDK HTTP server. Spring Boot migration, a database, a browser UI and hosted deployment remain future extensions.
