package net.picopress.mc.mods.zombietactics2.goals.move;

import net.picopress.mc.mods.zombietactics2.mixin.ZombieMisc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

import org.jetbrains.annotations.NotNull;


public class SelectiveFloatGoal extends FloatGoal {
    private final Mob mob;
    private final ZombieMisc misc;
    private boolean needBreathe = false;

    public SelectiveFloatGoal(Mob mob) {
        super(mob);
        this.mob = mob;
        this.misc = (ZombieMisc)mob;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if(target == null) return super.canUse();
        // selectively float
        // and, zombies want to breathe, but not want to be drowned
        if(misc.getInWaterTime() > calculateBreath(target)) needBreathe = true;
        return super.canUse() && (!target.isInWater() || mob.getBlockY() - target.getBlockY() < 1 || needBreathe);
    }

    @Override
    public boolean canContinueToUse() {
        if(misc.getInWaterTime() < 15) needBreathe = false;
        return super.canUse() || needBreathe;
    }

    // calculate the cost of swimming
    private int calculateBreath(@NotNull LivingEntity target) {
        return 600 - (mob.getBlockY() - target.getBlockY()) * 75;
    }
}
