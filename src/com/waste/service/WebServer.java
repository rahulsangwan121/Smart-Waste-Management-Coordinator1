package com.waste.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServer {
    public static void startServer() throws IOException {
        // --- CHANGE 1: Render ke liye dynamic port ---
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

        // 2. ADD NEW BIN
        server.createContext("/api/add", (exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String data = new String(exchange.getRequestBody().readAllBytes());
                new FileService().saveBin(data);
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // 3. SET IN-TRANSIT
        server.createContext("/api/transit", (exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String binID = new String(exchange.getRequestBody().readAllBytes()).trim();
                updateStatus(binID, false);
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // 4. RESET BIN
        server.createContext("/api/reset", (exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String binID = new String(exchange.getRequestBody().readAllBytes()).trim();
                updateStatus(binID, true);
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // HTML, JS, CSS files serve karne ke liye naya context
server.createContext("/web", (exchange -> {
    String path = exchange.getRequestURI().getPath();
    // Default file index.html rakhein
    if (path.equals("/web") || path.equals("/web/")) {
        path = "/web/index.html";
    }

    // File ko read karein (Root se)
    File file = new File("." + path); 
    if (file.exists()) {
        byte[] content = new FileInputStream(file).readAllBytes();
        
        // Content-Type set karein (taaki browser samajh sake ye kya hai)
        if (path.endsWith(".html")) exchange.getResponseHeaders().add("Content-Type", "text/html");
        else if (path.endsWith(".css")) exchange.getResponseHeaders().add("Content-Type", "text/css");
        else if (path.endsWith(".js")) exchange.getResponseHeaders().add("Content-Type", "application/javascript");

        exchange.sendResponseHeaders(200, content.length);
        OutputStream os = exchange.getResponseBody();
        os.write(content);
        os.close();
    } else {
        String error = "File Not Found: " + path;
        exchange.sendResponseHeaders(404, error.length());
        OutputStream os = exchange.getResponseBody();
        os.write(error.getBytes());
        os.close();
    }
}));

        System.out.println("Java Server started on port: " + port);
        server.start();
    }

    // --- CHANGE 2: CORS aur OPTIONS handling helper ---
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void updateStatus(String binID, boolean isReset) {
        FileService fs = new FileService();
        List<String> bins = fs.readBins();
        List<String> updated = new ArrayList<>();
        for (String line : bins) {
            String[] p = line.split(",");
            if (p[0].equals(binID)) {
                if (isReset) updated.add(p[0] + "," + p[1] + ",0,1");
                else updated.add(p[0] + "," + p[1] + "," + p[2] + ",2");
            } else {
                updated.add(line);
            }
        }
        fs.updateAllBins(updated);
    }
}
