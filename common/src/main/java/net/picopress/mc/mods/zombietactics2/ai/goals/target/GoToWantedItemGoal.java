package net.picopress.mc.mods.zombietactics2.ai.goals.target;

import net.picopress.mc.mods.zombietactics2.Config;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;


public class GoToWantedItemGoal extends Goal {
    private final Mob mob;
    private final Predicate<ItemStack> predicate;
    private final int range;

    private ItemEntity target;
    private int delay = 0;

    public GoToWantedItemGoal(Mob mob, Predicate<ItemStack> predicate) {
        this(mob, predicate, Config.pickupRange);
    }

    public GoToWantedItemGoal(Mob mob, Predicate<ItemStack> predicate, int range) {
        this.mob = mob;
        this.predicate = predicate;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // disable: pickupRange = 0
        if(Config.pickupRange == 0) return false;
        ++ delay;
        if(delay < 10) return false;
        delay = 0;
        List<ItemEntity> items = mob.level().getEntitiesOfClass(ItemEntity.class,
                new AABB(mob.getX() - range, mob.getY() - range, mob.getZ() - range,
                mob.getX() + range, mob.getY() + range, mob.getZ() + range));
        for (var item: items) {
            if (!(item.isInWater() || item.isInLava()) && predicate.test(item.getItem())) {
                mob.getNavigation().moveTo(item, mob.getTarget() != null && mob.getTarget().isAlive()? Config.aggressiveSpeed: 1);
                target = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !(mob.getNavigation().isDone() || target.isRemoved());
    }
}
