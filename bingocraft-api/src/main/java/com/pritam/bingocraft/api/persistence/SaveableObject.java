package com.pritam.bingocraft.api.persistence;

/**
 * Marker interface for objects that can be persisted using the {@link SaveService}.
 *
 * <p>Objects implementing this interface must provide mechanisms for serializing
 * themselves to a string representation and deserializing from that representation.
 *
 * <p>The string representation should contain all necessary data to fully restore
 * the object's state when {@link #fromString(String)} is called.
 *
 * <p><strong>Implementation Requirements:</strong>
 * <ul>
 * <li>The {@link #toString()} method must return a complete serialized representation</li>
 * <li>The {@link #fromString(String)} method must restore the object from the serialized data</li>
 * <li>Round-trip serialization/deserialization must preserve object state</li>
 * <li>Implementations should handle null or malformed input gracefully</li>
 * </ul>
 *
 * <p><strong>Example Implementation:</strong>
 * <pre>{@code
 * public class PlayerData implements SaveableObject {
 *     private String playerName;
 *     private int score;
 *
 *     @Override
 *     public String toString() {
 *         return playerName + ":" + score;
 *     }
 *
 *     @Override
 *     public void fromString(String data) {
 *         String[] parts = data.split(":");
 *         this.playerName = parts[0];
 *         this.score = Integer.parseInt(parts[1]);
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 * @author Pritam
 * @see SaveService
 */
public interface SaveableObject {

    /**
     * Serializes this object to a string representation.
     *
     * <p>The returned string must contain all data necessary to reconstruct
     * this object's state using {@link #fromString(String)}.
     *
     * <p>This method should not return null and should handle all possible
     * object states gracefully.
     *
     * @return a string representation of this object's state
     */
    String toString();

    /**
     * Deserializes this object from a string representation.
     *
     * <p>This method should populate all fields of this object based on
     * the provided serialized data. The data format should match what
     * is produced by {@link #toString()}.
     *
     * <p>Implementations should handle malformed or null input gracefully,
     * potentially by throwing appropriate exceptions or setting default values.
     *
     * @param data the serialized string data to restore from
     * @throws IllegalArgumentException if the data format is invalid
     * @throws NullPointerException if data is null (optional - implementations may handle this)
     */
    void fromString(String data);
}
