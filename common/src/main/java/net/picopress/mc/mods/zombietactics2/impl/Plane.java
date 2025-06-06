package net.picopress.mc.mods.zombietactics2.impl;

import net.minecraft.world.phys.AABB;
import net.picopress.mc.mods.zombietactics2.attachments.MiningData;


// for the stubs
public interface Plane {
    MiningData zombietactics2$getMiningData();
    AABB zombietactics2$getFollowingArea();
    int zombietactics2$getClimbCount();
    int zombietactics2$getThreshold();
    boolean zombietactics2$isDigging();
    boolean zombietactics2$shouldAlert();
    boolean zombietactics2$breakingDoor();
    boolean zombietactics2$floating();
    void zombietactics2$setThreshold(int threshold);
    void zombietactics2$setInterrupt(boolean interrupt);
    void zombietactics2$setAlert(boolean alert);
}
