package com.waste.models;

public class Driver extends User {
    private String assignedVehicle;

    public Driver(String name, String id, String vehicle) {
        super(name, id); // Parent constructor ko call karna
        this.assignedVehicle = vehicle;
    }

    public String getDriverDetails() {
        return "Driver: " + name + " (ID: " + id + ") - Vehicle: " + assignedVehicle;
    }
}