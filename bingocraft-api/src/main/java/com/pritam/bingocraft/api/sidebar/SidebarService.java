package com.pritam.bingocraft.api.sidebar;

public interface SidebarService {
    SidebarView getView(String sidebarId);
    void register(SidebarView view);
    void unregister(String sidebarId);
}
