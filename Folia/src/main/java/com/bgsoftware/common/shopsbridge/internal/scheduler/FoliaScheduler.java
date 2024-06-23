package com.bgsoftware.common.shopsbridge.internal.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler implements IScheduler {

    public static final FoliaScheduler INSTANCE = new FoliaScheduler();

    private FoliaScheduler() {

    }

    @Override
    public void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, x -> runnable.run(), delay);
    }

}
