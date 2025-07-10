package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.attachments.FindTargetType;
import net.picopress.mc.mods.zombietactics2.ai.goals.target.FindAllTargetsGoal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    @Unique private static int zombie_tactics$duration = 0;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    // garbage
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        // run if FindTargetType is INTENSIVE
        if(Config.findTargetType == FindTargetType.INTENSIVE) {
            ++ zombie_tactics$duration;
            if(zombie_tactics$duration > 22) {
                zombie_tactics$duration = 0;
                List<LivingEntity> dead = new ArrayList<>();
                for(LivingEntity x: FindAllTargetsGoal.cache_path.keySet()) {
                    if(!x.isAlive()) {
                        dead.add(x);
                    }
                }
                dead.forEach(FindAllTargetsGoal.cache_path::remove);
            }
        }
    }
}
