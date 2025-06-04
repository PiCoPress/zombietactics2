package net.picopress.mc.mods.zombietactics2.goals.mining;

import static net.picopress.mc.mods.zombietactics2.attachments.MiningRoutines.*;
import static net.picopress.mc.mods.zombietactics2.util.Tactics.getRelativeRotation;
import net.picopress.mc.mods.zombietactics2.attachments.MiningData;
import net.picopress.mc.mods.zombietactics2.goals.BreakBlockGoal;
import net.picopress.mc.mods.zombietactics2.Config;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;

import org.jetbrains.annotations.NotNull;


public class MonsterBreakBlockGoal<T extends Monster> extends BreakBlockGoal {
    private final T zombie;
    double max_p2, min_p2;

    public MiningData mine;

    public MonsterBreakBlockGoal(T zombie, MiningData mine) {
        super(zombie, mine, Config.hardnessMultiplier, Config.break_speed, Config.dropBlocks);
        this.mine = mine;
        this.zombie = zombie;
    }

    // get deltaY between me and target
    // then return a proper set of positions
    protected BlockPos[] getCandidate(@NotNull LivingEntity liv) {
        double deltaY = liv.getY() - zombie.getY();
        if(deltaY > -2 && deltaY < 2)
            return routineFlat;
        else if(deltaY <= -2)
            return routineDown;
        else // deltaY >= 2
            return routineUp;
    }

    // is valid to mine?
    @Override
    protected boolean checkBlock(BlockPos pos) {
        float destroying = getBlockHardness(pos);
        // except doors
        boolean ret = super.checkBlock(pos) && destroying <= Config.maxHardness && !zombie.level().getBlockState(pos).is(BlockTags.DOORS);
        if(ret) {
            mine.bp = pos;
            mine.bp_vec3 = pos.getCenter();
            mine.doMining = true;
        }
        return ret;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && zombie.distanceToSqr(mine.bp_vec3) <= max_p2;
    }

    @Override
    public void tick() {
        double dist = zombie.distanceToSqr(mine.bp_vec3);
        if (dist < min_p2 || dist > max_p2) {
            mine.doMining = false;
            return;
        }
        super.tick();
    }

    @Override
    public boolean canUse() {
        if(!super.canUse()) return false;

        // a zombie should be stuck
        // check availability of the mining
        final double len = zombie.getDeltaMovement().length();
        if(len > 0.8 || Config.strictMine && len > 0.1) { // striction for movement
            // relaxed for flying zombies
            if(!Config.canFly) return false;
        }

        // found a path but a zombie stuck
        LivingEntity liv = zombie.getTarget();
        PathNavigation nav = zombie.getNavigation();
        if(nav.isDone() && liv != null && nav.getPath() != null && !nav.getPath().canReach()) {
            if(zombie.isWithinMeleeAttackRange(liv) && zombie.hasLineOfSight(liv)) return false;

            // why is the path null even though it can reach a target?
            // the sucks

            // go once more
            // Issue: moveTo sometimes return false while a zombie can go to the target.
            // It can solve by using the method `hasLineOfSight` but this causes a problem
            // about fences that have 1.5 meters tall.
            if(nav.moveTo(liv, zombie.getSpeed())) return false;
            final BlockPos[] set = getCandidate(liv);

            for(BlockPos pos: set) {
                // checkBlock method is able to change 'zombie' variable
                // So 'temp' cannot be determined as a valid object
                // selects relative block position
                BlockPos temp = zombie.blockPosition().offset(pos.rotate(getRelativeRotation(zombie)));
                if(checkBlock(temp))
                    return true;
            }

            // zombie is in the wall
        } else if(zombie.isInWall()) {
            for(BlockPos p: routineWall) {
                BlockPos temp = zombie.blockPosition().offset(p);
                if(checkBlock(temp)) return true;
            }
        } else {
            // stuck in the water
            final BlockPos head2 = zombie.blockPosition().above(2);
            if(zombie.isEyeInFluid(FluidTags.WATER) && zombie.level().getBlockState(head2).canOcclude()) {
                return checkBlock(head2);
            }
        }
        // zombie cannot escape
        return false;
    }

    @Override
    public void start() {
        super.start();
        min_p2 = Config.minDist * Config.minDist;
        max_p2 = Config.maxDist * Config.maxDist;
    }
}
