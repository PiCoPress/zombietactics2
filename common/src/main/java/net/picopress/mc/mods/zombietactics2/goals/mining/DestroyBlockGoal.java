package net.picopress.mc.mods.zombietactics2.goals.mining;

import net.picopress.mc.mods.zombietactics2.util.Tactics;
import net.picopress.mc.mods.zombietactics2.goals.BreakBlockGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.List;


// destroy specific block
public class DestroyBlockGoal extends BreakBlockGoal {
    private final Block block;
    private int delay = 0;
    private int y = -6;
    private final int range;

    public DestroyBlockGoal(Mob mob, Block block, int range) {
        super(mob, 5, 0.2, false);
        this.block = block;
        this.range = range;
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
        // 24*24*24
        List<BlockPos> positions = Tactics.World.findBlocks(level, block,
                mob_pos.getX() - range, mob_pos.getY() + y, mob_pos.getZ() - range,
                mob_pos.getX() + range, mob_pos.getY() + y, mob_pos.getZ() + range);

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
        ++ y;
        if(y > range) y = -range;
        // this cannot be null but the ide warns
        // move and check distance
        Path p = mob.getNavigation().createPath(mine.bp, 2);
        if(p != null && p.canReach()) {
            mob.getNavigation().moveTo(p, 1);
        } else return false;
        return mob.distanceToSqr(mine.bp_vec3) < 4;
    }
}
