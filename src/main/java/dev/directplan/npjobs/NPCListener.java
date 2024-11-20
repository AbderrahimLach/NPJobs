package dev.directplan.npjobs;

import dev.directplan.npjobs.npc.NPCManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author DirectPlan
 */
class NPCListener implements Listener {

    private final NPCManager npcManager;

    public NPCListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        npcManager.addObserver(event.getPlayer());
    }
}
