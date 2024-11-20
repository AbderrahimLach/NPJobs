package dev.directplan.npjobs.job.internal.build;

import com.google.common.collect.Streams;
import dev.directplan.npjobs.job.AbstractJob;
import dev.directplan.npjobs.job.JobContext;
import dev.directplan.npjobs.job.Jobs;
import dev.directplan.npjobs.job.Worker;
import dev.directplan.npjobs.keyed.NamespaceKeyed;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author DirectPlan
 */
public final class BuildJob extends AbstractJob<BuildJob.Context> {

    private static final float INTERACT_DISTANCE = 2.5F;
    private static final int TICKS_BETWEEN_ACTIONS = 10;

    private final Map<Worker, WorkerData> workerDataMap = new HashMap<>();
    private final NamespaceKeyed id;
    private final ClipboardAdapter clipboardAdapter;

    private Clipboard clipboard;
    private PlaceableIterator iterator;
    private Vector pasteVector;
    private long volume;
    private int buildHeight;

    private int tick;
    private long startTime;
    private int builtCount;
    private long allocationPerWorker;

    // TODO: Debug fields
    private int speed;

    BuildJob(ClipboardAdapter clipboardAdapter, BuildJob.Context context) {
        super(context);

        this.clipboardAdapter = clipboardAdapter;

        id = NamespaceKeyed.simple(Jobs.BUILD.key(), context.getName());
    }

