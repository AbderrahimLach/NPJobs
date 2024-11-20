package dev.directplan.npjobs.npc;

/**
 * @author DirectPlan
 */
class NPCTickUpdate implements Runnable {

    private final NPCManager npcManager;

    public NPCTickUpdate(NPCManager npcManager) {
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
