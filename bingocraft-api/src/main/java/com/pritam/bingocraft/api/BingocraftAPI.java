package com.pritam.bingocraft.api;

import com.pritam.bingocraft.api.persistence.SaveService;
import net.kyori.adventure.text.Component;

public interface BingocraftAPI {
    void setMOTD(Component motd);
    SaveService getSaveService();
}
