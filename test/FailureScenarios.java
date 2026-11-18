import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;

public final class FailureScenarios {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3)).build();

    private static String request(URI root, String method, String path, String body, int expected) throws Exception {
        var request = HttpRequest.newBuilder(root.resolve(path)).timeout(Duration.ofSeconds(5))
            .method(method, HttpRequest.BodyPublishers.ofString(body)).build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != expected)
            throw new AssertionError(path + " expected " + expected + ", got " + response.statusCode());
        return response.body();
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("orderops-failures");
        var server = EventApi.start(0, directory);
        try {
            URI root = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
            request(root, "PUT", "/orders/A/events/1", "placed", 201);
            request(root, "PUT", "/orders/A/events/2", "reserved", 201);
            request(root, "PUT", "/orders/A/events/3", "shipped", 201);
            String before = request(root, "GET", "/orders/A/investigation", "", 200);
            request(root, "PUT", "/orders/A/events/4", "failed", 409);
            if (!before.equals(request(root, "GET", "/orders/A/investigation", "", 200)))
                throw new AssertionError("terminal rejection changed evidence");
            System.out.println("PASS terminal order preserves evidence");

            Path file = directory.resolve("41.events");
            byte[] good = Files.readAllBytes(file);
            for (String corrupt : new String[]{"malformed\n", "1\tplaced\n1\tplaced\n", "1\tshipped\n", ""}) {
                Files.writeString(file, corrupt);
                request(root, "GET", "/orders/A/investigation", "", 503);
                request(root, "GET", "/orders/A/summary", "", 503);
                request(root, "PUT", "/orders/A/events/5", "failed", 503);
                if (!Files.readString(file).equals(corrupt))
                    throw new AssertionError("corrupt evidence overwritten");
            }
            System.out.println("PASS four corrupt histories rejected by reads and writes");
            Files.write(file, good);
            if (!request(root, "PUT", "/orders/A/events/3", "shipped", 200).equals("SHIPPED"))
                throw new AssertionError("retry after storage repair failed");
            System.out.println("PASS repaired history accepts identical retry");
            request(root, "GET", "/orders/unknown/summary", "", 404);
            System.out.println("PASS unknown order has no summary");
        } finally {
            server.stop(0);
            try (var files = Files.list(directory)) {
                for (Path file : files.toList()) Files.delete(file);
            }
            Files.delete(directory);
        }
    }
}
