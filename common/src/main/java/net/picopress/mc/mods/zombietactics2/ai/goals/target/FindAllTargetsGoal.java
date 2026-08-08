package net.picopress.mc.mods.zombietactics2.ai.goals.target;

import net.picopress.mc.mods.zombietactics2.util.Tactics;
import net.picopress.mc.mods.zombietactics2.impl.Plane;
import net.picopress.mc.mods.zombietactics2.attachments.FindTargetType;
import net.picopress.mc.mods.zombietactics2.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import oshi.util.tuples.Pair;

import java.util.*;


// the new improved target finding goal
public class FindAllTargetsGoal extends TargetGoal {
    public static final Map<LivingEntity, Path> cache_path = new HashMap<>();
    private final List<Pair<Class<? extends LivingEntity>, Integer>> list;
    private final Plane plane;
    private List<LivingEntity> enemies;
    private TargetingConditions targetingConditions;
    @Nullable private final ServerLevel serverLevel;
    private Task current_task;
    private int delay;
    private int finding_index;

    private LivingEntity tmp_target;
    private int attack_delay;
    private boolean should_wait;

    /**
     * @param targets Pairs of class and priority
     */
    public FindAllTargetsGoal(List<Pair<Class<? extends LivingEntity>, Integer>> targets, Mob mob, boolean mustSee) {
        super(mob, mustSee);
        setFlags(EnumSet.of(Flag.TARGET));
        list = targets;
        serverLevel = Tactics.getServerLevel(mob);
        plane = (Plane)mob;
        targetingConditions = TargetingConditions.forCombat().range(Config.followRange).selector(null);
        if(Config.attackInvisible) targetingConditions = targetingConditions.ignoreLineOfSight();

        attack_delay = 0;
        should_wait = false;
    }

    private void doTaskIdle() {
        ++ delay;
        if(Config.findTargetType == FindTargetType.SIMPLE && delay > 4) current_task = Task.SEARCH;

        else if(delay > 6) current_task = Task.SEARCH;
    }

    private boolean isFartherThanMe(LivingEntity peer1, LivingEntity peer2) {
        return peer1 != null && peer2 != null && mob.distanceToSqr(peer1) < mob.distanceToSqr(peer2);
    }

    private void doTaskSearch(@NotNull ServerLevel sl) {
        // simple target finding a target of the specific class per 1 tick
        if(Config.findTargetType == FindTargetType.SIMPLE) {
            LivingEntity target;
            var clazz = list.get(finding_index);
            if (clazz.getA() != Player.class && clazz.getA() != ServerPlayer.class) {
                target = sl.getNearestEntity(clazz.getA(), targetingConditions, mob,
                        mob.getX(), mob.getEyeY(), mob.getZ(), followBox());
            } else {
                target = sl.getNearestPlayer(targetingConditions, mob,
                        mob.getX(), mob.getEyeY(), mob.getZ());
            }
            if(mob.getTarget() == null || isFartherThanMe(target, mob.getTarget())) {
                mob.setTarget(target);
            }
            ++ finding_index;
            finding_index %= list.size();
            current_task = Task.IDLE;
        } else {
            // query targets
            enemies = mob.level().getEntitiesOfClass(LivingEntity.class, followBox(), (t) -> {
                for(var sus: list) {
                    if(sus.getA().isAssignableFrom(t.getClass()) && targetingConditions.test(sl, mob, t)) {
                        return true;
                    }
                }
                return false;
            });
            current_task = Task.PRIORITIZE;
        }
        delay = 0;
    }

    private int dropEnemyIfUnreachable(Path path, int score) {
        if(path != null) {
            score += path.getNodeCount();
            if(!path.canReach()) score *= 128;
        }
        return score;
    }

