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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Plane {
    @Unique private int zombietactics2$climbedCount = 0;
    @Unique private boolean zombietactics2$isClimbing = false;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method="remove", at=@At("TAIL"))
    public void remove(RemovalReason reason, CallbackInfo ci) {
        if((Entity)this instanceof Zombie z) {
            // decrement the threshold
            Plane plane = (Plane)z;
            if(z.isPersistenceRequired()) {
                plane.zombietactics2$setThreshold(plane.zombietactics2$getThreshold() - 1);
            }
            // reset the mining progress
            // procedure:
            // die -> remove(=killed)
            // despawn/transform() -> remove(=discarded)
            if(plane.zombietactics2$isDigging())
                this.level().destroyBlockProgress(this.getId(), plane.zombietactics2$getMiningData().bp, -1);
        }
    }

    @Override
    public int zombietactics2$getClimbCount() {
        return zombietactics2$climbedCount;
    }

    @Inject(method="push", at=@At("HEAD"))
    public void push(Entity entity, CallbackInfo ci) {
        if((Entity)this instanceof Zombie z) {
            Plane plane = (Plane)z;
            if(!plane.zombietactics2$breakingDoor() && Config.zombiesClimbing && entity instanceof Zombie &&
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

    @Inject(method="getFlyingSpeed", at=@At("RETURN"), cancellable=true)
    public void getFlyingSpeed(CallbackInfoReturnable<Float> cir) {
        if((Entity)this instanceof Zombie z) {
            AttributeInstance fly = z.getAttribute(Attributes.FLYING_SPEED);
            if(fly != null) cir.setReturnValue((float)fly.getValue());
        }
    }
}
