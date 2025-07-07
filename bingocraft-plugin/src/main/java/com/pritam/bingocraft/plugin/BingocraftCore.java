package com.pritam.bingocraft.plugin;

import com.pritam.bingocraft.api.BingocraftAPI;
import com.pritam.bingocraft.plugin.config.MainConfig;
import com.pritam.bingocraft.plugin.diagnostic.SaveServiceDiagnosticCommand;
import com.pritam.bingocraft.plugin.listeners.ServerListeners;
import com.pritam.bingocraft.plugin.persistence.SaveService;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class BingocraftCore extends JavaPlugin implements BingocraftAPI {
    @Getter private static BingocraftCore plugin;
    @Getter private static MainConfig mainConfig;
    @Getter private static SaveService saveService;

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getServicesManager().register(BingocraftAPI.class, this, this, ServicePriority.Highest);

        // Loading configs...

        mainConfig = new MainConfig(this);
        saveService = new SaveService();

        getServer().getPluginManager().registerEvents(new ServerListeners(), this);

        // Register diagnostic command
        SaveServiceDiagnosticCommand diagnosticCommand = new SaveServiceDiagnosticCommand();
        if (this.getCommand("saveservice-diagnostic") != null) {
            this.getCommand("saveservice-diagnostic").setExecutor(diagnosticCommand);
            this.getCommand("saveservice-diagnostic").setTabCompleter(diagnosticCommand);
            getLogger().info("SaveService diagnostic command registered successfully");
        } else {
            getLogger().warning("Failed to register saveservice-diagnostic command");
        }

        getLogger().info("Enabled plugin!");
    }

    @Override
    public void onDisable() {
        saveService.shutdown();

        getLogger().info("Disabled plugin!");
    }

    @Override
    public void setMOTD(Component motd) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        String combined = serializer.serialize(motd);
        mainConfig.set("motd", Arrays.asList(combined.split("\\n", -1)));
    }
}
