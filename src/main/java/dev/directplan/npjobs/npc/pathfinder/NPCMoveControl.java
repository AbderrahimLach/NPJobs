package dev.directplan.npjobs.npc.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;

/**
 * @author DirectPlan
 */
public final class NPCMoveControl extends MoveControl {

    private final ServerPlayer serverPlayer;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    
    private float speedModifier;
    private boolean moving;

    public NPCMoveControl(ServerPlayer serverPlayer) {
        super(new Zombie(EntityType.ZOMBIE, serverPlayer.level()));

        this.serverPlayer = serverPlayer;
        this.wantedX = serverPlayer.getX();
        this.wantedY = serverPlayer.getY();
        this.wantedZ = serverPlayer.getZ();
    }

    @Override
    public void tick() {
        serverPlayer.zza = 0;
        if (!this.moving) return;

        this.moving = false;
        double distX = this.wantedX - this.serverPlayer.getX();
        double distY = this.wantedY - this.serverPlayer.getY();
        double distZ = this.wantedZ - this.serverPlayer.getZ();

        double distXSquare = Math.pow(distX, 2);
        double distZSquare = Math.pow(distZ, 2);

        double distance3d = Math.sqrt(distXSquare + Math.pow(distY, 2) + distXSquare);
        if (distance3d < 2.500000277905201E-7) return;

        double distance2d = Math.sqrt(distXSquare + distZSquare);
        if (distance2d > 0.4) {
            float f = (float) Math.toDegrees(Mth.atan2(distZ, distX)) - 90.0F;
            serverPlayer.setYRot(rotlerp(this.serverPlayer.getYRot(), f, 90.0F));
            serverPlayer.setYHeadRot(Location.normalizeYaw(serverPlayer.getYRot()));
        }
        float movementSpeed = (float) Attributes.MOVEMENT_SPEED.value().getDefaultValue();
        serverPlayer.zza = movementSpeed * speedModifier; // Set forward velocity

        Level level = serverPlayer.level();
        BlockPos pos = serverPlayer.blockPosition();
        BlockState blockState = level.getBlockState(pos);
        VoxelShape voxelShape = blockState.getCollisionShape(level, pos);
        if (distY >= serverPlayer.maxUpStep()
                && distance2d < Math.max(1.0F, serverPlayer.getBbWidth())
                || !voxelShape.isEmpty()
                && serverPlayer.getY() < voxelShape.max(Direction.Axis.Y) + pos.getY()
                && !blockState.is(BlockTags.DOORS)
                && !blockState.is(BlockTags.FENCES)) {
            serverPlayer.jumpFromGround();
        }
    }

    @Override
    public void setWantedPosition(double wantedX, double wantedY, double wantedZ, double speedModifier) {
        this.wantedX = wantedX;
        this.wantedY = wantedY;
        this.wantedZ = wantedZ;
        this.speedModifier = (float) speedModifier;
        this.moving = true;
    }

    @Override
    public double getWantedX() {
        return wantedX;
    }

    @Override
    public double getWantedY() {
        return wantedY;
    }

    @Override
    public double getWantedZ() {
        return wantedX;
    }

    public boolean isMoving() {
        return moving;
    }
}
