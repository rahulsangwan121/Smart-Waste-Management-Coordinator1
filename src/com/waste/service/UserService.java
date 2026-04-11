package com.waste.service;

import java.io.*;
import java.util.*;

public class UserService {

    private final String FILE_PATH = "users.txt";

    public boolean validateUser(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts[0].equals(username) && parts[1].equals(password)) {
                    return true; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; 
    }
}
