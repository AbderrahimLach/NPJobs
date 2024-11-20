package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author DirectPlan
 */
class JobFactoryRegistry {

    private final Map<String, JobFactory> jobMap = new HashMap<>();

    public <T extends JobContext, P extends Job<T>> void register(@NotNull Keyed keyed,
                                                                  @NotNull JobFactory<T, P> jobFunction) {
        jobMap.put(keyed.key(), jobFunction);
    }

    @SuppressWarnings("unchecked")
    public <T extends JobContext, P extends Job<T>> P create(@NotNull Keyed keyed,
                                                             @NotNull T context) {
        JobFactory<T, P> jobFunction = Objects.requireNonNull(jobMap.get(keyed.key()), "job not registered");
        return jobFunction.createJob(context);
    }
}
