package com.bgsoftware.common.shopsbridge.internal.scheduler;

import org.bukkit.plugin.Plugin;

public class BukkitScheduler implements IScheduler {

    public static final BukkitScheduler INSTANCE = new BukkitScheduler();

    private BukkitScheduler() {

    }

    @Override
    public void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

}
