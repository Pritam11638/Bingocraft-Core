package com.pritam.bingocraft.api;

import com.pritam.bingocraft.api.persistence.SaveService;
import com.pritam.bingocraft.api.sidebar.SidebarService;
import net.kyori.adventure.text.Component;

public interface BingocraftAPI {
    void setMOTD(Component motd);
    SaveService getSaveService();
    SidebarService getSidebarService();
}
