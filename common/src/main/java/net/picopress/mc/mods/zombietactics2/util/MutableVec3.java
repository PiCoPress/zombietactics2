package net.picopress.mc.mods.zombietactics2.util;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;


@SuppressWarnings("unused")
public class MutableVec3 extends Vec3 {
    public double x;
    public double y;
    public double z;

    public static MutableVec3 toMutableWithCopy(Vec3 vec) {
        return new MutableVec3(vec.x, vec.y, vec.z);
    }
    public static MutableVec3 toMutableWithRef(Vec3 vec) {
        return (MutableVec3)vec;
    }

    public MutableVec3(double x, double y, double z) {
        super(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutableVec3() {
        this(0, 0, 0);
    }

    @Override
    public @NotNull MutableVec3 add(Vec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    @Override
    public @NotNull MutableVec3 add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public @NotNull MutableVec3 subtract(Vec3 vec) {
        return subtract(vec.x, vec.y, vec.z);
    }

    @Override
    public @NotNull MutableVec3 subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @Override
    public @NotNull MutableVec3 scale(double scale) {
        x *= scale;
        y *= scale;
        z *= scale;
        return this;
    }

    @Override
    public @NotNull MutableVec3 multiply(Vec3 vec) {
        return multiply(vec.x, vec.y, vec.z);
    }

    @Override
    public @NotNull MutableVec3 multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    @Override
    public @NotNull MutableVec3 normalize() {
        double length = length();
        if (length < 1.0E-4) {
            return this;
        }
        return scale(1.0 / length);
    }

    public MutableVec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public MutableVec3 set(Vec3 vec) {
        return set(vec.x, vec.y, vec.z);
    }

    public Vec3 immutable() {
        return new Vec3(x, y, z);
    }
}
