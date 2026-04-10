package com.waste.models;

public class Dustbin {
    private String binID;
    private String location;
    private int fillLevel;

    // Constructor
    public Dustbin(String binID, String location, int fillLevel) {
        this.binID = binID;
        this.location = location;
        this.fillLevel = fillLevel;
    }

    // Getters and Setters (Encapsulation)
    public String getBinID() { return binID; }
    public String getLocation() { return location; }
    public int getFillLevel() { return fillLevel; }

    public void setFillLevel(int fillLevel) {
        if(fillLevel >= 0 && fillLevel <= 100) {
            this.fillLevel = fillLevel;
        }
    }
}