package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;


@Mixin(Monster.class)
public abstract class MonsterMixin extends PathfinderMob {
    protected MonsterMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    // zombie-like mobs can be spawned in sunny day
    @Inject(method="checkMonsterSpawnRules", at=@At(value="RETURN"), cancellable=true)
    private static void checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(level.getDifficulty() != Difficulty.PEACEFUL && (EntitySpawnReason.ignoresLightRequirements(spawnReason) || Monster.isDarkEnoughToSpawn(level, pos, random) || (type == EntityType.ZOMBIE || type == EntityType.HUSK) && Config.spawnUnderSun) && checkMobSpawnRules(type, level, spawnReason, pos, random));
    }

    @ModifyReturnValue(method="getWalkTargetValue", at=@At("RETURN"))
    private float getWalkTargetValue(float original) {
        if((PathfinderMob)this instanceof Zombie) {
            if(Config.spawnUnderSun) return 0;
        }
        return original;
    }
}
