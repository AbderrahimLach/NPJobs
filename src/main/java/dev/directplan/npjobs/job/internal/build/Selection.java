package dev.directplan.npjobs.job.internal.build;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * @author DirectPlan
 */
public interface Selection {

    /**
     * Gets the world of this selection
     *
     * @return the world.
     */
    World world();

    /**
     * Gets the minimum position of this selection.
     *
     * @return the minimum position.
     */
    Vector minPos();

    /**
     * Gets the maximum position of this selection.
     *
     * @return the maximum position.
     */
    Vector maxPos();

    static Selection from(World world, Vector firstPos, Vector secondPos) {
        return new SelectionImpl(world, BoundingBox.of(firstPos, secondPos));
    }

    static Selection from(World world, Location firstPos, Location secondPos) {
        return from(world, firstPos.toVector(), secondPos.toVector());
    }

    record SelectionImpl(World world, BoundingBox boundingBox) implements Selection {

        @Override
        public Vector minPos() {
            return boundingBox.getMin();
        }

        @Override
        public Vector maxPos() {
            return boundingBox.getMax();
        }
    }
}
