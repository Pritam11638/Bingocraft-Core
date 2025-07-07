package com.pritam.bingocraft.plugin.persistence;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveServiceMeta {
    private boolean enabled;
    private int saveInterval;
    private int cacheDuration;
    private int cacheSize;

    public SaveServiceMeta(boolean enabled, int saveInterval, int cacheDuration, int cacheSize) {
        this.enabled = enabled;
        this.saveInterval = saveInterval;
        this.cacheDuration = cacheDuration;
        this.cacheSize = cacheSize;
    }
}