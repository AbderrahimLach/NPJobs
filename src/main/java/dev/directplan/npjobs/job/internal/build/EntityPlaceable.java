package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.Worker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author DirectPlan
 */
public final class EntityPlaceable implements Placeable {

    private final Entity clipboard;

    public EntityPlaceable(Entity clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    public @NotNull Location getLocation() {
        return clipboard.getLocation();
    }

    @Override
    public @NotNull Material getPlaceItem() {
        // TODO: Add or find a material mapping for non-living entities.
        EntityType clipboardType = clipboard.getType();
        return Material.valueOf(clipboardType.name() + "_SPAWN_EGG");
    }

    @Override
    public boolean place(@NotNull Worker worker,  @NotNull Location location) {
        World world = Objects.requireNonNull(location.getWorld(), "world");
        Entity entity = world.spawnEntity(location, clipboard.getType(), false);
        // TODO: Clone NBT

        entity.teleport(location);
        return true;
    }
}
