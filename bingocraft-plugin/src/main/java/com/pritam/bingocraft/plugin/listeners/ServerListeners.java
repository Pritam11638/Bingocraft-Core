package com.pritam.bingocraft.plugin.listeners;

import com.pritam.bingocraft.plugin.BingocraftCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.HashMap;

public class ServerListeners implements Listener {
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.motd(BingocraftCore.getMainConfig().getMOTD());
    }
}
