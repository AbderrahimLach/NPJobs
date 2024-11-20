package dev.directplan.npjobs.job;

import dev.directplan.npjobs.keyed.NamespaceKeyed;
import dev.directplan.npjobs.npc.NPC;
import dev.directplan.npjobs.npc.NPCManager;
import dev.directplan.npjobs.util.EnumUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author DirectPlan
 */
public final class NPCWorker implements Worker {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final NamespaceKeyed id;
    private final NPCManager npcManager;
    private final NPC npc;
    private State state = State.NOT_SPAWNED;

    NPCWorker(NamespaceKeyed id, NPCManager npcManager, NPC npc) {
        this.id = id;
        this.npcManager = npcManager;
        this.npc = npc;
    }

    @Override
    public void spawn(Location location) {
        npc.spawn(location);
    }

    @Override
    public void remove() {
        npcManager.removeNpc(id.key());
    }

    @Override
    public void teleport(Location location) {
        npc.teleport(location);
    }

    @Override
    public void walkTo(Location location, boolean sprinting) {
        npc.walkTo(location, sprinting);
    }

    @Override
    public void jump() {
        npc.jump();
    }

    @Override
    public boolean isJumping() {
        return npc.isJumping();
    }

    @Override
    public boolean isNavigating() {
        return npc.isNavigating();
    }

    @Override
    public void playSwingAnimation() {
        npc.playSwingAnimation();
    }

    @Override
    public void setState(State state) {
        EnumUtils.checkAscendance(State.IDLING, state);
        this.state = state;
    }

    @Override
    public void setHeldItem(ItemStack item) {
        npc.setHeldItem(item != null ? item : AIR);
    }

    @Override
    public void setSneaking(boolean sneaking) {
        npc.setSneaking(sneaking);
    }

    @Override
    public void setSkin(Skin skin) {
        npc.setSkin(new NPC.Skin(skin.texture(), skin.signature()));
    }

    @Override
    public void setSkin(String name) {
        NPC.Skin.getSkin(name).thenAccept(skin -> setSkin(new Skin(skin.texture(), skin.signature())));
    }

    @Override
    public NamespaceKeyed getId() {
        return id;
    }

    @Override
    public String getName() {
        return npc.getName();
    }

    @Override
    public Location getLocation() {
        return npc.getLocation();
    }

    @Override
    public State getState() {
        return state;
    }
}
