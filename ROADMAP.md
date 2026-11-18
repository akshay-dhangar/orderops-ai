# orderops-ai: seven-day MVP

Goal: Investigate order failures from event timelines and cite the evidence.

Status: scoped local MVP implemented. Acceptance covers the Java local service and deterministic mock summaries described in docs/DEMO.md.

- [x] Day 1: Order state reducer and domain fixtures.
- [x] Day 2: Add event ingestion API with duplicate handling.
- [x] Day 3: Persist order timelines.
- [x] Day 4: Expose read-only investigation tools.
- [x] Day 5: Add evidence-linked model summaries with a mock provider.
- [x] Day 6: Evaluate common failure scenarios.
- [x] Day 7: Publish a reproducible local demo and limitations.

## Definition of done

- A documented local command runs the scoped end-to-end example.
- Meaningful positive and failure-path tests pass locally and in CI.
- README distinguishes implemented features, mocks, measured results, and limitations.
- Example inputs and outputs are reproducible.
- A short demo script and evidence-based LinkedIn draft are saved locally.
- Real model quality is not claimed without a live evaluation.

Seven days is a target. Reduce scope or record unfinished work explicitly; never mark a project complete merely because its week ended.

18 November acceptance review: demo command, reproducible output, failure fixtures, documented limits and local LinkedIn draft are present. Local checks pass. Portfolio completion awaits GitHub CI on this commit. Live-model evaluation remains future work.
