package com.waste.service;
import java.io.*;
import java.util.*;

public class FileService {
    private final String FILE_PATH = "bins.txt";

    public void saveBin(String data) {
        String[] newDataParts = data.split(",");
        String newID = newDataParts[0];

        List<String> allBins = readBins();
        List<String> updatedBins = new ArrayList<>();
        boolean found = false;

        // Pehle purani list mein check karo ki ID exist karti hai ya nahi
        for (String line : allBins) {
            String[] parts = line.split(",");
            if (parts[0].equals(newID)) {
                // Agar ID mil gayi, toh purani line ki jagah nayi data line daal do
                // Agar naye data mein status nahi hai, toh default "1" laga do
                String updatedLine = data;
                if (newDataParts.length == 3) updatedLine += ",1";
                updatedBins.add(updatedLine);
                found = true;
            } else {
                updatedBins.add(line);
            }
        }

        // Agar ID nahi mili (matlab naya bin hai), toh use list mein add kar do
        if (!found) {
            String newLine = data;
            if (newDataParts.length == 3) newLine += ",1";
            updatedBins.add(newLine);
        }

        // Ab poori updated list ko wapas file mein likh do
        updateAllBins(updatedBins);
    }

    public List<String> readBins() {
        List<String> bins = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                bins.add(line);
            }
        } catch (IOException e) { /* File doesn't exist yet */ }
        return bins;
    }

    public void updateAllBins(List<String> updatedLines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String line : updatedLines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }


    public List<String> readUsers() {
    List<String> users = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
        String line;
        while ((line = br.readLine()) != null) {
            users.add(line);
        }
    } catch (IOException e) {
        System.out.println("Users file not found");
    }
    return users;
}
    
    public void deleteBin(String binID) {
    List<String> allBins = readBins();
    List<String> updatedBins = new ArrayList<>();

    for (String line : allBins) {
        String[] parts = line.split(",");
        if (!parts[0].equals(binID)) {
            updatedBins.add(line);
        }
    }

    updateAllBins(updatedBins);
}
}
