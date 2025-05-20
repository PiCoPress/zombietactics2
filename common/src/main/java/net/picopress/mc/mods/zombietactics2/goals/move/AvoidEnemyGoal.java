package net.picopress.mc.mods.zombietactics2.goals.move;

import net.minecraft.world.entity.Mob;
import net.picopress.mc.mods.zombietactics2.impl.Plane;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;


public class AvoidEnemyGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    private final Plane plane;

    public AvoidEnemyGoal(PathfinderMob mob, Class<T> entityClassToAvoid, float maxDistance, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, entityClassToAvoid, maxDistance, walkSpeedModifier, sprintSpeedModifier);
        this.plane = (Plane)mob;
    }

    @Override
    public boolean canUse() {
        if(mob.getTarget() != null) {
            if(needAvoid(mob, mob.getTarget()) && super.canUse()) {
                // if I think that I can't take down the target, avoid it
                plane.zombietactics2$invoke(0, mob.getTarget());
                return true;
            }
        }
        return false;
    }

    // static method to
    public static boolean needAvoid(Mob mob, LivingEntity target) {
        if(target == null) return false;
        var attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if(attack != null) {
            return mob.getHealth() <= attack.getValue();
        }
        return false;
    }
}
