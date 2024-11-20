package dev.directplan.npjobs;

import java.util.Objects;

/**
 * @author DirectPlan
 */
public final class NPJobsProvider {

    private static NPJobs instance;

    static void provide(NPJobs sentientJob) {
        if (instance == null) instance = sentientJob;
    }

    public static NPJobs get() {
        return Objects.requireNonNull(instance, "Plugin has not yet initialized.");
    }
}
