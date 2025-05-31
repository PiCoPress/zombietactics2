package net.picopress.mc.mods.zombietactics2.fabric.mixin;

import net.picopress.mc.mods.zombietactics2.fabric.FabricConfig;

import eu.midnightdust.lib.config.MidnightConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


//@Mixin(MidnightConfig.class)
public abstract class MidNightMixin {
    //@Inject(method="write", at=@At("TAIL"))
    private static void write(String id, CallbackInfo ci) {
        FabricConfig.updateConfig();
    }
}
