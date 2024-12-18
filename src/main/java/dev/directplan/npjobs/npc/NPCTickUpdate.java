package dev.directplan.npjobs.npc;

/**
 * @author DirectPlan
 */
public final class NPCTickUpdate implements Runnable {

    private final NPCManager npcManager;

    NPCTickUpdate(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void run() {
        if (npcManager.isEmpty()) return;
        for (NPC npc : npcManager) {
            if (!npc.isSpawned()) continue;
            npc.tick();
        }
    }
}
