package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.JobFactory;
import dev.directplan.npjobs.job.internal.build.worldedit.WorldEditClipboardAdapter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * @author DirectPlan
 */
public final class BuildJobFactory implements JobFactory<BuildJob.Context, BuildJob> {

    private final Plugin plugin;
    private final ClipboardAdapter clipboardAdapter;

    public BuildJobFactory(Plugin plugin) {
        this.plugin = plugin;

        clipboardAdapter = getBestAdapter();
    }

    @Override
    public BuildJob createJob(BuildJob.Context context) {
        return new BuildJob(clipboardAdapter, context);
    }

    private ClipboardAdapter getBestAdapter() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
         if (pluginManager.isPluginEnabled("WorldEdit")) return new WorldEditClipboardAdapter();
        return new ClipboardAdapterImpl();
    }
}
