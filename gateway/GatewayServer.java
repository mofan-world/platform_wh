import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class GatewayServer {

    private static final Path ISSUE_WEB = Path.of("/app/issue-web").toAbsolutePath().normalize();
    private static final String IDENTITY_API = getenv("IDENTITY_API_BASE", "http://identity-service:8080");
    private static final String ISSUE_API = getenv("ISSUE_API_BASE", "http://issuetracker-end:8080");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "content-length",
            "expect",
            "host",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(getenv("PORT", "80"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", GatewayServer::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.printf("Unified platform gateway listening on :%d%n", port);
    }

    private static void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getRawPath();
            if ("/platform-health".equals(path)) {
                send(exchange, 200, "text/plain; charset=utf-8", "ok\n".getBytes());
                return;
            }
            if (isIdentityPath(path)) {
                proxy(exchange, IDENTITY_API, path);
                return;
            }
            if (path.startsWith("/api/")) {
                proxy(exchange, ISSUE_API, path);
                return;
            }
            serveStatic(exchange, ISSUE_WEB, path);
        } catch (Exception error) {
            byte[] body = ("Gateway error: " + error.getMessage()).getBytes();
            send(exchange, 502, "text/plain; charset=utf-8", body);
        }
    }

    private static boolean isIdentityPath(String path) {
        return path.startsWith("/api/auth/")
                || path.equals("/api/admin/users")
                || path.startsWith("/api/admin/users/")
                || path.equals("/api/admin/roles")
                || path.startsWith("/api/admin/identity/")
                || path.startsWith("/api/users/");
    }

    private static void proxy(HttpExchange exchange, String base, String targetPath)
            throws IOException, InterruptedException {
        String query = exchange.getRequestURI().getRawQuery();
        URI target = URI.create(base + targetPath + (query == null ? "" : "?" + query));
        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                .timeout(Duration.ofSeconds(30))
                .method(
                        exchange.getRequestMethod(),
                        requestBody.length == 0
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofByteArray(requestBody)
                );

        for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
            if (HOP_BY_HOP_HEADERS.contains(header.getKey().toLowerCase(Locale.ROOT))) {
                continue;
            }
            for (String value : header.getValue()) {
                builder.header(header.getKey(), value);
            }
        }
        builder.header("X-Forwarded-Proto", "http");

        HttpResponse<byte[]> upstream = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        Headers responseHeaders = exchange.getResponseHeaders();
        upstream.headers().map().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                responseHeaders.put(name, values);
            }
        });
        exchange.sendResponseHeaders(upstream.statusCode(), upstream.body().length);
        exchange.getResponseBody().write(upstream.body());
        exchange.close();
    }

    private static void serveStatic(HttpExchange exchange, Path base, String rawPath) throws IOException {
        String cleanPath = rawPath == null || rawPath.equals("/") ? "/index.html" : rawPath;
        Path file = base.resolve(cleanPath.replaceFirst("^/+", "")).normalize();
        if (!file.startsWith(base)) {
            send(exchange, 403, "text/plain; charset=utf-8", "Forbidden".getBytes());
            return;
        }
        if (!Files.isRegularFile(file)) {
            file = base.resolve("index.html");
        }
        send(exchange, 200, contentType(file), Files.readAllBytes(file));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (name.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (name.endsWith(".woff")) {
            return "font/woff";
        }
        if (name.endsWith(".woff2")) {
            return "font/woff2";
        }
        return "application/octet-stream";
    }

    private static String getenv(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
