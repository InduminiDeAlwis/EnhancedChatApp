package server;

import common.Constants;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Minimal HTTP file server to accept uploads and serve files.
 * Uploads use a simple POST where the client sets header X-Filename.
 */
public class HttpFileServer {
    public static void startFileServer(int port) throws IOException {
        Path dir = Paths.get(Constants.FILES_DIR);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/upload", new UploadHandler(dir));
        server.createContext("/files", new FileHandler(dir));
        server.setExecutor(null);
        server.start();
        System.out.println("HTTP file server started on http://" + Constants.SERVER_IP + ":" + port);
    }

    static class UploadHandler implements HttpHandler {
        private final Path dir;

        UploadHandler(Path dir) { this.dir = dir; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Headers reqHeaders = exchange.getRequestHeaders();
            String filename = reqHeaders.getFirst("X-Filename");
            if (filename == null || filename.isEmpty()) {
                filename = "upload-" + UUID.randomUUID();
            }

            Path out = dir.resolve(filename).normalize();
            try (InputStream is = exchange.getRequestBody(); OutputStream os = Files.newOutputStream(out)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            }

            String fileUrl = "http://" + Constants.SERVER_IP + ":" + Constants.FILE_HTTP_PORT + "/files/" + out.getFileName().toString();
            String resp = "{\"url\":\"" + fileUrl + "\", \"filename\":\"" + out.getFileName().toString() + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] bytes = resp.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    static class FileHandler implements HttpHandler {
        private final Path dir;

        FileHandler(Path dir) { this.dir = dir; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI req = exchange.getRequestURI();
            String path = req.getPath(); // /files/filename
            String filename = path.substring(path.indexOf("/files/") + 7);
            Path file = dir.resolve(filename).normalize();
            if (!Files.exists(file) || !file.startsWith(dir)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String mime = Files.probeContentType(file);
            if (mime == null) mime = "application/octet-stream";
            exchange.getResponseHeaders().add("Content-Type", mime);
            exchange.sendResponseHeaders(200, Files.size(file));
            try (OutputStream os = exchange.getResponseBody(); InputStream is = Files.newInputStream(file)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) os.write(buf, 0, r);
            }
        }
    }
}
