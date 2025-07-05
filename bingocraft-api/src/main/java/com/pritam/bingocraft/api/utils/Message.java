package com.pritam.bingocraft.api.utils;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Represents a localizable message with placeholder support.
 *
 * <p>This class encapsulates a message that can be dynamically generated
 * with placeholder substitution. Messages are built using Adventure Text
 * Components, allowing for rich text formatting including colors, hover
 * effects, and click actions.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Dynamic placeholder substitution</li>
 *   <li>Default placeholder values</li>
 *   <li>Adventure Text Component integration</li>
 *   <li>Functional interface for flexible message building</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Message welcomeMessage = new Message(placeholders ->
 *     Component.text("Welcome, " + placeholders.get("player_name") + "!")
 *         .color(NamedTextColor.GREEN)
 * );
 *
 * HashMap<String, String> placeholders = new HashMap<>();
 * placeholders.put("player_name", "Steve");
 * Component component = welcomeMessage.getComponent(placeholders);
 * }</pre>
 *
 * @author KakorinMC Development Team
 * @since 1.0.0
 */
public class Message {
    /**
     * Function that builds the Adventure Text Component from placeholders.
     */
    private final Function<HashMap<String, String>, Component> componentBuilder;

    /**
     * Default placeholder values to use when placeholders are not provided.
     */
    private HashMap<String, String> defaultPlaceholders = new HashMap<>();

    /**
     * Creates a new message with the specified component builder.
     *
     * @param componentBuilder function that converts placeholders to a Component
     */
    public Message(Function<HashMap<String, String>, Component> componentBuilder) {
        this.componentBuilder = componentBuilder;
    }

    /**
     * Creates a new message with the specified component builder and default placeholders.
     *
     * <p>Default placeholders are used when specific placeholders are not provided
     * in the getComponent call, ensuring messages always have fallback values.</p>
     *
     * @param componentBuilder function that converts placeholders to a Component
     * @param defaultPlaceholders default placeholder values to use
     */
    public Message(Function<HashMap<String, String>, Component> componentBuilder, HashMap<String, String> defaultPlaceholders) {
        this.componentBuilder = componentBuilder;
        this.defaultPlaceholders = defaultPlaceholders;
    }

    /**
     * Generates the Adventure Text Component for this message with the given placeholders.
     *
     * <p>This method merges the provided placeholders with the default placeholders,
     * with provided placeholders taking precedence. The resulting placeholder map
     * is then passed to the component builder function to generate the final Component.</p>
     *
     * @param placeholders the placeholder values to substitute in the message
     * @return the generated Adventure Text Component
     */
    public Component getComponent(HashMap<String, String> placeholders) {
        defaultPlaceholders.forEach(placeholders::putIfAbsent);
        return componentBuilder.apply(placeholders);
    }
}
