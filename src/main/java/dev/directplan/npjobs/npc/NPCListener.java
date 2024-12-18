package dev.directplan.npjobs.npc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author DirectPlan
 */
public final class NPCListener implements Listener {

    private final NPCManager npcManager;

    NPCListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        npcManager.addObserver(event.getPlayer());
    }
}
