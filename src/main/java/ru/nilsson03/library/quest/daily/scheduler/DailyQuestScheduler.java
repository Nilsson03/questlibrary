package ru.nilsson03.library.quest.daily.scheduler;

import java.util.Objects;
import java.util.function.LongSupplier;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public final class DailyQuestScheduler {

    private final NPlugin plugin;
    private final Runnable rotateAction;
    private final LongSupplier delayMillisSupplier;
    private BukkitTask task;

    public DailyQuestScheduler(NPlugin plugin, Runnable rotateAction, LongSupplier delayMillisSupplier) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.rotateAction = Objects.requireNonNull(rotateAction, "rotateAction");
        this.delayMillisSupplier = Objects.requireNonNull(delayMillisSupplier, "delayMillisSupplier");
    }

    public void scheduleNext() {
        cancel();
        long delayMillis = Math.max(0L, delayMillisSupplier.getAsLong());
        long delayTicks = Math.max(1L, delayMillis / 50L);
        ConsoleLogger.info(plugin, "Daily quests next reset in %d ms (%d ticks)", delayMillis, delayTicks);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    rotateAction.run();
                } catch (Exception e) {
                    ConsoleLogger.error(plugin, "Daily quest rotation failed: %s", e.getMessage());
                } finally {
                    scheduleNext();
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean isScheduled() {
        return task != null && !task.isCancelled();
    }
}
