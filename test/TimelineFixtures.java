import java.util.List;

public class TimelineFixtures {
    static OrderTimeline.Event event(String id, String type) {
        return new OrderTimeline.Event(id, type);
    }

    public static void main(String[] args) {
        var placed = event("checkout-7", "placed");
        var failed = event("inventory-8", "failed");
        var replay = OrderTimeline.investigate(List.of(placed, failed, placed));
        var expected = List.of(
            new OrderTimeline.Transition("checkout-7", OrderTimeline.State.NEW, OrderTimeline.State.PLACED),
            new OrderTimeline.Transition("inventory-8", OrderTimeline.State.PLACED, OrderTimeline.State.FAILED));
        if (!replay.evidence().equals(expected) || replay.state() != OrderTimeline.State.FAILED)
            throw new AssertionError("failure evidence lost or replay counted twice");
        try {
            OrderTimeline.investigate(List.of(placed, event("checkout-7", "reserved")));
            throw new AssertionError("conflicting duplicate accepted");
        } catch (IllegalArgumentException expectedError) {
            if (!expectedError.getMessage().contains("checkout-7")) throw expectedError;
        }
        System.out.println("Domain fixtures passed");
    }
}
