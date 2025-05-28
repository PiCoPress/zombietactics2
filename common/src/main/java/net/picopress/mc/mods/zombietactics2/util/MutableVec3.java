package net.picopress.mc.mods.zombietactics2.util;

import net.minecraft.world.phys.Vec3;


// WIP
public class MutableVec3 extends Vec3 {
    public static MutableVec3 toMutableWithCopy(Vec3 vec) {
        return new MutableVec3(vec.x, vec.y, vec.z);
    }

    public static MutableVec3 toMutableWithRef(Vec3 vec) {
        return (MutableVec3)vec;
    }

    public MutableVec3(double x, double y, double z) {
        super(x, y, z);
    }
}
