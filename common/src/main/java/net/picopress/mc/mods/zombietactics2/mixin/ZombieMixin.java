package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.goals.mining.DestroyBlockGoal;
import net.picopress.mc.mods.zombietactics2.goals.mining.MonsterBreakBlockGoal;
import net.picopress.mc.mods.zombietactics2.goals.move.AvoidEnemyGoal;
import net.picopress.mc.mods.zombietactics2.goals.target.DamagedByGoal;
import net.picopress.mc.mods.zombietactics2.goals.target.GoToWantedItemGoal;
import net.picopress.mc.mods.zombietactics2.goals.target.FindAllTargetsGoal;
import net.picopress.mc.mods.zombietactics2.goals.move.SelectiveFloatGoal;
import net.picopress.mc.mods.zombietactics2.goals.move.ZombieGoal;
import net.picopress.mc.mods.zombietactics2.impl.Plane;
import net.picopress.mc.mods.zombietactics2.util.Tactics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.NotNull;

import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;


@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements Plane {
    @Unique private static final List<Pair<Class<? extends LivingEntity>, Integer>> zombietactics2$target_priority = new ArrayList<>();
    @Unique private static final Set<Class<? extends LivingEntity>> zombietactics2$target_class = new HashSet<>();
    @Unique private static int zombietactics2$threshold = 0;
    @Unique private MonsterBreakBlockGoal<? extends Monster> zombietactics2$mine_goal;
    @Unique private DamagedByGoal zombietactics2$damaged_by;
    @Unique private BreakDoorGoal zombietactics2$bdg;
    @Unique private int zombietactics2$climbedCount = 0;
    @Unique private boolean zombietactics2$isClimbing = false;
    @Unique private boolean zombietactics2$persistence;
    @Unique private boolean zombietactics2$glowing = false;
    @Unique private boolean zombietactics2$flying = false;

    @Final @Shadow private static Predicate<Difficulty> DOOR_BREAKING_PREDICATE;
    @Shadow private int inWaterTime;
    @Shadow public abstract boolean canBreakDoors(); // This just makes path finding
    @Shadow public abstract void readAdditionalSaveData(CompoundTag compound);

    public ZombieMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    // zombie doesn't take fall damage when climbing
    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        if(zombietactics2$isClimbing && onGround) {
            fallDistance = 0;
            zombietactics2$isClimbing = false;
            zombietactics2$climbedCount = 0;
        }
        super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    protected float getFlyingSpeed() {
        return (float)this.getAttributeValue(Attributes.FLYING_SPEED);
    }

    // Modifying Attack range
    @Override
    protected @NotNull AABB getAttackBoundingBox() {
        Entity entity = this.getVehicle();
        AABB aabb;
        if (entity != null) {
            AABB aabb1 = entity.getBoundingBox();
            AABB aabb2 = this.getBoundingBox();
            aabb = new AABB(Math.min(aabb2.minX, aabb1.minX),
                    aabb2.minY,
                    Math.min(aabb2.minZ, aabb1.minZ),
                    Math.max(aabb2.maxX, aabb1.maxX),
                    aabb2.maxY,
                    Math.max(aabb2.maxZ, aabb1.maxZ));
        } else {
            aabb = this.getBoundingBox();
        }
        return aabb.inflate(Config.attackRange);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 32; // I think, the bigger the number is the better
    }

    @Override
    public int zombietactics2$getInt(int id) {
        // inWaterTime
        if(id == 0) return inWaterTime;
        if(id == 1) return zombietactics2$climbedCount;

        // nothing else
        return 0;
    }

    @Override
    public boolean zombietactics2$getBool(int id, Object ...args) {
        if(id == 0) {
            if(zombietactics2$mine_goal == null) return false;
            return zombietactics2$mine_goal.mine.doMining;
        }
        return false;
    }

    /**
     * @param id 0: set damaged_by.interrupt
     */
    @Override
    public void zombietactics2$invoke(int id, Object ...args) {
        if(id == 0) {
            if(zombietactics2$damaged_by != null) {
                zombietactics2$damaged_by.interrupt = true;
            }
        }
    }

    @Override
    public double getAttributeValue(Holder<Attribute> attribute) {
        // change follow range
        if(attribute == Attributes.FOLLOW_RANGE) return Config.followRange;
        return super.getAttributeValue(attribute);
    }

    @Override
    public boolean wantsToPickUp(@NotNull ItemStack stack) {
        // selecting a weapon
        return Tactics.ItemUtil.isBetter(this, stack);
    }

    @Override
    public boolean isPersistenceRequired() {
        return zombietactics2$persistence || super.isPersistenceRequired();
    }

    // For climbing
    @Override
    public void push(@NotNull Entity entity) {
        if(zombietactics2$bdg != null && Config.zombiesClimbing && entity instanceof Zombie &&
                (horizontalCollision || Config.hyperClimbing) && !((Plane)zombietactics2$bdg).zombietactics2$getBool(0)) {
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
        super.push(entity);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        if(Config.noDespawn) return false;
        return super.removeWhenFarAway(d);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        -- zombietactics2$threshold; // decrease
        // reset the mining progress
        // procedure:
        // die -> remove(=killed)
        // despawn/transform() -> remove(=discarded)
        if(zombietactics2$mine_goal != null && zombietactics2$mine_goal.mine.doMining)
            this.level().destroyBlockProgress(this.getId(), zombietactics2$mine_goal.mine.bp, -1);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        // unlock darkness
        return Config.spawnUnderSun? 0: super.getWalkTargetValue(pos, level);
    }

    @Inject(method="createAttributes", at=@At("RETURN"), cancellable=true)
    private static void createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        // if a zombie cannot fly, it is just nothing
        cir.setReturnValue(cir.getReturnValue().add(Attributes.FLYING_SPEED, Config.flySpeed));
    }

    @Inject(method="hurt", at=@At("HEAD"), cancellable=true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // why are avoid_list's elements removed even if I didn't remove?
        // fucking hell
        // zombies do strange behavior

        // allows kill by commands or creative players
        if(Config.neverDie && amount >= this.getHealth() && !source.is(DamageTypes.GENERIC_KILL) && !source.isCreativePlayer()) {
            // just make them unkillable things
            var s = this.level().getServer();
            if(s != null) {
                var sl = s.getLevel(this.level().dimension());
                if(sl != null) sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        this.getX(), this.getY() + 1.5, this.getZ(),
                        16, 0.5, 0.5, 0.5, 0.3);
                this.level().playSound(this, this.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1, 1);
            }
            cir.setReturnValue(false);
            return;
        }
        Entity who = source.getEntity();
        // new target list
        if(who instanceof PathfinderMob mob && !(who instanceof Monster) && !zombietactics2$target_class.contains(who.getClass())) {
            zombietactics2$target_priority.add(new Pair<>(mob.getClass(), 3));
            zombietactics2$target_class.add(mob.getClass());
        }
    }

    @Inject(method="<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at=@At("TAIL"))
    public void constructor(EntityType<? extends Zombie> entityType, Level level, CallbackInfo ci) {
        double tmp = this.level().random.nextDouble();
        zombietactics2$persistence = tmp <= Config.persistenceChance;
        if(zombietactics2$persistence && zombietactics2$threshold < Config.maxThreshold) {
            ++ zombietactics2$threshold;
        } else zombietactics2$persistence = false;

        if(zombietactics2$persistence) this.setPersistenceRequired(); // I'm persistent
        if(Config.canFly) { // I can fly
            this.zombietactics2$flying = true;
            this.moveControl = new FlyingMoveControl(this, 360, true);
            this.navigation = new FlyingPathNavigation(this, level);
            Objects.requireNonNull(this.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(Config.flySpeed);
        }

        // I can see all zombies through blocks
        if(Config.glowZombie) {
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 1, false, false));
        }
        // change default health
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(Config.defaultHealth);
        this.setHealth(Config.defaultHealth);
    }

    @Inject(method="tick", at=@At("TAIL"))
    public void tick(CallbackInfo ci) {
        if(!this.canPickUpLoot()) this.setCanPickUpLoot(true);

        if(Config.canFly) this.fallDistance = 0;
        else if(!zombietactics2$flying) this.setNoGravity(false);

        // for debugging
        if(Config.showDeltaMovement) {
            this.setCustomName(Component.literal(String.valueOf(this.getDeltaMovement().length())));
            this.setCustomNameVisible(true);
        }

        if(Config.noIdle) this.setNoActionTime(0);

        // I can see all zombies through blocks
        // hasEffect uses Map that computes hash algorithm to find a key
        // in the function "tick"
        if(Config.glowZombie && !zombietactics2$glowing) {
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 1, false, false));
            zombietactics2$glowing = true;
        } else if(!Config.glowZombie && zombietactics2$glowing) {
            this.removeEffect(MobEffects.GLOWING);
            zombietactics2$glowing = false;
        }
    }

    // fixes that doing both mining and attacking
    @Inject(method="doHurtTarget", at=@At("HEAD"))
    public void doHurtTargetHead(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(zombietactics2$mine_goal != null) zombietactics2$mine_goal.mine.doMining = false;
    }

    // Healing zombie
    @Inject(method="doHurtTarget", at=@At("TAIL"))
    public void doHurtTargetTail(Entity ent, CallbackInfoReturnable<Boolean> ci) {
        if(ent instanceof LivingEntity) {
            if(this.getHealth() <= this.getMaxHealth())
                this.heal((float)Config.healAmount);
        }
        // reset invulnerable time
        if(Config.noMercy) ent.invulnerableTime = 0;
    }

    // I do not want to see that zombies burn
    @Inject(method="isSunSensitive", at=@At("RETURN"), cancellable=true)
    public void isSunSensitive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Config.sunSensitive && cir.getReturnValue());
    }

    /**
     * ZombieMineGoal doesn't use zombie-exclusive things
     * @author PICOPress
     * @reason it's very hard to inject for each parameter
     */
    @Overwrite
    public void addBehaviourGoals() {
        // inserting a new instance of Pair in HashSet is not a good idea
        if(Config.targetAnimals && !zombietactics2$target_class.contains(Animal.class)) {
            zombietactics2$target_priority.add(new Pair<>(Animal.class, 5));
            zombietactics2$target_class.add(Animal.class);
        }
        if(Config.mineBlocks) this.goalSelector.addGoal(1, zombietactics2$mine_goal = new MonsterBreakBlockGoal<>(this));
        if(Config.canFloat) this.goalSelector.addGoal(5, new SelectiveFloatGoal(this));
        if(Config.canFly) this.goalSelector.addGoal(10, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        else this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0));
        if(Config.breakChest) this.goalSelector.addGoal(6, new DestroyBlockGoal(this, Blocks.CHEST, Config.findChest));
        if(Config.avoidance) this.goalSelector.addGoal(0, new AvoidEnemyGoal<>(this, Mob.class, 8, 1, Config.aggressiveSpeed));

        this.targetSelector.addGoal(3, new FindAllTargetsGoal(zombietactics2$target_priority, this, false));
        this.goalSelector.addGoal(1, new ZombieGoal((Zombie)(Monster)this, Config.aggressiveSpeed, true));
        this.goalSelector.addGoal(7, new MoveThroughVillageGoal(this, 1.0, false, 4, this::canBreakDoors));
        this.targetSelector.addGoal(1, zombietactics2$damaged_by = (DamagedByGoal)(new DamagedByGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.goalSelector.addGoal(1, zombietactics2$bdg = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE));
        this.goalSelector.addGoal(5, new GoToWantedItemGoal(this, this::wantsToPickUp));
    }

    static {
        zombietactics2$target_priority.add(new Pair<>(Player.class, 2));
        zombietactics2$target_priority.add(new Pair<>(AbstractVillager.class, 3));
        zombietactics2$target_priority.add(new Pair<>(IronGolem.class, 3));
        zombietactics2$target_priority.add(new Pair<>(Turtle.class, 3));

        zombietactics2$target_class.add(Player.class);
        zombietactics2$target_class.add(AbstractVillager.class);
        zombietactics2$target_class.add(IronGolem.class);
        zombietactics2$target_class.add(Turtle.class);
    }
}
