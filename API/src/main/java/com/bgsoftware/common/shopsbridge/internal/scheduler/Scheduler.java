package com.bgsoftware.common.shopsbridge.internal.scheduler;

import org.bukkit.plugin.Plugin;

public class Scheduler {

    private static final IScheduler scheduler = initializeScheduler();

    private static IScheduler initializeScheduler() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return (IScheduler)
                    Class.forName("com.bgsoftware.common.shopsbridge.internal.scheduler.FoliaScheduler")
                    .getField("INSTANCE")
                    .get(null);
        } catch (Exception error) {
            return BukkitScheduler.INSTANCE;
        }
    }

    private Scheduler() {

    }

    public static void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

}
