import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Pattern;

public final class EventApi {
    private static final Pattern ROUTE = Pattern.compile("/orders/([A-Za-z0-9_-]{1,64})/events/([A-Za-z0-9_-]{1,64})");
    private static final Pattern INVESTIGATION = Pattern.compile("/orders/([A-Za-z0-9_-]{1,64})/(investigation|summary)");
    private static final Set<String> TYPES = Set.of("placed", "reserved", "shipped", "failed");
    private final Map<String, List<OrderTimeline.Event>> orders = new HashMap<>();

    private final Path directory;

    private EventApi(Path directory) throws IOException {
        this.directory = directory;
        if (directory != null) Files.createDirectories(directory);
    }

    private record Reply(int status, String text) {}

    private synchronized Reply append(String order, OrderTimeline.Event event) {
        if (directory == null) return apply(order, event, orders.getOrDefault(order, List.of()), null);
        try (var channel = FileChannel.open(directory.resolve(".lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var lock = channel.lock()) {
            Path file = timelineFile(order);
            var history = load(file);
            return apply(order, event, history, file);
        } catch (IOException | IllegalArgumentException | java.nio.channels.OverlappingFileLockException failure) {
            return new Reply(503, "timeline storage unavailable");
        }
    }

    private Path timelineFile(String order) {
        String key = HexFormat.of().formatHex(order.getBytes(StandardCharsets.US_ASCII));
        return directory.resolve(key + ".events");
    }

    private List<OrderTimeline.Event> load(Path file) throws IOException {
        var history = new ArrayList<OrderTimeline.Event>();
        if (!Files.notExists(file)) {
            for (String line : Files.readAllLines(file)) {
                String[] fields = line.split("\t", -1);
                if (fields.length != 2 || !fields[0].matches("[A-Za-z0-9_-]{1,64}") || !TYPES.contains(fields[1]))
                    throw new IOException("invalid timeline record");
                history.add(new OrderTimeline.Event(fields[0], fields[1]));
            }
            if (history.isEmpty() || OrderTimeline.investigate(history).evidence().size() != history.size())
                throw new IOException("invalid stored timeline");
        }
        return history;
    }

    private synchronized Reply investigate(String order, boolean summary) {
        try {
            var history = directory == null ? orders.getOrDefault(order, List.of()) : load(timelineFile(order));
            if (history.isEmpty()) return new Reply(404, "unknown order");
            var result = OrderTimeline.investigate(history);
            if (summary) return new Reply(200, new MockSummaryProvider().summarize(order, result));
            var body = new StringBuilder("order ").append(order).append(" state ").append(result.state()).append('\n');
            for (var step : result.evidence()) {
                body.append(step.eventId()).append(' ').append(step.before()).append(" -> ")
                    .append(step.after()).append('\n');
            }
            return new Reply(200, body.toString());
        } catch (IOException | IllegalArgumentException failure) {
            return new Reply(503, "timeline storage unavailable");
        }
    }

    private Reply apply(String order, OrderTimeline.Event event, List<OrderTimeline.Event> existing, Path file) {
        var candidate = new ArrayList<>(existing);
        candidate.add(event);
        try {
            var result = OrderTimeline.investigate(candidate);
            if (result.evidence().size() == existing.size()) {
                return new Reply(200, result.state().name());
            }
            if (file == null) {
                orders.put(order, candidate);
            } else {
                Path temp = Files.createTempFile(directory, "timeline-", ".tmp");
                try {
                    Files.write(temp, candidate.stream().map(e -> e.id() + "\t" + e.type()).toList());
                    Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    Files.deleteIfExists(temp);
                }
            }
            return new Reply(201, result.state().name());
        } catch (IOException failure) {
            return new Reply(503, "timeline storage unavailable");
        } catch (IllegalArgumentException invalid) {
            return new Reply(409, invalid.getMessage());
        }
    }

    private Reply route(HttpExchange request) throws IOException {
        var investigation = INVESTIGATION.matcher(request.getRequestURI().getRawPath());
        if (investigation.matches()) {
            if (!request.getRequestMethod().equals("GET")) {
                request.getResponseHeaders().set("Allow", "GET");
                return new Reply(405, "use GET");
            }
            return investigate(investigation.group(1), investigation.group(2).equals("summary"));
        }
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
        return start(port, null);
    }

    static HttpServer start(int port, Path directory) throws IOException {
        var api = new EventApi(directory);
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
        start(args.length == 0 ? 8080 : Integer.parseInt(args[0]), args.length > 1 ? Path.of(args[1]) : null);
    }
}
