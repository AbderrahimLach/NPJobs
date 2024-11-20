package dev.directplan.npjobs.job;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author DirectPlan
 */
public final class JobManagerRegistry {

    private final Map<Plugin, JobManager> jobManagerMap = new HashMap<>();

    private final Plugin plugin;

    public JobManagerRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(@NotNull Plugin plugin, @NotNull JobManager jobManager) {
        String pluginName = plugin.getName();
        Preconditions.checkArgument(plugin.isEnabled(), "Plugin '" + pluginName + "' must be enabled");
        if (this.plugin == plugin && jobManagerMap.containsKey(plugin)) {
            throw new IllegalCallerException("You cannot register an instance for " + pluginName + " plugin.");
        }
        if (jobManagerMap.put(plugin, jobManager) != null) {
            plugin.getLogger()
                    .info("An instance of JobManager is already registered by " + pluginName + " plugin.");
        }
    }

    public JobManager get(@NotNull Plugin plugin) {
        return Objects.requireNonNull(jobManagerMap.get(plugin),
                "Could not find a JobManager instance by " + plugin.getName() + " plugin.");
    }
}
