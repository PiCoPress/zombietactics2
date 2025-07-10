package net.picopress.mc.mods.zombietactics2.ai.goals.target;

import net.picopress.mc.mods.zombietactics2.util.Tactics;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;


public class DamagedByGoal extends HurtByTargetGoal {
    public boolean interrupt = false;
    public DamagedByGoal(PathfinderMob mob, Class<?>... toIgnoreDamage) {
        super(mob, toIgnoreDamage);
    }

    @Override
    public boolean canContinueToUse() {
        // it should be stopped when avoiding
        return super.canContinueToUse() && !interrupt;
    }

    @Override
    public void stop() {
        LivingEntity tmp = mob.getTarget();
        if(Tactics.Heuristic.needAvoid(mob, tmp)) super.stop();
        interrupt = false;
    }
}
