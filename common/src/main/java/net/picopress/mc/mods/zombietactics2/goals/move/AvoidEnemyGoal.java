package net.picopress.mc.mods.zombietactics2.goals.move;

import static net.picopress.mc.mods.zombietactics2.util.Tactics.Heuristic.needAvoid;
import static net.picopress.mc.mods.zombietactics2.util.Tactics.Heuristic.simulate;

import net.minecraft.world.entity.monster.Zombie;
import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.impl.Plane;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;


public class AvoidEnemyGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    private final Plane plane;

    public AvoidEnemyGoal(PathfinderMob mob, Class<T> entityClassToAvoid, float maxDistance, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, entityClassToAvoid, maxDistance, walkSpeedModifier, sprintSpeedModifier);
        this.plane = (Plane)mob;
    }

    @Override
    public boolean canUse() {
        boolean state = super.canUse();
        if(state && toAvoid != null) {
            if(toAvoid instanceof Zombie) return false;
            boolean t = Config.simulate? !simulate(Zombie.class, mob, toAvoid): needAvoid(mob, toAvoid);
            if(t) {
                plane.zombietactics2$invoke(0);
            }
            return t;
        }
        return false;
    }
}
