package net.picopress.mc.mods.zombietactics2.impl;


import net.minecraft.world.phys.AABB;

// for the stubs
public interface Plane {
    AABB zombietactics2$getFollowingArea();
    int zombietactics2$getClimbCount();
    boolean zombietactics2$isDigging();
    boolean zombietactics2$shouldAlert();
    boolean zombietactics2$isBreakingDoor();
    void zombietactics2$setInterrupt(boolean interrupt);
    void zombietactics2$setAlert(boolean alert);
}
