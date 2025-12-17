package net.picopress.mc.mods.zombietactics2.mixin;

import net.minecraft.world.entity.monster.zombie.Zombie;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Zombie.class)
public interface ZombieMisc {
    @Accessor
    int getInWaterTime();

    @Accessor(value="inWaterTime")
    void setInWaterTime(int time);
}
