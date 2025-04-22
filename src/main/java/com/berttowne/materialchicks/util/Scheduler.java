package com.berttowne.materialchicks.util;

import com.berttowne.materialchicks.MaterialChicks;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class Scheduler {

    private static final Map<ScheduledTask, Supplier<Boolean>> TASKS = new LinkedHashMap<>();

    static {
        Scheduler.repeatAsync(() -> TASKS.entrySet().removeIf(entry -> {
            if (entry.getValue().get()) {
                entry.getKey().cancel();
                return true;
            }

            return false;
        }), 0L, 50L, TimeUnit.MILLISECONDS);
    }

    // ENTITY-ATTACHED TASKS

    /**
     * Run a task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Consumer} to execute.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask run(final @NotNull Entity entity, final Consumer<ScheduledTask> run) {
        return entity.getScheduler().run(MaterialChicks.getPlugin(MaterialChicks.class), run, null);
    }

    /**
     * Run a task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Runnable} to execute.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask run(final @NotNull Entity entity, final Runnable run) {
        return Scheduler.run(entity, task -> run.run());
    }

    /**
     * Run a delayed task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask later(final @NotNull Entity entity, final Consumer<ScheduledTask> run, final long delay) {
        return entity.getScheduler().runDelayed(MaterialChicks.getPlugin(MaterialChicks.class), run, null, delay);
    }

    /**
     * Run a delayed task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask later(final @NotNull Entity entity, final Runnable run, final long delay) {
        return Scheduler.later(entity, task -> run.run(), delay);
    }

    /**
     * Run a repeating task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask repeat(final @NotNull Entity entity, final Consumer<ScheduledTask> run, final long delay, final long period) {
        return entity.getScheduler().runAtFixedRate(MaterialChicks.getPlugin(MaterialChicks.class), run, null, delay, period);
    }

    /**
     * Run a repeating task on an {@link Entity} using the thread-safe {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}
     * to ensure that the task is executed on the same owning thread for the region the entity is in.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been removed.
     */
    public static @Nullable ScheduledTask repeat(final @NotNull Entity entity, final Runnable run, final long delay, final long period) {
        return Scheduler.repeat(entity, task -> run.run(), delay, period);
    }

    /**
     * Run a task on an {@link Entity} repeatedly until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -> k < 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param entity The {@link Entity} to run the task on.
     * @param run    The {@link Runnable} task to execute every period.
     * @param delay  The delay in ticks before the first run of the task.
     * @param period The time period in ticks to wait until running again after each run.
     * @param until  The {@link Supplier} to test when to cancel. When this returns <tt>true</tt> the task will be cancelled.
     */
    public static void repeatUntil(final @NotNull Entity entity, final Runnable run, final long delay, final long period, final Supplier<Boolean> until) {
        final ScheduledTask task = Scheduler.repeat(entity, run, delay, period);
        TASKS.put(task, until);
    }

    // GLOBAL THREAD TASKS

    /**
     * Run a task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     */
    public static @NotNull ScheduledTask run(final Runnable run) {
        return Scheduler.run(task -> run.run());
    }

    /**
     * Run a task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     */
    public static @NotNull ScheduledTask run(final Consumer<ScheduledTask> run) {
        return Bukkit.getGlobalRegionScheduler().run(MaterialChicks.getPlugin(MaterialChicks.class), run);
    }

    /**
     * Run a delayed task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     */
    public static @NotNull ScheduledTask later(final Runnable run, final long delay) {
        return Scheduler.later(task -> run.run(), delay);
    }

    /**
     * Run a delayed task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     */
    public static @NotNull ScheduledTask later(final Consumer<ScheduledTask> run, final long delay) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(MaterialChicks.getPlugin(MaterialChicks.class), run, delay);
    }

    /**
     * Run a repeating task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     */
    public static @NotNull ScheduledTask repeat(final Runnable run, final long delay, final long period) {
        return Scheduler.repeat(task -> run.run(), delay, period);
    }

    /**
     * Run a repeating task synchronously on the global thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     */
    public static @NotNull ScheduledTask repeat(final Consumer<ScheduledTask> run, final long delay, final long period) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(MaterialChicks.getPlugin(MaterialChicks.class), run, delay, period);
    }

    /**
     * Run a task synchronously repeatedly on the global thread until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -> k < 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run    The {@link Runnable} task to execute every period.
     * @param delay  The delay in ticks before the first run of the task.
     * @param period The time period in ticks to wait until running again after each run.
     * @param until  The {@link Supplier} to test when to cancel. When this returns <tt>true</tt> the task will be cancelled.
     */
    public static void repeatUntil(final Runnable run, final long delay, final long period, final Supplier<Boolean> until) {
        final ScheduledTask task = Scheduler.repeat(run, delay, period);
        TASKS.put(task, until);
    }

    // REGION THREAD TASKS

    /**
     * Run a task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Runnable} to execute.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask run(final Location location, final Runnable run) {
        return Scheduler.run(location, task -> run.run());
    }

    /**
     * Run a task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Consumer} to execute.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask run(final Location location, final Consumer<ScheduledTask> run) {
        return Bukkit.getRegionScheduler().run(MaterialChicks.getPlugin(MaterialChicks.class), location, run);
    }

    /**
     * Run a delayed task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask later(final Location location, final Runnable run, final long delay) {
        return Scheduler.later(location, task -> run.run(), delay);
    }

    /**
     * Run a delayed task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask later(final Location location, final Consumer<ScheduledTask> run, final long delay) {
        return Bukkit.getRegionScheduler().runDelayed(MaterialChicks.getPlugin(MaterialChicks.class), location, run, delay);
    }

    /**
     * Run a repeating task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Runnable} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask repeat(final Location location, final Runnable run, final long delay, final long period) {
        return Scheduler.repeat(location, task -> run.run(), delay, period);
    }

    /**
     * Run a repeating task synchronously on the region thread using
     * the {@link io.papermc.paper.threadedregions.scheduler.RegionScheduler}.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run The {@link Consumer} to execute.
     * @param delay The delay in ticks before running the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask repeat(final Location location, final Consumer<ScheduledTask> run, final long delay, final long period) {
        return Bukkit.getRegionScheduler().runAtFixedRate(MaterialChicks.getPlugin(MaterialChicks.class), location, run, delay, period);
    }

    /**
     * Run a task synchronously repeatedly on the region thread until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -> k < 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param location The {@link Location} to run the task on/around.
     * @param run    The {@link Runnable} task to execute every period.
     * @param delay  The delay in ticks before the first run of the task.
     * @param period The time period in ticks to wait until running again after each run.
     * @param until  The {@link Supplier} to test when to cancel. When this returns <tt>true</tt> the task will be cancelled.
     */
    public static void repeatUntil(final Location location, final Runnable run, final long delay, final long period, final Supplier<Boolean> until) {
        final ScheduledTask task = Scheduler.repeat(location, run, delay, period);
        TASKS.put(task, until);
    }

    // ASYNC TASKS

    /**
     * Run a task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask async(final Consumer<ScheduledTask> run) {
        return Bukkit.getAsyncScheduler().runNow(MaterialChicks.getPlugin(MaterialChicks.class), run);
    }

    /**
     * Run a task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Runnable} to execute
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask async(final Runnable run) {
        return Bukkit.getAsyncScheduler().runNow(MaterialChicks.getPlugin(MaterialChicks.class), task -> run.run());
    }

    /**
     * Run a delayed task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     * @param delay The time delay (NOT IN TICKS) to pass before the task should be executed.
     * @param unit The {@link TimeUnit} to use for the delay.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask laterAsync(final Consumer<ScheduledTask> run, final long delay, final TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runDelayed(MaterialChicks.getPlugin(MaterialChicks.class), run, delay, unit);
    }

    /**
     * Run a delayed task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @param delay The time delay (NOT IN TICKS) to pass before the task should be executed.
     * @param unit The {@link TimeUnit} to use for the delay.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask laterAsync(final Runnable run, final long delay, final TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runDelayed(MaterialChicks.getPlugin(MaterialChicks.class), task -> run.run(), delay, unit);
    }

    /**
     * Run a repeating task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Consumer} to execute.
     * @param delay The time delay (NOT IN TICKS) to pass before the task should be executed.
     * @param period The time period (NOT IN TICKS) to pass before the task should be executed again.
     * @param unit The {@link TimeUnit} to use for the delay and period.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask repeatAsync(final Consumer<ScheduledTask> run, final long delay, final long period, final TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(MaterialChicks.getPlugin(MaterialChicks.class), run, delay, period, unit);
    }

    /**
     * Run a repeating task asynchronously using the {@link io.papermc.paper.threadedregions.scheduler.AsyncScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @param delay The time delay (NOT IN TICKS) to pass before the task should be executed.
     * @param period The time period (NOT IN TICKS) to pass before the task should be executed again.
     * @param unit The {@link TimeUnit} to use for the delay and period.
     * @return The {@link ScheduledTask} that represents the scheduled task.
     */
    public static @NotNull ScheduledTask repeatAsync(final Runnable run, final long delay, final long period, final TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(MaterialChicks.getPlugin(MaterialChicks.class), task -> run.run(), delay, period, unit);
    }

    /**
     * Run a task asynchronously repeatedly until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -> k < 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run    The {@link Runnable} task to execute every period.
     * @param delay  The delay (NOT IN TICKS) before the first run of the task.
     * @param period The time period (NOT IN TICKS) to wait until running again after each run.
     * @param unit   The {@link TimeUnit} to use for the delay and period.
     * @param until  The {@link Supplier} to test when to cancel. When this returns <tt>true</tt> the task will be cancelled.
     */
    public static void repeatAsyncUntil(final Runnable run, final long delay, final long period, final TimeUnit unit, final Supplier<Boolean> until) {
        final ScheduledTask task = Scheduler.repeatAsync(run, delay, period, unit);
        TASKS.put(task, until);
    }

}