package com.pritam.bingocraft.plugin.config;

import com.pritam.bingocraft.api.configuration.Config;
import com.pritam.bingocraft.plugin.BingocraftCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class MainConfig extends Config {
    public MainConfig(JavaPlugin plugin) {
        super("config", plugin.getDataFolder(), plugin);

        addDefault("motd", List.of(
                "&aWelcome to &6BingoCraft!",
                "&bEnjoy your game!",
                "&cHave fun!"
        ));

        addDefault("save-service.enabled", false);;

        try {
            save();
        } catch (IOException e) {
            BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not save config!", e);
        }
    }

    public Component getMOTD() {
        List<String> motd = getStringList("motd");

        if (motd.isEmpty()) {
            return Component.text("No MOTD set.");
        }

        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(motd.getFirst());

        for (int i = 1; i < motd.size(); i++) {
            component = component.append(Component.newline()).append(LegacyComponentSerializer.legacyAmpersand().deserialize(motd.get(i)));
        }

        return component;
    }

    public boolean isSaveServiceEnabled() {
        return getBoolean("save-service.enabled");
    }

    public void setSaveServiceState(boolean enabled) {
        set("save-service.enabled", enabled);
    }
}
