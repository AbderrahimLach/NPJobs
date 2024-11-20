package dev.directplan.npjobs.npc;

import com.mojang.authlib.GameProfile;
import static java.util.Objects.requireNonNull;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * An internal implementation of {@link NPC}.
 * <p>
 * This implementation currently only supports Player NPCs. Insentient entities
 * might be added sometime in the future.
 *
 * @author DirectPlan
 */
public final class NPCImpl implements NPC {

    private final Plugin plugin;
    private final UUID id;
    private final String name;
    private Skin skin;

    private final NPCEntityPlayer serverPlayer;
    private final ServerLevel serverLevel;

    private final CraftWorld craftWorld;
    private final CraftPlayer craftPlayer;

    NPCImpl(Plugin plugin,
            World world,
            UUID id,
            String name,
            @Nullable Skin skin) {
        requireNonNull(world, "world");

        this.plugin = requireNonNull(plugin, "plugin");
        this.id = requireNonNull(id, "id");
        this.name = requireNonNull(name, "name");
        this.skin = skin;

        DedicatedServer dedicatedServer = ((CraftServer)plugin.getServer()).getServer();
        GameProfile gameProfile = new GameProfile(id, name);
        // Set skin
        if (skin != null) skin.apply(gameProfile);

        craftWorld = (CraftWorld) world;
        serverLevel = craftWorld.getHandle();
        serverPlayer = new NPCEntityPlayer(plugin, dedicatedServer, craftWorld.getHandle(), gameProfile);
        craftPlayer = serverPlayer.getBukkitEntity();
    }

    @Override
    public void spawn(@NotNull Location location) {
        serverPlayer.getEntityData().set(EntityDataSerializers.BYTE.createAccessor(17), (byte) 0xFF, true);
        craftWorld.addEntity(serverPlayer, CreatureSpawnEvent.SpawnReason.CUSTOM);
        teleport(location);
    }

    @Override
    public void tick() {
        serverPlayer.doTick();
    }

    @Override
    public void walkTo(@NotNull Location location, boolean sprinting) {
        float speedModifier = serverPlayer.getSpeedModifier();
        if (sprinting) speedModifier += 0.5F;

        serverPlayer.getNavigation().moveTo(location.getX(), location.getY(), location.getZ(), speedModifier);
    }

    @Override
    public void setSkin(@NotNull Skin skin) {
        this.skin = skin;

        skin.apply(serverPlayer.getGameProfile());

        if (!isSpawned()) return;
        // Refresh
        hide(null);
        Bukkit.getScheduler().runTask(plugin, () -> show(null));
    }

    @Override
    public void addObserver(@NotNull Player player) {
        show(player);
    }

    @Override
    public void removeObserver(@NotNull Player player) {
        hide(player);
    }

    @Override
    public void setHeldItem(@NotNull ItemStack item) {
        craftPlayer.getInventory().setItemInMainHand(item);
    }

    @Override
    public void setSneaking(boolean sneaking) {
        craftPlayer.setSneaking(sneaking);
    }

    @Override
    public void jump() {
        serverPlayer.jumpFromGround();
    }

    @Override
    public void playSwingAnimation() {
        serverPlayer.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void teleport(@NotNull Location location) {
        craftPlayer.teleport(location);
    }

    @Override
    public void remove() {
        serverPlayer.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return craftPlayer.getLocation();
    }

    @Override
    public Skin getSkin() {
        return skin;
    }

    @Override
    public boolean isJumping() {
        return serverPlayer.hasImpulse;
    }

    @Override
    public boolean isSpawned() {
        return craftPlayer.isValid();
    }

    @Override
    public boolean isNavigating() {
        return serverPlayer.isNavigating();
    }

    /**
     * Shows this NPC to the given {@code player} or to all online players within view distance range.
     * <p>
     * If the given {@code player} is null, this NPC will be shown to all online players
     * within view distance range.
     *
     * @param player player to show to.
     */
    private void show(@Nullable Player player) {
        Predicate<UUID> filter = player != null ? Predicate.isEqual(player.getUniqueId()) : null;
        PacketDispatcher.sendToEntityObservers(serverLevel, serverPlayer, filter,
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer),
                new ClientboundAddEntityPacket(serverPlayer, 0, serverPlayer.blockPosition()),
                new ClientboundSetEntityDataPacket(serverPlayer.getId(), serverPlayer.getEntityData().getNonDefaultValues())
        );

        // Wait a bit before sending player info remove packet.
        plugin.getServer()
                .getScheduler()
                .runTaskLater(plugin, () -> PacketDispatcher.sendToEntityObservers(serverLevel, serverPlayer, filter,
                        new ClientboundPlayerInfoRemovePacket(List.of(id))
                ), 5L);
    }

    private void hide(@Nullable Player player) {
        Predicate<UUID> filter = player != null ? Predicate.isEqual(player.getUniqueId()) : null;
        PacketDispatcher.sendToEntityObservers(serverLevel, serverPlayer, filter,
                new ClientboundPlayerInfoRemovePacket(List.of(id)),
                new ClientboundRemoveEntitiesPacket(serverPlayer.getId())
        );
    }
}
