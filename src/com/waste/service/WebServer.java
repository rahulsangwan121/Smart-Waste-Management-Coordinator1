package com.waste.service;

import com.sun.net.httpserver.HttpServer;
import com.waste.service.FileService;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServer {
    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1. GET ALL BINS
        server.createContext("/api/bins", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
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
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String data = new String(exchange.getRequestBody().readAllBytes());
                new FileService().saveBin(data);
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // 3. SET IN-TRANSIT (Lock Task)
        server.createContext("/api/transit", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String binID = new String(exchange.getRequestBody().readAllBytes()).trim();
                updateStatus(binID, false); // false = status 2 (Transit)
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        // 4. RESET BIN (Task Done)
        server.createContext("/api/reset", (exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String binID = new String(exchange.getRequestBody().readAllBytes()).trim();
                updateStatus(binID, true); // true = level 0, status 1
                exchange.sendResponseHeaders(200, 0);
            }
            exchange.close();
        }));

        System.out.println("Java Server started at http://localhost:8080");
        server.start();
    }

    private static void updateStatus(String binID, boolean isReset) {
        FileService fs = new FileService();
        List<String> bins = fs.readBins();
        List<String> updated = new ArrayList<>();
        for (String line : bins) {
            String[] p = line.split(",");
            if (p[0].equals(binID)) {
                if (isReset) updated.add(p[0] + "," + p[1] + ",0,1"); // Reset level and status
                else updated.add(p[0] + "," + p[1] + "," + p[2] + ",2"); // Set transit
            } else {
                updated.add(line);
            }
        }
        fs.updateAllBins(updated);
    }
}