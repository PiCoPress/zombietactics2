package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;


@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin extends Zombie {
    public ZombieVillagerMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    // this is to make the zombie villager convert in water
    @ModifyReturnValue(method="convertsInWater", at=@At("RETURN"))
    public boolean convertsInWater(boolean original) {
        return Config.convertZombieVillager;
    }
}
