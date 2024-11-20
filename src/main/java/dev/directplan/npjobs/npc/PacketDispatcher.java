package dev.directplan.npjobs.npc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The primary functionalities of this class are deprecated and subject to removal,
 * except for {@link #sendToEntityObservers}.
 *
 * @author DirectPlan
 */
public final class PacketDispatcher {

    private final static PacketDispatcher INCLUSIVE = new PacketDispatcher(false);

    private Collection<Player> observers;

    private final boolean exclusive;

    private PacketDispatcher(boolean exclusive) {
        this.exclusive = exclusive;
    }

    /**
     * Sends one or more packets to players that observe the given {@code entity}.
     *
     * @param serverLevel server level.
     * @param entity the observed entity.
     * @param filter filter observers.
     * @param packets packet(s) to send.
     */
    public static void sendToEntityObservers(@NotNull ServerLevel serverLevel,
                                       @NotNull Entity entity,
                                       @Nullable Predicate<UUID> filter,
                                       @NotNull Packet<?>... packets) {
        ChunkMap.TrackedEntity trackedEntity = serverLevel.getChunkSource()
                .chunkMap
                .entityMap
                .get(entity.getId());
        trackedEntity.seenBy.forEach(connection -> {
            if (filter != null && !filter.test(connection.getPlayer().getUUID())) return;
            for (Packet<?> packet : packets) {
                connection.send(packet);
            }
        });
    }

    /**
     * Sends one or more packets to players that observe the given {@code entity}.
     *
     * @param serverLevel server level.
     * @param entity the observed entity.
     * @param packets packet(s) to send.
     */
    public static void sendToEntityObservers(@NotNull ServerLevel serverLevel,
                                       @NotNull Entity entity,
                                       @NotNull Packet<?>... packets) {
        sendToEntityObservers(serverLevel, entity, null, packets);
    }

    /**
     * Creates an exclusive instance of {@link PacketDispatcher} where
     * only the specified players will receive sent packet(s) using {@link #sendPacket}
     * <p>
     * You may still add observer(s) to this dispatcher using {@link #addObserver}
     *
     * @return an instance of {@link PacketDispatcher} specified by the parameters.
     */
    @Deprecated(forRemoval = true)
    public static PacketDispatcher of(@NotNull Player... players) {
        PacketDispatcher dispatcher = new PacketDispatcher(true);
        for (Player player : players) dispatcher.addObserver(player);
        return dispatcher;
    }

    /**
     * An inclusive instance of {@link PacketDispatcher} where
     * all online players will receive packets sent using {@link #sendPacket}
     *
     * @return a singleton instance of {@link PacketDispatcher}
     */
    @Deprecated(forRemoval = true)
    public static PacketDispatcher inclusive() {
        return INCLUSIVE;
    }

    /**
     * Returns either an inclusive singleton
     * or an exclusive instance of {@link PacketDispatcher} with
     * empty observers.
     * <p>
     * If an instance is inclusive, all online players will receive
     * packets sent using {@link #sendPacket}
     * <p>
     * If an instance is exclusive, only the specified group of players
     * added using {@link #addObserver} will receive packets sent using
     * {@link #sendPacket}
     *
     * @param exclusive whether to return an exclusive instance.
     * @return either an inclusive or exclusive instance based on
     * the specified parameter.
     * @see PacketDispatcher#of(Player...)
     * @see PacketDispatcher#inclusive()
     */
    @Deprecated(forRemoval = true)
    public static PacketDispatcher create(boolean exclusive) {
        if (exclusive) return of();
        return inclusive();
    }

    /**
     * Sends one or more NMS {@link Packet}s to the observers.
     * If this instance is non-exclusive, said packets will be sent
     * to all online players.
     *
     * @param packets packets to send
     */
    @Deprecated(forRemoval = true)
    public void sendPacket(@Nullable Packet<?>... packets) {
        getObservers().stream()
                .map(player -> ((CraftPlayer)player).getHandle().connection)
                .forEach(connection -> {
                    for (Packet<?> packet : packets) {
                        if (packet == null) continue;
                        connection.send(packet);
                    }
        }       );
    }

    /**
     * Adds a {@link Player player} to the observers list assuming that this
     * dispatcher is exclusive.
     *
     * @throws IllegalArgumentException if this dispatcher is inclusive.
     * @param player observer to add.
     */
    @Deprecated(forRemoval = true)
    public void addObserver(@NotNull Player player) {
        // There's no point in adding observers if this instance is inclusive.
        Preconditions.checkArgument(exclusive, "Cannot add a player to an inclusive dispatcher");

        if (observers == null) observers = Lists.newArrayList();
        observers.add(player);
    }

    /**
     * Removes {@link Player player} from the observers list assuming that this
     * dispatcher is exclusive.
     *
     * @throws IllegalArgumentException if this dispatcher is inclusive.
     * @param player observer to remove
     */
    @Deprecated(forRemoval = true)
    public void removeObserver(@NotNull Player player) {
        Preconditions.checkArgument(exclusive, "Cannot remove a player from an inclusive dispatcher");

        if (observers == null) return;
        observers.remove(player);
    }

    /**
     * Returns the players who will receive packets
     * sent using {@link #sendPacket}
     * <p>
     * If this dispatcher is exclusive, only the added {@link Collection<Player> observers}
     * will receive packets. Otherwise, this applies to all online players.
     *
     * @return players who will receive packets
     */
    @Deprecated(forRemoval = true)
    public Collection<? extends Player> getObservers() {
        if (exclusive) {
            if (observers == null) observers = Lists.newArrayList();
            return observers;
        }
        return Bukkit.getOnlinePlayers();
    }

    /**
     * @return Whether only a specified group of {@link Collection<Player> players}
     * will receive packets sent using {@link #sendPacket}
     */
    @Deprecated(forRemoval = true)
    public boolean isExclusive() {
        return exclusive;
    }

    @Deprecated(forRemoval = true)
    public boolean isEmpty() {
        return getObservers().isEmpty();
    }
}
