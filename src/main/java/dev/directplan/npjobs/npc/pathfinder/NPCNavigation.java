package dev.directplan.npjobs.npc.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dev.directplan.npjobs.npc.NPCEntityPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author DirectPlan
 */
public final class NPCNavigation extends PathNavigation {

    private static final int MAXIMUM_VISITED_NODES = 1024;
    private static final double FOLLOW_RANGE = 100.0;
    private static final int NAVIGATION_TIME_OUT = 20 * 5; // 5 seconds.

    private final NPCEntityPlayer mob;
    private final NPCNodeEvaluator nodeEvaluator;
    private final NPCPathfinder pathfinder;

    private Path path;
    private double speedModifier;
    private int tick;
    private boolean hasDelayedRecomputation;
    private long timeLastRecompute;
    private BlockPos targetPos;
    private int reachRange;
    private float maxDistanceToWaypoint = 1F;
    private float maxVisitedNodesMultiplier = 1.0F;
    private boolean isStuck;

    private int lastPathAdvance;
    private Set<CraftBlock> openedDoors;

    public NPCNavigation(NPCEntityPlayer serverPlayer) {
        super(new Zombie(EntityType.ZOMBIE, serverPlayer.level()), serverPlayer.level());

        this.mob = serverPlayer;

        nodeEvaluator = new NPCNodeEvaluator();
        nodeEvaluator.setCanPassDoors(true);
        nodeEvaluator.setCanOpenDoors(true);
        nodeEvaluator.setCanWalkOverFences(true);
        pathfinder = new NPCPathfinder(nodeEvaluator, MAXIMUM_VISITED_NODES);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(float var0) {
        this.maxVisitedNodesMultiplier = var0;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        // PathNavigation superclass uses this method to construct a pathfinder
        // which we already did in the constructor block of this implementation.
        return null;
    }

    public void setSpeedModifier(double var0) {
        this.speedModifier = var0;
    }

    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }
    }

    @Nullable
    @Override
    public Path createPath(Stream<BlockPos> var0, int var1) {
        return this.createPath(var0.collect(Collectors.toSet()), 8, false, var1);
    }

    @Nullable
    @Override
    public Path createPath(Set<BlockPos> var0, int var1) {
        return this.createPath(var0, 8, false, var1);
    }

    @Nullable
    @Override
    public Path createPath(BlockPos var0, int var1) {
        return this.createPath(ImmutableSet.of(var0), 8, false, var1);
    }

    @Nullable
    @Override
    public Path createPath(BlockPos var0, int var1, int var2) {
        return this.createPath(ImmutableSet.of(var0), 8, false, var1, (float)var2);
    }

    @Nullable
    @Override
    public Path createPath(Entity var0, int var1) {
        return this.createPath(ImmutableSet.of(var0.blockPosition()), 16, true, var1);
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean var2, int var3) {
        return this.createPath(var0, var1, var2, var3, (float) FOLLOW_RANGE);
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean var2, int var3, float followRange) {
        if (var0.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && var0.contains(this.targetPos)) {
            return this.path;
        } else {
            this.level.getProfiler().push("npjobs:pathfind");
            BlockPos var5 = var2 ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int var6 = (int)(followRange + (float)var1);
            PathNavigationRegion var7 = new PathNavigationRegion(this.level, var5.offset(-var6, -var6, -var6), var5.offset(var6, var6, var6));
            Path var8 = this.pathfinder.findPath(var7, this.mob, var0, followRange, var3, this.maxVisitedNodesMultiplier);
            this.level.getProfiler().pop();
            if (var8 != null && var8.getTarget() != null) {
                this.targetPos = var8.getTarget();
                this.reachRange = var3;
                this.resetStuckTimeout();
            }

            return var8;
        }
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speedModifier) {
        return this.moveTo(this.createPath(x, y, z, 1), speedModifier);
    }

    @Override
    public boolean moveTo(Entity entity, double speedModifier) {
        Path var3 = this.createPath(entity, 1);
        return var3 != null && this.moveTo(var3, speedModifier);
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speedModifier) {
        if (path == null) {
            this.path = null;
            return false;
        }

        if (!path.sameAs(this.path)) this.path = path;

        if (isDone()) return false;

        this.trimPath();

        if (this.path.getNodeCount() <= 0) return false;

        this.speedModifier = speedModifier;
        this.lastStuckCheck = this.tick;
        this.lastStuckCheckPos = this.getTempMobPos();
        this.openedDoors = Sets.newConcurrentHashSet();
        return true;
    }

    @Nullable
    public Path getPath() {
        return this.path;
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (this.isDone()) {
            return;
        }

//        System.out.println("Path (" + path.getNextNodeIndex() + "/" + path.getNodeCount() + ")");
        if (canUpdatePath()) {
            followThePath();
        }
        if (isDone()) return;
        Vec3 pos = path.getNextEntityPos(this.mob);

        checkDoorPath();
        doStuckDetection(getTempMobPos());

        mob.getMoveControl().setWantedPosition(pos.x, this.getGroundY(pos), pos.z, speedModifier);
    }


    protected void followThePath() {
        Vec3 playerPos = this.getTempMobPos();

        this.maxDistanceToWaypoint = (this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F);
        Node nextNode = this.path.getNextNode();
        Vec3i nodePos = nextNode.asBlockPos();
        double diffX = Math.abs(this.mob.getX() - ((double) nodePos.getX() + 0.5));
        double diffY = Math.abs(this.mob.getY() - (double) nodePos.getY());
        double diffZ = Math.abs(this.mob.getZ() - ((double) nodePos.getZ() + 0.5));

        boolean withinWaypointDistance = diffX < (double)this.maxDistanceToWaypoint && diffZ < (double) this.maxDistanceToWaypoint && diffY < 1.0;
        boolean canCutCorner = this.canCutCorner(nextNode.type);
        boolean shouldTargetNextNodeInDirection = this.shouldTargetNextNodeInDirection(playerPos);

        if (withinWaypointDistance || canCutCorner && shouldTargetNextNodeInDirection) {
            advancePath();
        }
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 playerPos) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 centerCurrentPos = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!playerPos.closerThan(centerCurrentPos, 2.0)) {
                return false;
            } else if (this.canMoveDirectly(playerPos, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3 centerNextPos = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 centerPosVector = centerCurrentPos.subtract(playerPos);
                Vec3 centerNextPosVector = centerNextPos.subtract(playerPos);
                double centerPosDistance = centerPosVector.lengthSqr();
                double centerNextPosDistance = centerNextPosVector.lengthSqr();
                boolean var9 = centerNextPosDistance < centerPosDistance;
                boolean var10 = centerPosDistance < 0.5;
                if (!var9 && !var10) {
                    return false;
                } else {
                    Vec3 var11 = centerPosVector.normalize();
                    Vec3 var12 = centerNextPosVector.normalize();
                    return var12.dot(var11) < 0.0;
                }
            }
        }
    }

    private void advancePath() {
        path.advance();

        isStuck = false;
        lastPathAdvance = tick;
    }

    @Override
    protected void doStuckDetection(Vec3 playerPos) {
        Node endNode = path.getEndNode();
        if (endNode == null) return;

        double distance = playerPos.distanceTo(endNode.asVec3());
        double velocity = mob.getVelocity();
        double estimatedTimeToNode = (distance / velocity) + 30;

        int deltaTime = tick - lastPathAdvance;
        if (deltaTime > estimatedTimeToNode) {
            isStuck = true;
            timeoutPath();
        }
    }

    private void checkDoorPath() {
        if (!nodeEvaluator.canOpenDoors()) return;

        checkOpenedDoors(false);

        Node node = path.getNextNode();
        if (node.type != PathType.WALKABLE_DOOR) return;

        BlockPos blockPos = node.asBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof DoorBlock doorBlock) {
            if (!DoorBlock.isWoodenDoor(blockState) || doorBlock.isOpen(blockState)) return;

            doorBlock.setOpen(mob, level, blockState, blockPos, true);
            // We need to store this open door so it can be closed later on.
            openedDoors.add(new CraftBlock(level, blockPos));
        }
    }

    private void checkOpenedDoors(boolean forceClean) {
        if (openedDoors == null) return;

        for (CraftBlock block : openedDoors) {
            BlockState state = block.getNMS();
            if (state.getBlock() instanceof DoorBlock doorBlock) {
                // Check if the door is actually open.
                if (doorBlock.isOpen(state)) {
                    BlockPos position = block.getPosition();
                    double distanceSquare = mob.distanceToSqr(position.getX(), position.getY(), position.getZ());
                    if (!forceClean && distanceSquare < 2.25) return; // sqrt(2.25) = 1.5 blocks range

                    doorBlock.setOpen(mob, level, state, position, false);
                }
                openedDoors.remove(block);
            }
        }
    }

    public void stop() {
        this.path = null;
        checkOpenedDoors(true);
        openedDoors = null;
    }

    public boolean isDone() {
        boolean done = path == null || path.isDone();
        if (done && path != null) {
            // Invalidate
            stop();
        }
        return done;
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0;
        this.isStuck = false;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
    }

    @Override
    protected double getGroundY(Vec3 var0) {
        BlockPos var1 = BlockPos.containing(var0);
        return this.level.getBlockState(var1.below()).isAir() ? var0.y : WalkNodeEvaluator.getFloorLevel(this.level, var1);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    private boolean isInLiquid() {
        return this.mob.isInWaterOrBubble() || this.mob.isInLava();
    }

    protected void trimPath() {
        if (this.path == null) return;

        for(int var0 = 0; var0 < this.path.getNodeCount(); ++var0) {
            Node var1 = this.path.getNode(var0);
            Node var2 = var0 + 1 < this.path.getNodeCount() ? this.path.getNode(var0 + 1) : null;
            BlockState var3 = this.level.getBlockState(new BlockPos(var1.x, var1.y, var1.z));

            if (!var3.is(BlockTags.CAULDRONS)) continue;
            this.path.replaceNode(var0, var1.cloneAndMove(var1.x, var1.y + 1, var1.z));
            if (var2 != null && var1.y >= var2.y) {
                this.path.replaceNode(var0 + 1, var1.cloneAndMove(var2.x, var1.y + 1, var2.z));
            }
        }
    }

    protected boolean canMoveDirectly(Vec3 var0, Vec3 var1) {
        return false;
    }

    public boolean canCutCorner(PathType pathType) {
        return pathType != PathType.DANGER_FIRE
                && pathType != PathType.DANGER_POWDER_SNOW
                && pathType != PathType.DANGER_OTHER;
//                && pathType != PathType.WALKABLE_DOOR;
    }


    public boolean isStableDestination(BlockPos var0) {
        BlockPos var1 = var0.below();
        return this.level.getBlockState(var1).isSolidRender(this.level, var1);
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean var0) {
        this.nodeEvaluator.setCanFloat(var0);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    public boolean shouldRecomputePath(BlockPos var0) {
        if (isStuck) return true;

        if (this.hasDelayedRecomputation) {
            return false;
        } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            Node var1 = this.path.getEndNode();
            Vec3 var2 = new Vec3(((double)var1.x + this.mob.getX()) / 2.0, ((double)var1.y + this.mob.getY()) / 2.0, ((double)var1.z + this.mob.getZ()) / 2.0);
            return var0.closerToCenterThan(var2, this.path.getNodeCount() - this.path.getNextNodeIndex());
        } else {
            return false;
        }
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck() {
        return this.isStuck;
    }
}
