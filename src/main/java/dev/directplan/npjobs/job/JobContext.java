package dev.directplan.npjobs.job;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public interface JobContext {

    /**
     * Gets the name of a job.
     *
     * @return name of a job
     */
    String getName();

    /**
     * Gets the maximum amount of workers that can be allocated
     * to a job.
     * <p>
     * Returns -1 if the maximum amount is undefined.
     *
     * @return maximum workers
     */
    int getMaxWorkers();

    /**
     * Gets the site location of a job where workers will spawn.
     *
     * @return site location where workers will spawn
     */
    Location getSiteLocation();

    /**
     * Gets the employer of a job. An employer is a player or console
     * that started a job.
     *
     * @return employer of a job.
     */
    Job.Employer getEmployer();

    interface Builder<T extends JobContext, B extends Builder<T, B>> {

        B name(@NotNull String name);

        B maxWorkers(int maxWorkers);

        B siteLocation(@NotNull Location siteLocation);

        B employer(@NotNull Job.Employer employer);

        T build();
    }
}
