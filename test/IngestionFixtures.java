import java.net.URI;
import java.net.http.*;

public final class IngestionFixtures {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static void put(URI uri, String type, int status, String body) throws Exception {
        var request = HttpRequest.newBuilder(uri).PUT(HttpRequest.BodyPublishers.ofString(type)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != status || !response.body().equals(body))
            throw new AssertionError(uri + " returned " + response.statusCode() + " " + response.body());
    }

    public static void main(String[] args) throws Exception {
        var server = EventApi.start(0);
        try {
            var root = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
            var placed = root.resolve("/orders/A/events/1");
            var next = root.resolve("/orders/A/events/2");
            put(placed, "placed", 201, "PLACED");
            put(next, "shipped", 409, "ship requires reservation");
            put(next, "reserved", 201, "RESERVED");
            put(placed, "placed", 200, "RESERVED");
            put(placed, "failed", 409, "conflicting event id: 1");
            put(root.resolve("/orders/B/events/1"), "failed", 201, "FAILED");
            put(next, "bogus", 400, "unknown event type");
            put(next, "x".repeat(33), 413, "event type too long");
        } finally {
            server.stop(0);
        }
    }
}
