package com.pritam.bingocraft.plugin.config;

import com.pritam.bingocraft.api.configuration.Config;
import com.pritam.bingocraft.plugin.BingocraftCore;
import com.pritam.bingocraft.plugin.persistence.SaveServiceMeta;
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

        setComments("motd", List.of(
                "This is the Message of the Day (MOTD) displayed to players.",
                "Legacy formatting codes are supported. (&a, &6, &b, &c, etc.)",
                "Each line will be displayed separately."
        ));
        ;
        addDefault("save-service.enabled", false);
        addDefault("save-service.save-interval", 60);
        addDefault("save-service.cache-duration", 300);
        addDefault("save-service.cache-size", 1000);
        setComments("save-service", List.of(
                "save-service.enabled: Whether the save service should be enabled.",
                "save-service.save-interval: Interval in seconds to save game data.",
                "save-service.cache-duration: Duration in seconds to keep cached data.",
                "save-service.cache-size: Maximum number of cached entries."
        ));

        addDefault("sidebar.update-interval", 20);
        setComments("sidebar", List.of(
                "sidebar.update-interval: Interval in ticks to update the sidebar for players."
        ));

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

    public void setMotd(List<String> motd) {
        set("motd", motd);
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

    public int getSidebarUpdateInterval() {
        return getInt("sidebar.update-interval");
    }

    public void setSidebarUpdateInterval(int interval) {
        set("sidebar.update-interval", interval);
    }
}
