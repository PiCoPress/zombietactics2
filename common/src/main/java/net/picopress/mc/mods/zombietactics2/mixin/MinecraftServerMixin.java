package net.picopress.mc.mods.zombietactics2.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.picopress.mc.mods.zombietactics2.impl.Plane;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.DataFixer;

import oshi.util.tuples.Pair;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Plane {
    @Unique private final List<Pair<Class<? extends LivingEntity>, Integer>> zombietactics2$target_priority = new ArrayList<>();
    @Unique private final Set<Class<? extends LivingEntity>> zombietactics2$target_class = new HashSet<>();

    protected MinecraftServerMixin() {}

    @Inject(method="<init>", at=@At("TAIL"))
    public void constructor(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource,
                            PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer fixerUpper,
                            Services services, LevelLoadListener levelLoadListener, CallbackInfo ci) {
        zombietactics2$target_priority.add(new Pair<>(Player.class, 2));
        zombietactics2$target_priority.add(new Pair<>(AbstractVillager.class, 3));
        zombietactics2$target_priority.add(new Pair<>(IronGolem.class, 3));
        zombietactics2$target_priority.add(new Pair<>(Turtle.class, 3));
        zombietactics2$target_class.add(Player.class);
        zombietactics2$target_class.add(AbstractVillager.class);
        zombietactics2$target_class.add(IronGolem.class);
        zombietactics2$target_class.add(Turtle.class);
    }


    @Override
    public List<Pair<Class<? extends LivingEntity>, Integer>> zombietactics2$getTargetPriority() {
        return zombietactics2$target_priority;
    }

    @Override
    public Set<Class<? extends LivingEntity>> zombietactics2$getTargetClass() {
        return zombietactics2$target_class;
    }
}
