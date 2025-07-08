package com.pritam.bingocraft.plugin;

import com.pritam.bingocraft.api.BingocraftAPI;
import com.pritam.bingocraft.plugin.config.MainConfig;
import com.pritam.bingocraft.plugin.listeners.ServerListeners;
import com.pritam.bingocraft.plugin.persistence.SaveService;
import com.pritam.bingocraft.plugin.sidebar.SidebarService;
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
    @Getter private SaveService saveService;
    @Getter private SidebarService sidebarService;

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getServicesManager().register(BingocraftAPI.class, this, this, ServicePriority.Highest);

        // Loading configs...

        mainConfig = new MainConfig(this);
        saveService = new SaveService();
        sidebarService = new SidebarService();

        sidebarService.start(mainConfig.getSidebarUpdateInterval());

        getServer().getPluginManager().registerEvents(new ServerListeners(), this);

        getLogger().info("Enabled plugin!");
    }

    @Override
    public void onDisable() {
        saveService.shutdown();
        sidebarService.stop();

        getLogger().info("Disabled plugin!");
    }

    @Override
    public void setMOTD(Component motd) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        String combined = serializer.serialize(motd);
        mainConfig.setMotd(Arrays.asList(combined.split("\\n", -1)));
    }
}
