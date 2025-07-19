package net.picopress.mc.mods.zombietactics2.ai.goals;

import net.picopress.mc.mods.zombietactics2.attachments.MiningData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;

import java.util.EnumSet;


// independent of Config
public abstract class BreakBlockGoal extends Goal {
    private final double hardnessMultiplier;
    private final double break_speed;
    private int miningTicks;
    private final boolean dropBlock;

    protected final Mob mob;
    protected double progress, hardness = Double.MAX_VALUE;

    public final MiningData mine;

    public BreakBlockGoal(Mob mob, MiningData mine, double hardnessMultiplier, double break_speed, boolean dropBlock) {
        this.mine = mine;
        this.mob = mob;
        this.hardnessMultiplier = hardnessMultiplier;
        this.break_speed = break_speed;
        this.dropBlock = dropBlock;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    protected boolean checkBlock(BlockPos pos) {
        final BlockState state = mob.level().getBlockState(pos);
        final Block b = state.getBlock();

        // exclude unbreakable blocks
        return !b.isPossibleToRespawnInThis(state) && b.defaultDestroyTime() >= 0 && state.getFluidState().isEmpty();
    }

    public void terminate() {
        mob.level().destroyBlockProgress(mob.getId(), mine.bp, -1);
        progress = 0;
        mine.doMining = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return !mob.isNoAi() && mob.isAlive();
    }

    @Override
    public void start() {
        progress = 0;
        hardness = getBlockHardness(mine.bp) * hardnessMultiplier;
        mine.doMining = true;
        miningTicks = 0;
    }

    @Override
    public void stop() {
        // reset all progress and find a path again
        mob.level().destroyBlockProgress(mob.getId(), mine.bp, -1);
        mob.getNavigation().recomputePath();
        mine.doMining = false;
        mine.bp = null;
    }

    @Override
    public void tick() {
        if (!mine.doMining) return;

        // if the target block has been broken by others
        if(mob.level().getBlockState(mine.bp).isAir()) {
            terminate();
            return;
        }
        if (progress >= hardness) {
            mob.level().destroyBlock(mine.bp, dropBlock, mob);
            terminate();
        } else {
            // 0 <= progress <= 10
            mob.level().destroyBlockProgress(mob.getId(), mine.bp, (int)((progress / hardness) * 10));
            mob.stopInPlace();
            mob.getLookControl().setLookAt(mine.bp_vec3);
            progress += break_speed;
            mob.swing(InteractionHand.MAIN_HAND);

            ++ miningTicks;
            if(mob.level() instanceof ServerLevel server) {
                if(miningTicks % 4 == 0) {
                    SoundType sound = server.getBlockState(mine.bp).getSoundType();
                    float pitch = 0.6F + server.random.nextFloat() * 0.4F;
                    server.playSound(null, mine.bp, sound.getHitSound(), SoundSource.BLOCKS, 1, pitch);
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return mine.doMining;
    }

    public float getBlockHardness(BlockPos pos) {
        return mob.level().getBlockState(pos).getBlock().defaultDestroyTime();
    }
}
