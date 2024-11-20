package dev.directplan.npjobs.npc.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.directplan.npjobs.npc.NPCEntityPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author DirectPlan
 */
public final class NPCPathfinder extends PathFinder {

    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NPCNodeEvaluator nodeEvaluator;
    private final BinaryHeap openSet = new BinaryHeap();

    public NPCPathfinder(NPCNodeEvaluator nodeEvaluator, int maxVisitedNodes) {
        super(nodeEvaluator, maxVisitedNodes);

        this.nodeEvaluator = nodeEvaluator;
        this.maxVisitedNodes = maxVisitedNodes;
    }

    @Nullable
    public Path findPath(PathNavigationRegion var0, Mob var1, Set<BlockPos> var2, float var3, int var4, float var5) {
        throw new UnsupportedOperationException("Unsupported implementation for NPC");
    }

    public Path findPath(PathNavigationRegion var0,
                         NPCEntityPlayer var1,
                         Set<BlockPos> var2,
                         float followRange,
                         int var4,
                         float maxVisitedMultiplier) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(var0, var1);
        Node start = this.nodeEvaluator.getStart();
        Map<Target, BlockPos> var7 = var2.stream().collect(Collectors.toMap((var0x) ->
                this.nodeEvaluator.getTarget(var0x.getX(), var0x.getY(), var0x.getZ()), Function.identity()));
        Path var8 = this.findPath(var0.getProfiler(), start, var7, followRange, var4, maxVisitedMultiplier);
        this.nodeEvaluator.done();
        return var8;
    }

    @Nullable
    private Path findPath(ProfilerFiller var0, Node start, Map<Target, BlockPos> var2, float followRange, int var4, float maxVisitedMultiplier) {
        var0.push("find_path");
        var0.markForCharting(MetricCategory.PATH_FINDING);
        Set<Target> var6 = var2.keySet();
        start.g = 0.0F;
        start.h = this.getBestH(start, var6);
        start.f = start.h;
        this.openSet.clear();
        this.openSet.insert(start);
        int var8 = 0;
        Set<Target> var9 = Sets.newHashSetWithExpectedSize(var6.size());
        int maxVisitedNodes = (int)(this.maxVisitedNodes * maxVisitedMultiplier);

        while(!this.openSet.isEmpty()) {
            ++var8;
            if (var8 >= maxVisitedNodes) {
                break;
            }

            Node var11 = this.openSet.pop();
            var11.closed = true;

            for (Target var16 : var6) {
                if (var11.distanceManhattan(var16) <= (float) var4) {
                    var16.setReached();
                    var9.add(var16);
                }
            }

            if (!var9.isEmpty()) {
                break;
            }

            if (!(var11.distanceTo(start) >= followRange)) {
                int neighbors = this.nodeEvaluator.getNeighbors(this.neighbors, var11);

                for(int var13 = 0; var13 < neighbors; ++var13) {
                    Node var14 = this.neighbors[var13];
                    float var15 = this.distance(var11, var14);
                    var14.walkedDistance = var11.walkedDistance + var15;
                    float var16 = var11.g + var15 + var14.costMalus;
                    if (var14.walkedDistance < followRange && (!var14.inOpenSet() || var16 < var14.g)) {
                        var14.cameFrom = var11;
                        var14.g = var16;
                        var14.h = this.getBestH(var14, var6) * 1.5F;
                        if (var14.inOpenSet()) {
                            this.openSet.changeCost(var14, var14.g + var14.h);
                        } else {
                            var14.f = var14.g + var14.h;
                            this.openSet.insert(var14);
                        }
                    }
                }
            }
        }

        Optional<Path> var11 = !var9.isEmpty() ? var9.stream().map((var1x) -> {
            return this.reconstructPath(var1x.getBestNode(), var2.get(var1x), true);
        }).min(Comparator.comparingInt(Path::getNodeCount)) : var6.stream().map((var1x) -> {
            return this.reconstructPath(var1x.getBestNode(), var2.get(var1x), false);
        }).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        var0.pop();
        return var11.orElse(null);
    }

    protected float distance(Node var0, Node var1) {
        return var0.distanceTo(var1);
    }

    private float getBestH(Node var0, Set<Target> var1) {
        float var2 = Float.MAX_VALUE;

        float var5;
        for(Iterator<Target> var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            Target var6 = var4.next();
            var5 = var0.distanceTo(var6);
            var6.updateBest(var5, var0);
        }

        return var2;
    }

    private Path reconstructPath(Node var0, BlockPos var1, boolean var2) {
        List<Node> var3 = Lists.newArrayList();
        Node var4 = var0;
        var3.addFirst(var4);

        while(var4.cameFrom != null) {
            var4 = var4.cameFrom;
            var3.addFirst(var4);
        }

        return new Path(var3, var1, var2);
    }
}
