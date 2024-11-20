package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.Worker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Represents objects or entities capable of being placed.
 *
 * @see BlockPlaceable
 * @see EntityPlaceable
 *
 * @author DirectPlan
 */
public interface Placeable {

    /**
     * Gets the location of ths placeable.
     *
     * @return location of this placeable.
     */
    @NotNull
    Location getLocation();

    /**
     * Gets the material used to place this placeable For example, if this placeable is a
     * {@link Material#WATER} block, this method will return {@link Material#WATER_BUCKET}.
     *
     * @return material used to place this placeable.
     */
    @NotNull
    Material getPlaceItem();

    /**
     * Places this placeable at the specified {@code location}.
     * <p>
     *     Returns true if this placeable is successfully placed,
     *     otherwise false if there is a non-matching block or entity
     *     placed at the specified {@code location}.
     * </p>
     * <p>
     *     <strong>Implementation Note:</strong>
     *     If a placement is unsuccessful, {@link BlockPlaceable} will try to set
     *     the block at {@code location} to AIR and then try setting the correct
     *     block in the next execution.
     * </p>
     *
     * @param worker the worker that will handle this placeable.
     * @param location where to place.
     * @return whether this placement is successful.
     */
    boolean place(@NonNull Worker worker, @NotNull Location location);
}
