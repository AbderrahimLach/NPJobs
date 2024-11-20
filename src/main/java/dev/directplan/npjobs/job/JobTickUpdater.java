package dev.directplan.npjobs.job;

/**
 * @author DirectPlan
 */
public final class JobTickUpdater implements Runnable {

    private final JobManager jobManager;

    JobTickUpdater(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public void run() {
        for (Job<?> job : jobManager.getActiveJobs()) {
            if (job.getState().isCompleted()) {
                jobManager.completeJob(job);
                continue;
            }
            job.tick();
        }
    }
}
