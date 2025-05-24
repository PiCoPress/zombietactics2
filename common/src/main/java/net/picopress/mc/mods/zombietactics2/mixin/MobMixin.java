package net.picopress.mc.mods.zombietactics2.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.picopress.mc.mods.zombietactics2.util.Tactics;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Shadow public abstract boolean canReplaceEqualItem(ItemStack stack, ItemStack stack2);

    @Unique private Mob zombietactics2$self;

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method="<init>", at=@At("RETURN"))
    public void constructor(EntityType<?> entityType, Level level, CallbackInfo ci) {
        zombietactics2$self = (Mob)(LivingEntity)this;
    }

    /**
     * @author PICOPress
     * @reason there is a problem to take the better item
     */
    @Inject(method="canReplaceCurrentItem", at=@At("HEAD"), cancellable=true)
    public void canReplaceCurrentItem(ItemStack candidate, ItemStack existing, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if(zombietactics2$self instanceof Zombie) {
            if(existing.isEmpty()) cir.setReturnValue(true);
            if(candidate.getItem() instanceof ProjectileItem && existing.getItem() instanceof ProjectileItem) {
                cir.setReturnValue(this.canReplaceEqualItem(candidate, existing));
            } else {
                cir.setReturnValue(Tactics.ItemUtil.isBetter(zombietactics2$self, candidate));
            }
            // only for zombies
            cir.cancel();
        }
    }
}
