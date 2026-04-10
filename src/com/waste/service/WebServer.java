package com.waste.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServer {
    public static void startServer() throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 1. GET ALL BINS
        server.createContext("/api/bins", (exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            FileService fs = new FileService();
            List<String> bins = fs.readBins();
            String response = String.join(";", bins);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }));

        // 2. SET IN-TRANSIT (With Driver Info)
        server.createContext("/api/transit", (exchange -> {
            addCorsHeaders(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String data = new String(exchange.getRequestBody().readAllBytes()).trim();
                // Data format: "binID,driverName"
                String[] parts = data.split(",");
                if (parts.length == 2) {
                    updateStatus(parts[0], false, parts[1]);
                }
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // 3. RESET BIN
        server.createContext("/api/reset", (exchange -> {
            addCorsHeaders(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String binID = new String(exchange.getRequestBody().readAllBytes()).trim();
                updateStatus(binID, true, "None");
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // Static File Server (src/web path)
        server.createContext("/web", (exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/web") || path.equals("/web/")) path = "/web/index.html";
            File file = new File("./src" + path); 
            if (file.exists() && !file.isDirectory()) {
                if (path.endsWith(".html")) exchange.getResponseHeaders().add("Content-Type", "text/html");
                else if (path.endsWith(".css")) exchange.getResponseHeaders().add("Content-Type", "text/css");
                else if (path.endsWith(".js")) exchange.getResponseHeaders().add("Content-Type", "application/javascript");
                byte[] content = new FileInputStream(file).readAllBytes();
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }
        }));

        server.start();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void updateStatus(String binID, boolean isReset, String driverName) {
        FileService fs = new FileService();
        List<String> bins = fs.readBins();
        List<String> updated = new ArrayList<>();
        for (String line : bins) {
            String[] p = line.split(",");
            if (p[0].equals(binID)) {
                if (isReset) updated.add(p[0] + "," + p[1] + ",0,1,None");
                else updated.add(p[0] + "," + p[1] + "," + p[2] + ",2," + driverName);
            } else {
                updated.add(line);
            }
        }
        fs.updateAllBins(updated);
    }
}
