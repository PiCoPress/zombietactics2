package net.picopress.mc.mods.zombietactics2.impl;

import net.picopress.mc.mods.zombietactics2.attachments.MiningData;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Set;


// for the stubs
public interface Plane {
    MiningData zombietactics2$getMiningData();
    AABB zombietactics2$getFollowingArea();

    /**
     * it returns ticks a zombie climbed
     */
    int zombietactics2$getClimbCount();
    int zombietactics2$getThreshold();
    boolean zombietactics2$isDigging();

    /**
     * propagates a target to other zombies
     */
    boolean zombietactics2$shouldAlert();
    boolean zombietactics2$breakingDoor();
    boolean zombietactics2$floating();
    void zombietactics2$setThreshold(int threshold);
    void zombietactics2$setInterrupt(boolean interrupt);
    void zombietactics2$setAlert(boolean alert);

    /**
     * There methods are used for manage global variables
     */
    List<Pair<Class<? extends LivingEntity>, Integer>> zombietactics2$getTargetPriority();
    Set<Class<? extends LivingEntity>> zombietactics2$getTargetClass();
}
