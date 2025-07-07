package com.pritam.bingocraft.plugin.config;

import com.pritam.bingocraft.api.configuration.Config;
import com.pritam.bingocraft.plugin.BingocraftCore;
import com.pritam.bingocraft.plugin.meta.SaveServiceMeta;
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

        addDefault("save-service.enabled", false);
        addDefault("save-service.save-interval", 60);
        addDefault("save-service.cache-duration", 300);
        addDefault("save-service.cache-size", 1000);

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

    public SaveServiceMeta getSaveServiceMeta() {
        boolean enabled = getBoolean("save-service.enabled");
        int saveInterval = getInt("save-service.save-interval");
        int cacheDuration = getInt("save-service.cache-duration");
        int cacheSize = getInt("save-service.cache-size");

        return new SaveServiceMeta(enabled, saveInterval, cacheDuration, cacheSize);
    }

    public void updateSaveServiceMeta(SaveServiceMeta meta) {
        set("save-service.enabled", meta.isEnabled());
        set("save-service.save-interval", meta.getSaveInterval());
        set("save-service.cache-duration", meta.getCacheDuration());

        try {
            save();
        } catch (IOException e) {
            BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not update save service meta!", e);
        }
    }
}
