import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public final class EventApi {
    private static final Pattern ROUTE = Pattern.compile("/orders/([A-Za-z0-9_-]{1,64})/events/([A-Za-z0-9_-]{1,64})");
    private static final Set<String> TYPES = Set.of("placed", "reserved", "shipped", "failed");
    private final Map<String, List<OrderTimeline.Event>> orders = new HashMap<>();

    private record Reply(int status, String text) {}

    private synchronized Reply append(String order, OrderTimeline.Event event) {
        var existing = orders.getOrDefault(order, List.of());
        var candidate = new ArrayList<>(existing);
        candidate.add(event);
        try {
            var result = OrderTimeline.investigate(candidate);
            if (result.evidence().size() == existing.size()) {
                return new Reply(200, result.state().name());
            }
            orders.put(order, candidate);
            return new Reply(201, result.state().name());
        } catch (IllegalArgumentException invalid) {
            return new Reply(409, invalid.getMessage());
        }
    }

    private Reply route(HttpExchange request) throws IOException {
        var path = ROUTE.matcher(request.getRequestURI().getRawPath());
        if (!path.matches()) return new Reply(404, "unknown path");
        if (!request.getRequestMethod().equals("PUT")) {
            request.getResponseHeaders().set("Allow", "PUT");
            return new Reply(405, "use PUT");
        }
        byte[] payload = request.getRequestBody().readNBytes(33);
        if (payload.length > 32) return new Reply(413, "event type too long");
        var type = new String(payload, StandardCharsets.UTF_8);
        if (!TYPES.contains(type)) return new Reply(400, "unknown event type");
        return append(path.group(1), new OrderTimeline.Event(path.group(2), type));
    }

    static HttpServer start(int port) throws IOException {
        var api = new EventApi();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                var reply = api.route(exchange);
                var bytes = reply.text().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(reply.status(), bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        start(args.length == 0 ? 8080 : Integer.parseInt(args[0]));
    }
}