    private int doTaskPriorLinear(BlockPos my_position, BlockPos delta, LivingEntity enemy, int score) {
        // using linear function
        BlockPos.MutableBlockPos linear_pos = mob.blockPosition().mutable();
        double distance = mob.distanceTo(enemy);

        for(int i = 0; i <= distance; ++ i) {
            double factor = i / distance;

            // delta = my_position - enemy
            // if factor = 1: result should be enemy
            // my_position - 1 * (my_position - enemy) = enemy
            if(!mob.level().getBlockState(linear_pos.set(
                    my_position.getX() - delta.getX() * factor,
                    my_position.getY() - delta.getY() * factor,
                    my_position.getZ() - delta.getZ() * factor)).isAir())
                score += Config.blockCost;
            else ++ score;
        }
        return score;
    }

    private int doTaskPriorIntensive(LivingEntity enemy, int score) {
        Path path = cache_path.get(enemy);
        if(path == null) {
            // use cache to prevent overloading
            path = mob.getNavigation().createPath(enemy, Config.accuracy);
            cache_path.put(enemy, path);
        }
        return dropEnemyIfUnreachable(path, score);
    }

    private int doTaskPriorOverload(LivingEntity enemy, int score) {
        Path path = mob.getNavigation().createPath(enemy, Config.accuracy);
        return dropEnemyIfUnreachable(path, score);
    }

    private void doTaskPrior() {
        BlockPos my_position = mob.blockPosition();
        LivingEntity final_target = null;
        int minimum_cost = Integer.MAX_VALUE;

        // calculate the cost for each of the imposters
        for(var enemy: enemies) {
            BlockPos delta = my_position.subtract(enemy.blockPosition());
            int score = 0;
            int target_key = 0;

            score = switch(Config.findTargetType) {
                case FindTargetType.LINEAR -> doTaskPriorLinear(my_position, delta, enemy, score);
                case FindTargetType.INTENSIVE -> doTaskPriorIntensive(enemy, score);

                // no one can endure this overload
                case FindTargetType.OVERLOAD -> doTaskPriorOverload(enemy, score);
                default -> score;
            };

            // apply priority
            for(var p: list) {
                if(p.getA().isAssignableFrom(enemy.getClass())) break;
                ++ target_key;
            }
            // idx must match the target list unless priorities are invalid
            score *= list.get(target_key).getB();

            // getting crazy
            if(mob.hasLineOfSight(enemy)) score /= 2;
            if(delta.getY() >= -2) score /= 2;
            score *= Tactics.Heuristic.getEnemyPower(enemy);

            // select minimum score
            if(score < minimum_cost) {
                minimum_cost = score;
                final_target = enemy;
            }
        }

        // set target
        if(final_target != null) {
            if(Config.delayToAttack == 0) mob.setTarget(final_target);
            else {
                tmp_target = final_target;
                attack_delay = Config.delayToAttack;
                should_wait = true;
            }
        }
        current_task = Task.IDLE;
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() == null || !mob.getTarget().isAlive() || plane.zombietactics2$shouldAlert();
    }

    @Override
    public void start() {
        finding_index = 0;
        delay = 0;
        current_task = Task.IDLE;
    }

    @Override
    public void tick() {
        // I am server
        if(serverLevel == null) return;

        // Feature requested by JoeSchmoe123
        if(should_wait)
        {
            -- attack_delay;
            if(attack_delay == 0) {
                if(tmp_target.isAlive() &&
                        tmp_target.distanceToSqr(this.mob) <= Config.followRange * Config.followRange) {
                    mob.setTarget(tmp_target);
                }
                should_wait = false;
                tmp_target = null;
            }
            return;
        }

        switch(current_task) {
            case Task.IDLE: doTaskIdle();
            break;

            case Task.SEARCH: doTaskSearch(serverLevel);
            break;

            // distribute loads with tasks, but it is similar to the brain system
            case Task.PRIORITIZE: doTaskPrior();
            break;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || super.canContinueToUse();
    }

    // please update their bounding box
    // don't cache it
    private AABB followBox() {
        return plane.zombietactics2$getFollowingArea();
    }

    // brain rot
    public enum Task {
        SEARCH,
        PRIORITIZE,
        IDLE,
    }
}
