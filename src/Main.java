

import com.waste.service.WebServer;

public class Main {
    public static void main(String[] args) {
        try {
            WebServer server = new WebServer();
            server.startServer();
            System.out.println("Dashboard Backend is Running...");
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}