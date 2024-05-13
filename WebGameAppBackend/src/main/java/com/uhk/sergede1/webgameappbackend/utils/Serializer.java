package com.uhk.sergede1.webgameappbackend.utils;

import java.io.*;
import java.util.Base64;

public class Serializer<T> {

    public String serialize(T input) {
        try {
            // Create a ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Create an ObjectOutputStream
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            // Write the object to the ObjectOutputStream
            oos.writeObject(input);

            // Flush and close the ObjectOutputStream
            oos.flush();
            oos.close();

            // Convert the ByteArrayOutputStream to a byte array
            byte[] bytes = baos.toByteArray();

            // Convert the byte array to a Base64 encoded String
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public T deserialize(String input) {
        try {
            // Decode the Base64 encoded String to a byte array
            byte[] bytes = Base64.getDecoder().decode(input);

            // Create a ByteArrayInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // Create an ObjectInputStream
            ObjectInputStream ois = new ObjectInputStream(bais);

            // Read the object from the ObjectInputStream and cast it to the specified class
            Object obj = ois.readObject();

            // Close the ObjectInputStream
            ois.close();

            // Return the object cast to the specified class
            return (T) obj;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
