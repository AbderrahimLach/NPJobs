package dev.directplan.npjobs.job;

/**
 * @author DirectPlan
 */
public interface JobFactory<I extends JobContext, O extends Job<I>> {

    /**
     * Constructs a new job instance with the provided context.
     *
     * @param context the provided context
     * @return a new job instance
     */
    O createJob(I context);
}
