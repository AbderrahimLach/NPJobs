package dev.directplan.npjobs.job;

import com.google.common.collect.Lists;
import dev.directplan.npjobs.util.EnumUtils;
import org.bukkit.Location;

import java.util.List;

/**
 * @author DirectPlan
 */
public abstract class AbstractJob<T extends JobContext> implements Job<T> {

    private final List<Worker> workers = Lists.newLinkedList();
    private final T context;
    private State state = State.STARTING;
    private int spawnedWorkers;

    public AbstractJob(T context) {
        this.context = context;
    }

    public abstract void tick(Worker worker);

    @Override
    public void tick() {
        for (Worker worker : workers) {
            if (worker.getState().isNotSpawned()) {
                worker.spawn(getSpawnLocation());
                worker.setState(Worker.State.IDLING);
                spawnedWorkers++;
            }
            if (!state.isRunning()) continue;
            tick(worker);
        }
        if (state.isStarting() && spawnedWorkers >= getWorkerSize()) {
            setState(State.RUNNING);
            onStart();
        }
    }

    @Override
    public Iterable<Worker> getWorkers() {
        return workers;
    }

    @Override
    public int getWorkerSize() {
        return workers.size();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public T getContext() {
        return context;
    }

    @Override
    public void addWorker(Worker worker) {
        workers.add(worker);
    }

    @Override
    public void setState(State state) {
        EnumUtils.checkAscendance(this.state, state);
        this.state = state;
    }

    public Location getSpawnLocation() {
        return context.getSiteLocation();
    }
}
