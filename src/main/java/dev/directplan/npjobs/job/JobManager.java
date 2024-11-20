package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.Keyed;
import dev.directplan.npjobs.npc.NPCManager;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Optional;

/**
 * @author DirectPlan
 */
public interface JobManager {

    /**
     * Creates a new internal instance of the {@link JobManager} class.
     *
     * @param plugin bukkit plugin
     * @param npcManager npc manager which will handle spawning, de-spawning and ticking of NPCs.
     * @return a new internal {@link JobManager} instance.
     */
    static JobManager create(@NotNull Plugin plugin, @NotNull NPCManager npcManager) {
        return new JobManagerImpl(plugin, npcManager);
    }

    /**
     * Registers a new job factory which will handle constructing new job instances
     *
     * @param id the id which the job factory will be registered by.
     * @param jobFactory the job factory instance.
     * @param <T> job context type.
     * @param <P> job type.
     */
    <T extends JobContext, P extends Job<T>> void register(@NotNull Keyed id,  @NotNull JobFactory<T, P> jobFactory);

    /**
     * Creates a new job instance by the specified id and context.
     *
     * @param id id of the job.
     * @param context job context.
     * @return new job instance.
     * @param <T> job context type.
     * @param <P> job type.
     */
    @NotNull
    <T extends JobContext, P extends Job<T>> P create(@NotNull Keyed id, @NotNull T context);

    /**
     * Attempts to start the specified job.
     * <p>
     * This method will return false if:
     * <li>There is an active job registered by the same provided job id.</li>
     * <li>{@link Job#canStart()} returns false</li>
     *
     * @param job job to start.
     * @return whether this job has successfully started.
     * @param <T> job context type.
     */
    <T extends JobContext> boolean startJob(@NotNull Job<T> job);

    /**
     * Adds a new worker to the specified job.
     *
     * @param job the job which the worker will be added to.
     * @param workerName the worker's name.
     * @param location where to spawn this worker.
     * @return a new worker instance.
     */
    @NotNull
    Worker addWorker(@NotNull Job<?> job, @Nullable String workerName, @NotNull Location location);

    /**
     * Gets an active job instance associated with the provided id
     *
     * @param id the job id
     * @return a nullable instance wrapped in an {@link Optional}.
     */
    Optional<Job<?>> getActiveJob(@NotNull Keyed id);

    /**
     * Completes a job by removing it from the active jobs list
     * and calling {@link Job#onComplete()}.
     *
     * @param job job to complete.
     */
    void completeJob(@NotNull Job<?> job);

    /**
     * Gets an immutable list of active jobs.
     *
     * @return an immutable list of active jobs.
     */
    @UnmodifiableView Collection<Job<?>> getActiveJobs();

    /**
     * Creates and attempts to start a new job instance with the provided
     * {@code id} and {@code context}.
     * <p>
     * If the job cannot start for any reason, the returned
     * optional will be empty.
     *
     * @param id an id that will represent a new job instance.
     * @param context the job context
     * @return a job optional indicating whether the job has started.
     * @param <T> job context type
     */
    default <T extends JobContext> Optional<Job<T>> startJob(@NotNull Keyed id, @NotNull T context) {
        Job<T> job = create(id, context);
        return Optional.ofNullable(startJob(job) ? job : null);
    }
}
