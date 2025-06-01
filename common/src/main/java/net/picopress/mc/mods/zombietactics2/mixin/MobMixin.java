package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.impl.Plane;
import net.picopress.mc.mods.zombietactics2.util.Tactics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;


@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements Plane {
    @Unique private Mob zombietactics2$self;
    @Unique private AABB zombietactics2$followArea;
    @Unique private BlockPos zombietactics2$prevPos = BlockPos.ZERO;
    @Unique private static int zombietactics2$threshold = 0;

    @Shadow @Final private static double DEFAULT_ATTACK_REACH;
    @Shadow private boolean persistenceRequired;
    @Shadow public abstract boolean canReplaceEqualItem(ItemStack stack, ItemStack stack2);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull AABB zombietactics2$getFollowingArea() {
        return zombietactics2$followArea;
    }

    @Override
    public int zombietactics2$getThreshold() {
        return zombietactics2$threshold;
    }

    @Override
    public void zombietactics2$setThreshold(int threshold) {
        zombietactics2$threshold = threshold;
    }

    // reduce calling of AABB.inflate()
    @Inject(method="<init>", at=@At("RETURN"))
    public void constructor(EntityType<?> entityType, Level level, CallbackInfo ci) {
        zombietactics2$self = (Mob)(LivingEntity)this;
        zombietactics2$prevPos = this.blockPosition();
        zombietactics2$followArea = new AABB(zombietactics2$prevPos).inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE));

        if(zombietactics2$self instanceof Zombie && zombietactics2$threshold < Config.maxThreshold) {
            // for zombies, we need to set the threshold
            double tmp = this.level().random.nextDouble();
            persistenceRequired = tmp <= Config.persistenceChance;
            if(persistenceRequired) ++ zombietactics2$threshold;
        }
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        if(!zombietactics2$prevPos.equals(this.blockPosition())) {
            zombietactics2$prevPos = this.blockPosition();
            zombietactics2$followArea = new AABB(zombietactics2$prevPos).inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE));
        }
    }

    @Inject(method="canReplaceCurrentItem", at=@At("HEAD"), cancellable=true)
    public void canReplaceCurrentItem(ItemStack candidate, ItemStack existing, CallbackInfoReturnable<Boolean> cir) {
        if(zombietactics2$self instanceof Zombie) {
            if(existing.isEmpty()) cir.setReturnValue(true);
            else if(candidate.getItem() instanceof ProjectileItem && existing.getItem() instanceof ProjectileItem) {
                cir.setReturnValue(this.canReplaceEqualItem(candidate, existing));
            } else {
                cir.setReturnValue(Tactics.ItemUtil.isBetter(zombietactics2$self, candidate));
            }
            // only for zombies
            cir.cancel();
        }
    }

    @ModifyExpressionValue(method="getMaxSpawnClusterSize", at=@At(value="CONSTANT", args="intValue=4"))
    public int cluster(int original) {
        // increase zombie spawn size to 6
        return zombietactics2$self instanceof Zombie? 32: original;
    }

    @ModifyReturnValue(method="removeWhenFarAway", at=@At("RETURN"))
    public boolean removeWhenFarAway(boolean original) {
        if(zombietactics2$self instanceof Zombie) {
            return Config.noDespawn;
        }
        return original;
    }

    // modifying attack range
    @ModifyExpressionValue(method="getAttackBoundingBox", at=@At(value="INVOKE", target="Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
    public AABB getAttackBoundingBox(AABB original) {
        // increase the attack range of zombies
        if(zombietactics2$self instanceof Zombie) {
            return original.inflate(Config.attackRange);
        }
        return original.inflate(DEFAULT_ATTACK_REACH, 0, DEFAULT_ATTACK_REACH);
    }
}
