package dev.directplan.npjobs.npc.pathfinder;

import dev.directplan.npjobs.npc.NPCEntityPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.PathfindingContext;

import javax.annotation.Nullable;

/**
 * @author DirectPlan
 */
public class NPCPathfindingContext extends PathfindingContext {

    private final CollisionGetter level;
    @Nullable
    private final PathTypeCache cache;
    private final BlockPos mobPosition;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    NPCPathfindingContext(CollisionGetter var0, NPCEntityPlayer entityPlayer) {
        super(null, new Zombie(entityPlayer.level()));

        this.level = var0;
        Level var4 = entityPlayer.level();
        if (var4 instanceof ServerLevel var2) {
            this.cache = var2.getPathTypeCache();
        } else {
            this.cache = null;
        }

        this.mobPosition = entityPlayer.blockPosition();
    }

    @Override
    public PathType getPathTypeFromState(int var0, int var1, int var2) {
        BlockPos var3 = this.mutablePos.set(var0, var1, var2);
        return this.cache == null ? NPCNodeEvaluator.getPathTypeFromState(this.level, var3) : this.cache.getOrCompute(this.level, var3);
    }

    @Override
    public BlockState getBlockState(BlockPos var0) {
        return this.level.getBlockState(var0);
    }

    @Override
    public CollisionGetter level() {
        return this.level;
    }

    @Override
    public BlockPos mobPosition() {
        return this.mobPosition;
    }
}
