/** Deterministic summary fixture for exercising the investigation workflow. */
public final class MockSummaryProvider {
    public String summarize(String order, OrderTimeline.Result result) {
        if (result.evidence().isEmpty()) {
            throw new IllegalArgumentException("summary requires recorded events");
        }
        var latest = result.evidence().get(result.evidence().size() - 1);
        var body = new StringBuilder("provider mock-v1\n");
        body.append("Order ").append(order).append(" is ").append(result.state())
            .append(" [event ").append(latest.eventId()).append("].\n");
        for (var transition : result.evidence()) {
            body.append("State changed from ").append(transition.before())
                .append(" to ").append(transition.after()).append(" [event ")
                .append(transition.eventId()).append("].\n");
        }
        if (result.state() == OrderTimeline.State.FAILED) {
            body.append("The failure cause is unknown from the recorded event types.\n");
        }
        return body.toString();
    }
}
