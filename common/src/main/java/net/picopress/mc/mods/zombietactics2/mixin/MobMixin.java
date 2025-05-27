package net.picopress.mc.mods.zombietactics2.mixin;

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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements Plane {
    @Shadow public abstract boolean canReplaceEqualItem(ItemStack stack, ItemStack stack2);

    @Unique private Mob zombietactics2$self;
    @Unique private AABB zombietactics2$followArea;
    @Unique private BlockPos zombietactics2$prevPos = BlockPos.ZERO;

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // reduce calling of AABB.inflate()
    @Inject(method="<init>", at=@At("RETURN"))
    public void constructor(EntityType<?> entityType, Level level, CallbackInfo ci) {
        zombietactics2$self = (Mob)(LivingEntity)this;
        zombietactics2$prevPos = this.blockPosition();
        zombietactics2$followArea = new AABB(zombietactics2$prevPos).inflate(this.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    @Override
    public @NotNull AABB zombietactics2$getFollowingArea() {
        return zombietactics2$followArea;
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
}
