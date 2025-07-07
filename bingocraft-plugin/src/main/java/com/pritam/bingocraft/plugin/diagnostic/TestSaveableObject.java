package com.pritam.bingocraft.plugin.diagnostic;

import com.pritam.bingocraft.api.persistence.SaveableObject;

/**
 * A simple test implementation of SaveableObject for diagnostic purposes.
 */
public class TestSaveableObject implements SaveableObject {
    private String data;
    private long timestamp;
    private int counter;

    public TestSaveableObject() {
        this.data = "";
        this.timestamp = 0;
        this.counter = 0;
    }

    public TestSaveableObject(String data, long timestamp, int counter) {
        this.data = data;
        this.timestamp = timestamp;
        this.counter = counter;
    }

    @Override
    public String toString() {
        return data + "|" + timestamp + "|" + counter;
    }

    @Override
    public void fromString(String data) {
        if (data == null || data.isBlank()) {
            this.data = "";
            this.timestamp = 0;
            this.counter = 0;
            return;
        }

        String[] parts = data.split("\\|", 3);
        this.data = parts.length > 0 ? parts[0] : "";
        this.timestamp = parts.length > 1 ? Long.parseLong(parts[1]) : 0;
        this.counter = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    }

    // Getters for validation
    public String getData() { return data; }
    public long getTimestamp() { return timestamp; }
    public int getCounter() { return counter; }

    // For equality checks in testing
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TestSaveableObject)) return false;
        TestSaveableObject other = (TestSaveableObject) obj;
        return timestamp == other.timestamp && 
               counter == other.counter && 
               data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode() + (int) timestamp + counter;
    }
}