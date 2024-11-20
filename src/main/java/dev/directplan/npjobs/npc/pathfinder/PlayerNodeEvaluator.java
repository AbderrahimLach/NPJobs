package dev.directplan.npjobs.npc.pathfinder;

import dev.directplan.npjobs.npc.NPCEntityPlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;

/**
 * @author DirectPlan
 */
abstract class PlayerNodeEvaluator extends NodeEvaluator {

    protected NPCPathfindingContext currentContext;
    protected NPCEntityPlayer mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;
    protected boolean canWalkOverFences;

    void prepare(PathNavigationRegion pathNavigationRegion, NPCEntityPlayer player) {
        this.currentContext = new NPCPathfindingContext(pathNavigationRegion, player);
        this.mob = player;
        this.nodes.clear();
        this.entityWidth = Mth.floor(player.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(player.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(player.getBbWidth() + 1.0F);
    }

    @Override
    public void done() {
        this.mob = null;
    }

    protected Node getNode(BlockPos var0) {
        return this.getNode(var0.getX(), var0.getY(), var0.getZ());
    }

    protected Node getNode(int var0, int var1, int var2) {
        return this.nodes.computeIfAbsent(Node.createHash(var0, var1, var2), (_) -> new Node(var0, var1, var2));
    }

//    @Override
//    public PathType getBlockPathType(BlockGetter blockGetter, int i, int i1, int i2, Mob mob) {
//        throw new UnsupportedOperationException("Unsupported method for NPC");
//    }
//
//    public abstract PathType getBlockPathType(BlockGetter blockGetter, int i, int i1, int i2, NPCEntityPlayer mob);

    protected Target getTargetFromNode(Node var0) {
        return new Target(var0);
    }

    public void setCanPassDoors(boolean var0) {
        this.canPassDoors = var0;
    }

    public void setCanOpenDoors(boolean var0) {
        this.canOpenDoors = var0;
    }

    public void setCanFloat(boolean var0) {
        this.canFloat = var0;
    }

    public void setCanWalkOverFences(boolean var0) {
        this.canWalkOverFences = var0;
    }

    @Override
    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    @Override
    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }

    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }
}
