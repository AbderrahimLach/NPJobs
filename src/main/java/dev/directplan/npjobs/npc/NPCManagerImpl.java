package dev.directplan.npjobs.npc;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * An internal implementation of {@link NPCManager}.
 *
 * @author DirectPlan
 */
public final class NPCManagerImpl implements NPCManager {

    private final Map<String, NPC> npcMap = new HashMap<>();

    private final Plugin plugin;

    NPCManagerImpl(Plugin plugin) {
        this.plugin = plugin;

        plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, new NPCTickUpdate(this), 1, 1);

        plugin.getServer()
                .getPluginManager()
                .registerEvents(new NPCListener(this), plugin);
    }

    @Override
    public NPC createNpc(World world, String id, String name) {
        NPC npc = new NPCImpl(plugin, world, UUID.randomUUID(), name, null);
        npcMap.put(id, npc);
        return npc;
    }

    @Override
    public boolean removeNpc(String id) {
        NPC npc = npcMap.remove(id);
        if (npc == null) return false;

        if (!npc.isSpawned()) return true;
        npc.remove();
        return true;
    }

    @Override
    public NPC getNpc(String npcName) {
        return npcMap.get(npcName);
    }

    @Override
    public int getSize() {
        return npcMap.size();
    }

    @Override
    public boolean isEmpty() {
        return npcMap.isEmpty();
    }

    @Override
    public void removeAll() {
        npcMap.forEach((s, npc) -> npc.remove());
        npcMap.clear();
    }

    @Override
    public @NotNull Iterator<NPC> iterator() {
        return npcMap.values()
                .iterator();
    }
}
