package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.impl.Plane;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Plane {
    @Unique private Zombie zombietactics2$zombie;
    @Unique private Plane zombietactics2$plane;
    @Unique private int zombietactics2$climbedCount = 0;
    @Unique private boolean zombietactics2$isClimbing = false;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method="<init>", at=@At("TAIL"))
    public void constructor(EntityType<? extends LivingEntity> entityType, Level level, CallbackInfo ci) {
        if((Entity)this instanceof Zombie z) {
            // initialize the plane and zombie
            zombietactics2$plane = (Plane)z;
            zombietactics2$zombie = z;
        }
    }

    @Inject(method="remove", at=@At("TAIL"))
    public void remove(RemovalReason reason, CallbackInfo ci) {
        if(zombietactics2$plane != null) {
            // decrement the threshold
            if(zombietactics2$zombie.isPersistenceRequired()) {
                zombietactics2$plane.zombietactics2$setThreshold(zombietactics2$plane.zombietactics2$getThreshold() - 1);
            }
            // reset the mining progress
            // procedure:
            // die -> remove(=killed)
            // despawn/transform() -> remove(=discarded)
            if(zombietactics2$plane.zombietactics2$isDigging())
                this.level().destroyBlockProgress(this.getId(), zombietactics2$plane.zombietactics2$getMiningData().bp, -1);
        }
    }

    @Override
    public int zombietactics2$getClimbCount() {
        return zombietactics2$climbedCount;
    }

    @Inject(method="push", at=@At("HEAD"))
    public void push(Entity entity, CallbackInfo ci) {
        if(zombietactics2$plane != null) {
            if(! zombietactics2$plane.zombietactics2$breakingDoor() && Config.zombiesClimbing && entity instanceof Zombie &&
                    (horizontalCollision || Config.hyperClimbing)) {
                if(zombietactics2$climbedCount < Config.climbLimitTicks) {
                    final Vec3 v = getDeltaMovement();
                    // climb with random error
                    if(Config.randomlyClimb)
                        setDeltaMovement(v.x + (this.getRandom().nextDouble() - 0.5) / 64,
                                Config.climbingSpeed, v.z + (this.getRandom().nextDouble() - 0.5) / 64);
                    else setDeltaMovement(v.x, Config.climbingSpeed, v.z);
                    zombietactics2$isClimbing = true;
                    ++ zombietactics2$climbedCount;
                }
            }
        }
    }

    @Inject(method="checkFallDamage", at=@At("HEAD"))
    public void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
        if((Entity)this instanceof Zombie) {
            if(zombietactics2$isClimbing && onGround) {
                fallDistance = 0;
                zombietactics2$isClimbing = false;
                zombietactics2$climbedCount = 0;
            }
        }
    }

    @ModifyReturnValue(method="getFlyingSpeed", at=@At("RETURN"))
    public float getFlyingSpeed(float original) {
        if(zombietactics2$plane != null) {
            AttributeInstance fly = zombietactics2$zombie.getAttribute(Attributes.FLYING_SPEED);
            if(fly != null) return (float)fly.getValue();
        }
        return original;
    }

    // I think it can control the falling speed in the water
    @WrapMethod(method="getFluidFallingAdjustedMovement")
    public Vec3 getFluidFallingAdjustedMovement(double gravity, boolean isFalling, Vec3 deltaMovement, Operation<Vec3> original) {
        if(zombietactics2$plane != null) {
            if(!zombietactics2$floating()) {
                if(gravity != 0 && !this.isSprinting()) {
                    // deltaMovement(n + 1) = deltaMovement(n) - gravity / (8 * (0.5) ^ swimSpeed + 1)
                    double d = deltaMovement.y - gravity / (1 + 8 * Math.pow(0.5, Config.swimSpeed));
                    if(isFalling && Math.abs(deltaMovement.y - 0.005) >= 0.003 && Math.abs(d) < 0.003) {
                        d = -0.003;
                    }
                    return new Vec3(deltaMovement.x, d, deltaMovement.z);
                }
                return deltaMovement;
            }
        }
        return original.call(gravity, isFalling, deltaMovement);
    }
}
