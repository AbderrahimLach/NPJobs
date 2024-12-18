package dev.directplan.npjobs;

import dev.directplan.npjobs.job.JobCommand;
import dev.directplan.npjobs.job.JobManager;
import dev.directplan.npjobs.job.JobManagerRegistry;
import dev.directplan.npjobs.job.Jobs;
import dev.directplan.npjobs.job.internal.build.BuildJobFactory;
import dev.directplan.npjobs.job.internal.build.PasteBuildCommand;
import dev.directplan.npjobs.npc.NPCManager;
import dev.directplan.npjobs.util.PluginUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public non-sealed class NPJPlugin extends JavaPlugin implements NPJobs {

    private JobManagerRegistry jobManagerRegistry;

    private NPCManager npcManager;
    private JobManager jobManager;

    @Override
    public void onEnable() {
        jobManagerRegistry = new JobManagerRegistry(this);

        npcManager = NPCManager.create(this);
        jobManager = JobManager.create(this, npcManager);
        jobManager.register(Jobs.BUILD, new BuildJobFactory(this));
        jobManagerRegistry.register(this, jobManager);

        PluginUtils.registerCommand(this, "job", new PasteBuildCommand(jobManager));
        PluginUtils.registerCommand(this, "job", new JobCommand(jobManager));

        NPJobsProvider.provide(this);
    }

    @Override
    public JobManager getJobManager() {
        return jobManager;
    }

    @Override
    public JobManager getJobManager(Plugin plugin) {
        if (plugin == this) return jobManager;
        return jobManagerRegistry.get(plugin);
    }

    @Override
    public void onDisable() {
        npcManager.removeAll();
    }
}
