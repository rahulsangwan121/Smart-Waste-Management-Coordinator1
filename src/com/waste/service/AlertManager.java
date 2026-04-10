package com.waste.service;

public class AlertManager implements NotificationService {
    @Override
    public void sendAlert(String location, String driverName) {
        // Real world mein yahan SMS/Email logic hota hai
        System.out.println("SYSTEM ALERT: Dustbin at " + location + " is FULL.");
        System.out.println("Task assigned to Driver: " + driverName);
    }
}