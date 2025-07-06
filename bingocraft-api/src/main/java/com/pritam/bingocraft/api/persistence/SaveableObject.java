package com.pritam.bingocraft.api.persistence;

public interface SaveableObject {
    String toString();
    void fromString(String data);
}
