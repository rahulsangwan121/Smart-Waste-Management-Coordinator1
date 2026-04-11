package com.waste.service;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServer {

    private FileService fileService = new FileService();

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // API routes
        server.createContext("/api/bins", new BinHandler());
        server.createContext("/api/add", new AddHandler());
        server.createContext("/api/delete", new DeleteHandler());
        server.createContext("/api/reset", new ResetHandler());
        server.createContext("/api/transit", new TransitHandler());
        server.createContext("/api/login", new LoginHandler());

        // ✅ Static handler (IMPORTANT)
        server.createContext("/", new StaticHandler());

        server.setExecutor(null);
        server.start();
    }

    // ---------------- API HANDLERS ----------------

    class LoginHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);

        System.out.println("BODY: " + body);

        if (body == null || !body.contains(",")) {
            sendResponse(exchange, "FAIL");
            return;
        }

        String[] parts = body.split(",");
        String username = parts[0].trim();
        String password = parts[1].trim();

        System.out.println("USER: " + username);
        System.out.println("PASS: " + password);

        List<String> users = fileService.readUsers();

        for (String u : users) {
            System.out.println("FILE: " + u);

            String[] userData = u.split(",");

            if (userData[0].trim().equals(username) &&
                userData[1].trim().equals(password)) {

                sendResponse(exchange, "SUCCESS");
                return;
            }
        }

        sendResponse(exchange, "FAIL");
    }
}

    class BinHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            List<String> bins = fileService.readBins();
            String response = String.join(";", bins);
            sendResponse(exchange, response);
        }
    }

    class AddHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            fileService.saveBin(body);
            sendResponse(exchange, "Added");
        }
    }

    class DeleteHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = readBody(exchange);
            fileService.deleteBin(id);
            sendResponse(exchange, "Deleted");
        }
    }

    class ResetHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);

        String[] partsBody = body.split(",");
        String id = partsBody[0];
        String user = partsBody[1];

        List<String> bins = fileService.readBins();
        List<String> updated = new ArrayList<>();

        for (String line : bins) {
            String[] parts = line.split(",");

            String assignedUser = parts.length >= 5 ? parts[4] : "";

            if (parts[0].equals(id)) {

                // 🔥 CHECK OWNER
                if (!assignedUser.equals(user)) {
                    sendResponse(exchange, "NOT_ALLOWED");
                    return;
                }

                updated.add(parts[0] + "," + parts[1] + ",0,1,");
            } else {
                updated.add(line);
            }
        }

        fileService.updateAllBins(updated);
        sendResponse(exchange, "DONE");
    }
}

    class TransitHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);

        // id + username aa raha hoga
        String[] partsBody = body.split(",");
        String id = partsBody[0];
        String user = partsBody[1];

        List<String> bins = fileService.readBins();
        List<String> updated = new ArrayList<>();

        for (String line : bins) {
            String[] parts = line.split(",");

            if (parts[0].equals(id)) {
                // assign user
                updated.add(parts[0] + "," + parts[1] + "," + parts[2] + ",2," + user);
            } else {
                updated.add(line);
            }
        }

        fileService.updateAllBins(updated);
        sendResponse(exchange, "Driver Assigned");
    }
}

    // ---------------- STATIC HANDLER (🔥 FIX) ----------------

 class StaticHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File("web" + path);

        System.out.println("Trying to load: " + file.getAbsolutePath());

        if (!file.exists()) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }

        // ✅ MIME TYPE FIX
        String contentType = "text/html";

        if (path.endsWith(".css")) {
            contentType = "text/css";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript";
        } else if (path.endsWith(".png")) {
            contentType = "image/png";
        } else if (path.endsWith(".jpg")) {
            contentType = "image/jpeg";
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);

        byte[] bytes = new FileInputStream(file).readAllBytes();

        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
    // ---------------- COMMON METHODS ----------------

    private String readBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        return reader.readLine();
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
