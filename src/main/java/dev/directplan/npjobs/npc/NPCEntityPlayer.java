package dev.directplan.npjobs.npc;

import com.mojang.authlib.GameProfile;
import dev.directplan.npjobs.npc.pathfinder.NPCMoveControl;
import dev.directplan.npjobs.npc.pathfinder.NPCNavigation;
import dev.directplan.npjobs.util.EmptyChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author DirectPlan
 */
public final class NPCEntityPlayer extends ServerPlayer {

    private static final ClientInformation CLIENT_INFORMATION = ClientInformation.createDefault();

    private final Plugin plugin;
    private final NPCMoveControl moveControl;
    private final NPCNavigation navigation;

    private float speedModifier = 1.3F;
    private float velocity;
    private Vec3 lastPos;

    NPCEntityPlayer(Plugin plugin, MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile, CLIENT_INFORMATION);

        this.plugin = plugin;
        moveControl = new NPCMoveControl(this);
        navigation = new NPCNavigation(this);

        EmptyConnection emptyConnection = new EmptyConnection();
        connection = new EmptyServerGamePacketListenerImpl(server, emptyConnection, this);
    }

    @Override
    public void doTick() {
        super.doTick();

        Vec3 current = position();
        if (lastPos == null) lastPos = current;
        velocity = (float) current.distanceTo(lastPos);
        lastPos = current;

        navigation.tick();
        if (isNavigating()) {
            moveControl.tick();
            travel(new Vec3(this.xxa, this.yya, this.zza));
        }
    }

    /**
     * A Notchian client will always require an info {@link ClientboundPlayerInfoUpdatePacket ADD_PLAYER} packet prior
     * to spawning the player to other players. Once the packet is received by a client, the player will be
     * added to the tab list.
     * <p>
     * An NPC will need to be removed from the tab list after they're spawned for obvious reasons, and we can do so by
     * sending an {@link ClientboundPlayerInfoRemovePacket} to every online player. The problem with doing so is that when
     * a player goes out of range and then returns within range from an NPC, it will not be shown again to the players
     * simply because it requires sending the {@link ClientboundPlayerInfoUpdatePacket ADD_PLAYER} again and this is
     * not handled by {@link ServerEntity#sendPairingData} because it only treats real players that broadcast the ADD_PLAYER
     * packet just once per login.
     * <p>
     * When this method is called, we can know for sure that the NPC is about to be broadcast. This override takes
     * advantage of this and implements a fix for the problem mentioned above.
     *
     * @see ServerEntity#sendPairingData(ServerPlayer, Consumer)
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entitytrackerentry) {

        List<ServerPlayer> witnesses = new ArrayList<>();

        ServerLevel serverLevel = (ServerLevel) level();
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
            Vec3 vec3d = serverPlayer.position().subtract(position());
            int viewDistance = Mth.clamp(serverPlayer.requestedViewDistance(), 2, chunkMap.serverViewDistance);
            double viewRange = Math.min(EntityType.PLAYER.clientTrackingRange() * 16, viewDistance * 16);
            double vecSquare = vec3d.x * vec3d.x + vec3d.z * vec3d.z;
            double viewRangeSquare = viewRange * viewRange;

            if (vecSquare <= viewRangeSquare) {
                serverPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(this)));
                witnesses.add(serverPlayer);
            }
        }
        plugin.getServer()
                .getScheduler()
                .runTaskLater(plugin, () -> witnesses.forEach(serverPlayer ->
                        serverPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(getUUID())))),
                        20L // We need to wait a certain period before sending info remove packet. One second is ideal.
                );
        return super.getAddEntityPacket(entitytrackerentry);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damagesource) {
        return true;
    }

    @Override
    public boolean canTakeItem(ItemStack itemstack) {
        return false;
    }

    @Override
    public float getHealth() {
        return 20.0F;
    }

    public NPCMoveControl getMoveControl() {
        return moveControl;
    }

    public NPCNavigation getNavigation() {
        return navigation;
    }

    public float getPathfindingMalus(PathType pathType) {
        return pathType.getMalus();
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    /**
     * Returns velocity measured in distance per tick. Each second is 20 ticks.
     *
     * @return velocity of the npc
     */
    public double getVelocity() {
        return velocity;
    }

    public boolean isNavigating() {
        return navigation.isInProgress();
    }

    public void setSpeedModifier(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    static class EmptyServerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

        public EmptyServerGamePacketListenerImpl(MinecraftServer server, Connection connection, ServerPlayer serverPlayer) {
            super(server, connection, serverPlayer, CommonListenerCookie.createInitial(serverPlayer.getGameProfile(), false));
        }

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {}
    }

    static class EmptyConnection extends Connection {

        public EmptyConnection() {
            super(PacketFlow.CLIENTBOUND);

            channel = new EmptyChannel();
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) {}
    }
}
