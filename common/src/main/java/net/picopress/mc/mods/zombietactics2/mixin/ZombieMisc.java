package net.picopress.mc.mods.zombietactics2.mixin;

import net.minecraft.world.entity.monster.Zombie;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Zombie.class)
public interface ZombieMisc {
    @Accessor(value="inWaterTime")
    int getInWaterTime();
}
