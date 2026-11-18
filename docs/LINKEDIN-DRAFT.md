# LinkedIn draft

I built OrderOps, a Java backend that reconstructs an order's state from its event history and exposes the evidence behind each transition.

The local demo covers repeated delivery, conflicting event IDs, restart persistence and corrupted histories. A read-only endpoint returns the event trail. A deterministic mock summary cites those same event IDs and explicitly reports when the failure cause is unknown.

The complete demo runs with one shell command and is checked in GitHub Actions. The next AI step is integrating and evaluating a live model against this evidence workflow. The current summary provider is a mock.

Repository and reproduction instructions
https://github.com/akshay-dhangar/orderops-ai

Draft saved locally for review. Publication remains pending.
