package dev.directplan.npjobs.npc;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author DirectPlan
 */
public interface NPCManager extends Iterable<NPC> {

    /**
     * Creates an internal new {@link NPCManager} instance using
     * the specified {@code plugin}.
     *
     * @param plugin bukkit plugin.
     * @return a new internal {@link NPCManager} instance.
     */
    static NPCManager create(Plugin plugin) {
        return new NPCManagerImpl(plugin);
    }

    /**
     * Retrieves a registered npc instance by the specified {@code npcName}.
     *
     * @param npcName npc name
     * @return npc instance by {@code npcName}.
     */
    NPC getNpc(String npcName);

    /**
     * Creates and registers a new instance of {@link NPC} in the specified {@code world},
     * identified by {@code id}, and named  {@code name}.
     *
     * @param world where to spawn the npc in
     * @param id the id which the npc will be identified by.
     * @param name the name of the npc.
     * @return the new instance of {@link NPC}.
     */
    NPC createNpc(World world, String id, String name);

    /**
     * Removes and de-spawns the {@link NPC} instance identified by {@code id}
     * from the registered NPCs map.
     *
     * @param id id of the npc to remove.
     * @return whether the npc was successfully removed.
     */
    boolean removeNpc(String id);

    /**
     * Removes and de-spawns all registered {@link NPC} instances.
     */
    void removeAll();

    /**
     * Gets the amount of NPCs registered.
     *
     * @return amount of NPCs registered.
     */
    int getSize();

    /**
     * Gets whether no NPCs are registered.
     *
     * @return whether no NPCs are registered.
     */
    boolean isEmpty();

    /**
     * Shows all NPCs within view distance range to the given {@code player}.
     *
     * @param player to show to.
     */
    default void addObserver(Player player) {
        forEach(npc -> npc.addObserver(player));
    }

    /**
     * Hides all NPCs within view distance range from the given {@code player}.
     *
     * @param player to hide from.
     */
    default void removeObserver(Player player) {
        forEach(npc -> npc.removeObserver(player));
    }
}
