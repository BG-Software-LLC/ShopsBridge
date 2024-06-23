package com.bgsoftware.common.shopsbridge.internal.scheduler;

import org.bukkit.plugin.Plugin;

public interface IScheduler {

    void runTaskLater(Plugin plugin, Runnable runnable, long delay);

}
