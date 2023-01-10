package de.crafttogether.tcportals.util;

import de.crafttogether.TCPortals;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class PollingTask {
    private final Consumer consumer;
    private final long delay;
    private final long period;

    private BukkitTask task;

    public interface Consumer {
        boolean operation();
    }

    public PollingTask(Consumer consumer, long delay, long period) {
        this.consumer = consumer;
        this.delay = delay;
        this.period = period;
        start();
    }

    private void run() {
        if (consumer.operation())
            cancel();
    }

    public void start() {
        if (task != null && !task.isCancelled())
            return;

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(TCPortals.plugin, this::run, 0L, 1L);
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
}