    @Override
    public boolean canStart() {
        try {
            clipboard = getContext().getClipboardType().apply(clipboardAdapter);
            return true;
        } catch (ClipboardException e) {
            getEmployer().sendMessage(ChatColor.RED + "Clipboard Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onStart() {
        // If the clipboard is configured to ignore air,
        // the volume is calculated by counting non-air blocks,
        // otherwise it is calculated using the very simple width * height * length.
        if (clipboard.getContext().isIgnoreAir()) {
            volume = (int) Streams.stream(clipboard)
                    .filter(placeableSupplier -> placeableSupplier.get() != null)
                    .count();
        } else {
            volume = clipboard.getVolume();
        }

        buildHeight = clipboard.getHeight();
        pasteVector = getContext()
                .getPasteLocation()
                .toVector()
                .subtract(clipboard.getOrigin());
        iterator = clipboard.iterator();
        computeAllocationPerWorker();

        startTime = System.currentTimeMillis();
        getEmployer().sendMessage(ChatColor.GREEN + "Build job has started!");
    }

    @Override
    public void tick() {
        super.tick();

        tick++;
        if (tick % 20 == 0) {
            getEmployer().sendActionBar(
                              "&aJob: &e" + getName() +
                            "  &aBuilding: &e" + builtCount + "/" + volume +
                            "  &aSpeed: &e" + speed + " blocks/s @ " + getWorkerSize() +
                            "  &aCompletion: &e" + getCompletion() + "%");
            speed = 0;
        }

        if (getState().isRunning() && builtCount >= volume) setState(State.COMPLETED);
    }

    @Override
    public void tick(Worker worker) {
        Worker.State workerState = worker.getState();
        if (workerState.isFinished()) return;
        // Purpose of 5 ticks delay is to give enough time for the worker
        // to be on the ground otherwise the pathfinder will not work
        if (tick < 5) return;

        WorkerData workerData = workerDataMap.get(worker);
        requireNonNull(workerData, "No worker data. This shouldn't happen.");

        if (!workerState.isWorking()) {
            // If the iterator is empty and the worker is free, then
            // the worker has finished their part of the job.
            if (!iterator.hasNext() && workerData.isFree()) {
                worker.setHeldItem(null);
                worker.setState(Worker.State.FINISHED);
                workerDataMap.remove(worker);
                walkToSpawnLocation(worker);
                return;
            }

            if (workerData.isFree()) {
                // Allocate work.
                while (iterator.hasNext() && workerData.getAllocatedWorkSize() <= allocationPerWorker) {
                    Supplier<Placeable> placeableSupplier = iterator.next();
                    if (placeableSupplier.get() == null) continue;
                    workerData.allocatePlaceable(placeableSupplier);
                }
            }

            Placeable placeable = workerData.popPlaceable();
            Location placeLocation = placeable.getLocation().add(pasteVector);
            if (placeLocation.getWorld() == null) {
                placeLocation.setWorld(worker.getLocation().getWorld());
            }

            workerData.setTargetPlaceable(placeable);
            workerData.setPlaceLocation(placeLocation);

            worker.walkTo(placeLocation, false);
            worker.setHeldItem(new ItemStack(placeable.getPlaceItem()));
            worker.setState(Worker.State.WORKING);
            return;
        }

        // We want the builders to wait an arbitrary period of time before
        // placing blocks/entities again so they can imitate "realistic" movements.
        if (tick - workerData.getLastAction() + (Math.random() * TICKS_BETWEEN_ACTIONS) < TICKS_BETWEEN_ACTIONS) return;
        workerData.setLastAction(tick);

        Location placeLocation = workerData.getPlaceLocation();
        Location workerLocation = worker.getLocation();

        // Stare at the target placeable. (O_O)
        workerLocation.setDirection(placeLocation.toVector().subtract(workerLocation.toVector()));
        worker.teleport(workerLocation);

        // If the worker is no longer navigating, this usually implies they are stuck so we just teleport.
        if (!worker.isNavigating()) {
            worker.teleport(placeLocation.clone().add(0, 1, 0));
        }

        double horizontalEucDistance = Math.sqrt(
                        Math.pow(placeLocation.getX() - workerLocation.getX(), 2) +
                        Math.pow(placeLocation.getZ() - workerLocation.getZ(), 2));

        // Check if the worker is horizontally within the interact distance range.
        if (horizontalEucDistance <= INTERACT_DISTANCE) {
            double verticalDistance = placeLocation.getY() - workerLocation.getY();
            // Check if the worker is vertically within the interact distance range.
            if (verticalDistance > INTERACT_DISTANCE) {
                // The jump function is interruptible, which causes the worker to "fly"
                // if triggered repeatedly. This behavior is acceptable.
                worker.jump();
                return;
            }
            worker.playSwingAnimation();

            Placeable targetPlaceable = workerData.getTargetPlaceable();
            // If false, this most likely means that there is a non-matching block at the
            // "placeLocation". The block at that location will be broken
            // at this tick and the correct block will be placed at the next tick.
            if (!targetPlaceable.place(worker, placeLocation)) return;

            worker.setState(Worker.State.IDLING);
            builtCount++;
            speed++;
        }
    }

    @Override
    public void onComplete() {
        // Invalidate
        getWorkers().forEach(Worker::remove);
        workerDataMap.clear();

        long took = System.currentTimeMillis() - startTime;
        getEmployer().sendMessage(ChatColor.GREEN +
                "Job " + getName() + " is now completed! Took " + Duration.ofMillis(took).toSeconds() + " seconds.");
    }

    @Override
    public void addWorker(Worker worker) {
        super.addWorker(worker);
        workerDataMap.put(worker, new WorkerData());

        if (getState().isRunning()) computeAllocationPerWorker();
    }

    @Override
    public NamespaceKeyed getId() {
        return id;
    }

    @Override
    public int getCompletion() {
        return (int) (((double) builtCount / volume) * 100.0);
    }

    @Override
    public Location getSpawnLocation() {
        // Gets a random location around the center (site location)
        // within a radius equivalent to sqrt(workers) to avoid crowds and
        // spawn them next to each other at the same time.
        return getAround(getContext().getSiteLocation(), Math.sqrt(getWorkerSize()));
    }

    /**
     * All workers will need to be equally allocated a certain
     * amount of work which is calculated based on the
     * volume of the clipboard and the workers on site.
     */
    private void computeAllocationPerWorker() {
        allocationPerWorker = volume / buildHeight / getWorkerSize();
    }

    private void walkToSpawnLocation(Worker worker) {
        worker.walkTo(getSpawnLocation(), false);
    }

    /**
     * Gets a random horizontal location around the specified
     * {@code center} within the specified {@code radius}.
     *
     * @param center the center
     * @param radius the radius
     * @return random location around the center within the radius.
     */
    private Location getAround(Location center, double radius) {
        return center.clone().add(Math.random() * radius, 0, Math.random() * radius);
    }

    private static class WorkerData {

        private final Deque<Supplier<Placeable>> allocatedWork = new LinkedList<>();
        private Placeable targetPlaceable;
        private Location placeLocation;
        private int lastAction;

        WorkerData() {}

        public Placeable popPlaceable() {
            return allocatedWork.pop().get(); // Safe because we check if the supplier is non-null prior to allocation.
        }

        public void allocatePlaceable(Supplier<Placeable> placeable) {
            allocatedWork.offer(placeable);
        }

        public int getAllocatedWorkSize() {
            return allocatedWork.size();
        }

        public boolean isFree() {
            return allocatedWork.isEmpty();
        }

        public Placeable getTargetPlaceable() {
            return targetPlaceable;
        }

        public Location getPlaceLocation() {
            return placeLocation;
        }

        public int getLastAction() {
            return lastAction;
        }

        public void setTargetPlaceable(Placeable targetPlaceable) {
            this.targetPlaceable = targetPlaceable;
        }

        public void setPlaceLocation(Location placeLocation) {
            this.placeLocation = placeLocation;
        }

        public void setLastAction(int lastAction) {
            this.lastAction = lastAction;
        }
    }

    public static class ContextBuilder implements JobContext.Builder<Context, ContextBuilder> {

        private Employer employer = Employer.CONSOLE;

        private String name;
        private int maxWorkers = -1;
        private Location siteLocation;
        private Location pasteLocation;
        private ClipboardType clipboardType;

        ContextBuilder() {}

        @Override
        public ContextBuilder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public ContextBuilder maxWorkers(int maxWorkers) {
            this.maxWorkers = maxWorkers;
            return this;
        }

        @Override
        public ContextBuilder siteLocation(@NotNull Location siteLocation) {
            this.siteLocation = siteLocation;
            return this;
        }

        @Override
        public ContextBuilder employer(@NotNull Employer employer) {
            this.employer = employer;
            return this;
        }

        public ContextBuilder pasteLocation(@NotNull Location pasteLocation) {
            this.pasteLocation = pasteLocation;
            if (siteLocation == null) siteLocation = pasteLocation;
            return this;
        }

        public ContextBuilder clipboardType(@NotNull ClipboardType clipboardType) {
            this.clipboardType = clipboardType;
            return this;
        }

        @Override
        public Context build() {
            validate();
            return new Context(name, employer, maxWorkers, siteLocation, pasteLocation, clipboardType);
        }

        private void validate() {
            requireNonNull(name, "name cannot be null");
            requireNonNull(siteLocation, "site location cannot be null");
            requireNonNull(pasteLocation, "paste location cannot be null");
            requireNonNull(clipboardType, "clipboard type cannot be null");
        }
    }

    public static class Context implements JobContext {

        private final String name;
        private final Employer employer;
        private final int maxWorkers;

        private final Location siteLocation;
        private final Location pasteLocation;
        private final ClipboardType clipboardType;

        Context(String name,
                Employer employer,
                int maxWorkers,
                Location siteLocation,
                Location pasteLocation,
                ClipboardType clipboardType) {
            this.name = name;
            this.employer = employer;
            this.maxWorkers = maxWorkers;
            this.siteLocation = siteLocation;
            this.pasteLocation = pasteLocation;
            this.clipboardType = clipboardType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Employer getEmployer() {
            return employer;
        }

        @Override
        public int getMaxWorkers() {
            return maxWorkers;
        }

        @Override
        public Location getSiteLocation() {
            return siteLocation;
        }

        public Location getPasteLocation() {
            return pasteLocation;
        }

        public ClipboardType getClipboardType() {
            return clipboardType;
        }

        public static ContextBuilder builder() {
            return new ContextBuilder();
        }
    }
}
