package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.Keyed;
import dev.directplan.npjobs.keyed.NamespaceKeyed;
import dev.directplan.npjobs.npc.NPC;
import dev.directplan.npjobs.npc.NPCManager;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * @author DirectPlan
 */
public final class JobManagerImpl implements JobManager {

    private final Map<String, Job<?>> activeJobsMap = new ConcurrentHashMap<>();
    private final JobFactoryRegistry registry = new JobFactoryRegistry();

    private final Plugin plugin;
    private final NPCManager npcManager;
    private JobTickUpdater tickUpdater;

    JobManagerImpl(Plugin plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @Override
    public <T extends JobContext, P extends Job<T>> void register(@NotNull Keyed keyed,
                                                                  @NotNull JobFactory<T, P> jobFactory) {
        registry.register(keyed, jobFactory);
        plugin.getLogger().info("Registered new job: " + keyed.key());
    }

    @Override
    public <T extends JobContext, P extends Job<T>> @NotNull P create(@NotNull Keyed keyed,
                                                                      @NotNull T context) {
        P job = registry.create(keyed, context);
        int maxWorkers = context.getMaxWorkers();
        if (maxWorkers != -1) {
            // Add workers
            IntStream.rangeClosed(1, maxWorkers).forEach(_ -> {
                Worker worker = addWorker(job, null, context.getSiteLocation());
                worker.setSkin(worker.getName());
            });
        }
        return job;
    }

    @Override
    public @NotNull Worker addWorker(@NotNull Job<?> job,
                                     @Nullable String workerName,
                                     @NotNull Location location) {
        if (workerName == null) {
            // TODO: Added for testing. Remove later.
            workerName = DebugEntityNameGenerator.getEntityName(UUID.randomUUID());
        }
        JobContext context = job.getContext();
        if (job.getWorkerSize() >= context.getMaxWorkers()) {
            throw new IndexOutOfBoundsException("Cannot exceed allocated crew size (" + context.getMaxWorkers() + ")");
        }
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        NamespaceKeyed npcId = NamespaceKeyed.simple(job.getName(), workerName.toLowerCase());

        NPC npc = npcManager.createNpc(world, npcId.key(), workerName);

        NPCWorker worker = new NPCWorker(npcId, npcManager, npc);
        job.addWorker(worker);
        return worker;
    }

    @Override
    public <T extends JobContext> boolean startJob(@NotNull Job<T> job) {
        NamespaceKeyed jobId = job.getId();
        if (getActiveJob(jobId).isPresent()) {
            job.getEmployer().sendMessage(ChatColor.RED + "There's already an active job called " + job.getName() + ".");
            return false;
        }

        if (!job.canStart()) {
            job.getEmployer().sendMessage(ChatColor.RED + "Failed to start " + job.getName() + " job.");
            return false;
        }

        job.setState(Job.State.STARTING);
        activeJobsMap.put(job.getId().key(), job);
        if (tickUpdater == null) {
            tickUpdater = new JobTickUpdater(this);
            plugin.getServer()
                    .getScheduler()
                    .runTaskTimer(plugin, tickUpdater, 5L, 1L);
        }

        return true;
    }

    @Override
    public void completeJob(@NotNull Job<?> job) {
        activeJobsMap.remove(job.getId().key());
        job.onComplete();
    }

    @Override
    public Optional<Job<?>> getActiveJob(@NotNull Keyed keyed) {
        return Optional.ofNullable(activeJobsMap.get(keyed.key()));
    }

    @Override
    public Collection<Job<?>> getActiveJobs() {
        return activeJobsMap.values();
    }
}
