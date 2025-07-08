package com.pritam.bingocraft.plugin.sidebar;

import com.pritam.bingocraft.api.sidebar.SidebarView;
import com.pritam.bingocraft.plugin.BingocraftCore;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SidebarService implements com.pritam.bingocraft.api.sidebar.SidebarService {
    private final Set<SidebarView> views = new HashSet<>();
    private BukkitTask task;

    public SidebarService() {}

    public void register(SidebarView view) {
        views.add(view);
    }

    public void unregister(String sidebarId) {
        views.removeIf(sidebarView -> Objects.equals(sidebarView.getSCOREBOARD_ID(), sidebarId));
    }

    public SidebarView getView(String sidebarId) {
        return views.stream()
                .filter(view -> Objects.equals(view.getSCOREBOARD_ID(), sidebarId))
                .findFirst()
                .orElse(null);
    }

    public void start(long intervalTicks) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimer(BingocraftCore.getPlugin(), () -> {
            for (SidebarView view : views) {
                view.tick();
            }
        }, 0, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}