package dev.directplan.npjobs.job.internal.build.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.directplan.npjobs.job.internal.build.BlockPlaceable;
import dev.directplan.npjobs.job.internal.build.Clipboard;
import dev.directplan.npjobs.job.internal.build.ClipboardContext;
import dev.directplan.npjobs.job.internal.build.CuboidIterator;
import dev.directplan.npjobs.job.internal.build.PlaceableIterator;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public final class WorldEditClipboard implements Clipboard {

    private final com.sk89q.worldedit.extent.clipboard.Clipboard clipboard;
    private final ClipboardContext context;

    public WorldEditClipboard(@NotNull com.sk89q.worldedit.extent.clipboard.Clipboard clipboard,
                              @NotNull ClipboardContext context) {
        this.clipboard = clipboard;
        this.context = context;
    }

    @Override
    public @NotNull PlaceableIterator iterator() {
        Vector minPos = toVector(clipboard.getMinimumPoint());
        Vector maxPos = toVector(clipboard.getMaximumPoint());

        return new CuboidIterator(minPos, maxPos, vector -> {
            BlockVector3 vector3 = new BlockVector3(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            BlockState blockState = clipboard.getBlock(vector3);
            if (context.isIgnoreAir() && blockState.getBlockType().equals(BlockTypes.AIR)) {
                return null;
            }
            BlockData blockData = BukkitAdapter.adapt(blockState);
            return new BlockPlaceable(blockData, new Location(null, vector3.x(), vector3.y(), vector3.z()));
        });
    }

    @Override
    public @NotNull ClipboardContext getContext() {
        return context;
    }

    @Override
    public @NotNull Vector getOrigin() {
        return toVector(clipboard.getOrigin());
    }

    @Override
    public long getVolume() {
        return clipboard.getRegion().getVolume();
    }

    @Override
    public int getWidth() {
        return clipboard.getRegion().getWidth();
    }

    @Override
    public int getHeight() {
        return clipboard.getRegion().getHeight();
    }

    @Override
    public int getLength() {
        return clipboard.getRegion().getLength();
    }

    private Vector toVector(BlockVector3 vector3) {
        return new Vector(vector3.x(), vector3.y(), vector3.z());
    }
}
