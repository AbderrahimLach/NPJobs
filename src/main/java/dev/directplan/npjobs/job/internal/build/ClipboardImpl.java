package dev.directplan.npjobs.job.internal.build;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * An internal implementation of {@link Clipboard}.
 *
 * @author DirectPlan
 */
public final class ClipboardImpl implements Clipboard {

    private final Selection selection;
    private final ClipboardContext context;

    ClipboardImpl(Selection selection, ClipboardContext context) {
        this.selection = selection;
        this.context = context;
    }

    @Override
    public @NotNull PlaceableIterator iterator() {
        return new CuboidIterator(minPos(), maxPos(), vector -> {
            Block block = selection.world().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

            if (context.isIgnoreAir() && block.isEmpty()) return null;
            return new BlockPlaceable(block);
        });
    }

    @Override
    public @NotNull ClipboardContext getContext() {
        return context;
    }

    @Override
    public @NotNull Vector getOrigin() {
        return minPos();
    }

    @Override
    public long getVolume() {
        return (long) getWidth() * getHeight() * getLength();
    }

    @Override
    public int getWidth() {
        return maxPos().getBlockX() - minPos().getBlockX() + 1;
    }

    @Override
    public int getHeight() {
        return maxPos().getBlockY() - minPos().getBlockY() + 1;
    }

    @Override
    public int getLength() {
        return maxPos().getBlockZ() - minPos().getBlockZ() + 1;
    }

    private Vector minPos() {
        return selection.minPos();
    }

    private Vector maxPos() {
        return selection.maxPos();
    }
}