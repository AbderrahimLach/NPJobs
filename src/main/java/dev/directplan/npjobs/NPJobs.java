package dev.directplan.npjobs;

import dev.directplan.npjobs.job.JobManager;
import org.bukkit.plugin.Plugin;

/**
 * @author DirectPlan
 */
public sealed interface NPJobs permits NPJPlugin {

    JobManager getJobManager();

    JobManager getJobManager(Plugin plugin);
}
