package com.waste.service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServer {

    private FileService fileService = new FileService();

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/bins", new BinHandler());
        server.createContext("/api/add", new AddHandler());
        server.createContext("/api/delete", new DeleteHandler());
        server.createContext("/api/reset", new ResetHandler());
        server.createContext("/api/transit", new TransitHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port 8080");
    }

    // ✅ GET BINS
    class BinHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            List<String> bins = fileService.readBins();

            String response = String.join(";", bins);

            sendResponse(exchange, response);
        }
    }

    // ✅ ADD BIN
    class AddHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);

            fileService.saveBin(body);

            sendResponse(exchange, "Added");
        }
    }

    // ✅ DELETE BIN
    class DeleteHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = readBody(exchange);

            fileService.deleteBin(id);

            sendResponse(exchange, "Deleted");
        }
    }

    // ✅ RESET BIN (mark empty)
    class ResetHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = readBody(exchange);

            List<String> bins = fileService.readBins();
            List<String> updated = new ArrayList<>();

            for (String line : bins) {
                String[] parts = line.split(",");

                if (parts[0].equals(id)) {
                    updated.add(parts[0] + "," + parts[1] + ",0,1");
                } else {
                    updated.add(line);
                }
            }

            fileService.updateAllBins(updated);

            sendResponse(exchange, "Reset Done");
        }
    }

    // ✅ TRANSIT (driver assigned)
    class TransitHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = readBody(exchange);

            List<String> bins = fileService.readBins();
            List<String> updated = new ArrayList<>();

            for (String line : bins) {
                String[] parts = line.split(",");

                if (parts[0].equals(id)) {
                    updated.add(parts[0] + "," + parts[1] + "," + parts[2] + ",2");
                } else {
                    updated.add(line);
                }
            }

            fileService.updateAllBins(updated);

            sendResponse(exchange, "Driver Assigned");
        }
    }

    // 🔧 COMMON METHODS

    private String readBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        return reader.readLine();
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // CORS fix

        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
