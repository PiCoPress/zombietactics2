package net.picopress.mc.mods.zombietactics2.goals.mining;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.util.Tactics;
import net.picopress.mc.mods.zombietactics2.goals.BreakBlockGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;


// destroy specific block
public class DestroyBlockGoal extends BreakBlockGoal {
    private final Block block;
    private final int range;
    private int delay = 0;
    private int y;

    public DestroyBlockGoal(Mob mob, Block block, int range) {
        super(mob, Config.hardnessMultiplier, Config.break_speed, Config.dropBlocks);
        this.block = block;
        this.range = range;
        y = -range;
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if(!super.canUse()) return false;
        ++ delay;
        if(delay < 2) return false;
        delay = 0;
        BlockPos mob_pos = mob.blockPosition();
        double dist = Double.MAX_VALUE;

        List<BlockPos> positions = Tactics.World.findBlocks(mob.level(), block,
                mob_pos.getX() - range, mob_pos.getY() + y, mob_pos.getZ() - range,
                mob_pos.getX() + range, mob_pos.getY() + y, mob_pos.getZ() + range);

        // update y before return
        ++ y;
        if(y > range) y = -range;
        if(positions.isEmpty()) return false;
        // priority to the closest block
        for(var pos: positions) {
            double d = mob.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            if(d < dist) {
                dist = d;
                mine.bp = pos;
                mine.bp_vec3 = pos.getCenter();
            }
        }
        // this cannot be null, but the ide warns
        // move and check Manhattan distance
        Path p = mob.getNavigation().createPath(mine.bp, 0);
        if(p != null && p.getEndNode() != null && p.getEndNode().distanceManhattan(mine.bp) < 3) {
            mob.getNavigation().moveTo(p, 1);
        } else return false;
        return Tactics.World.ManhattanDistance(Objects.requireNonNull(mine.bp), mob.blockPosition()) < 3;
    }
}
