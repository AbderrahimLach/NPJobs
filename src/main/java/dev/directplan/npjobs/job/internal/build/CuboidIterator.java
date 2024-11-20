package dev.directplan.npjobs.job.internal.build;

import com.google.common.base.Suppliers;
import org.bukkit.util.Vector;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author DirectPlan
 */
public final class CuboidIterator implements PlaceableIterator {

    private final Vector minPos;
    private final Vector maxPos;
    private final Function<Vector, Placeable> function;

    private int x;
    private int y;
    private int z;
    private boolean positive = true;

    public CuboidIterator(Vector minPos, Vector maxPos, Function<Vector, Placeable> function) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.function = function;

        x = minPos.getBlockX();
        y = minPos.getBlockY();
        z = minPos.getBlockZ();
    }

    @Override
    public boolean hasNext() {
        return y <= maxPos.getBlockY();
    }

    @Override
    public Supplier<Placeable> next() {
        Supplier<Placeable> supplier = Suppliers.memoize(() -> function.apply(new Vector(x, y, z)));
        advance();
        return supplier;
    }

    private void advance() {
        // Snake traversal pattern. This is probably not the best way to do it.
        if (positive) {
            x++;
            if (x <= maxPos.getBlockX()) return;
            x = maxPos.getBlockX();
        } else {
            x--;
            if (x >= minPos.getBlockX()) return;
            x = minPos.getBlockX();
        }
        positive = !positive;

        z++;
        if (z <= maxPos.getBlockZ()) return;
        z = minPos.getBlockZ();
        y++;
    }
}
