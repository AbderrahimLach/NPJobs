package dev.directplan.npjobs.job.internal.build;

import dev.directplan.npjobs.job.Worker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * @author DirectPlan
 */
public final class BlockPlaceable implements Placeable {

    private final BlockData blockData;
    private final Location location;

    public BlockPlaceable(Block clipboard) {
        this.blockData = clipboard.getBlockData();
        this.location = clipboard.getLocation();
    }

    public BlockPlaceable(BlockData blockData, Location location) {
        this.blockData = blockData;
        this.location = location;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public @NotNull Material getPlaceItem() {
        return blockData.getPlacementMaterial();
    }

    @Override
    public boolean place(@NotNull Worker worker, @NotNull Location location) {
        Block block = location.getBlock();
        if (block.getBlockData().equals(blockData)) return true;

        if (block.getType() != Material.AIR) {
            block.setType(Material.AIR, false);
            playBlockSound(block, location, true);
            return false;
        }

        block.setBlockData(blockData, false);
        playBlockSound(block, location, false);
        return true;
    }

    private void playBlockSound(Block block, Location location, boolean breakSound) {
        SoundGroup soundGroup = blockData.getSoundGroup();
        Sound sound = breakSound ? soundGroup.getBreakSound() : soundGroup.getPlaceSound();

        block.getWorld().playSound(location, sound, soundGroup.getVolume(), soundGroup.getPitch());
    }
}
