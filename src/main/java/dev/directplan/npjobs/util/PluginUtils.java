package dev.directplan.npjobs.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;

/**
 * @author DirectPlan
 */
public class PluginUtils {

    private static final Field COMMAND_MAP_FIELD;

    static {
        try {
            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerCommand(Plugin plugin, String name, Command command) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        try {
            CommandMap commandMap = (CommandMap) COMMAND_MAP_FIELD.get(pluginManager);
            commandMap.register(name, command);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
