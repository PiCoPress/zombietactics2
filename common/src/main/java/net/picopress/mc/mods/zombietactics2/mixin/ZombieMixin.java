package net.picopress.mc.mods.zombietactics2.mixin;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.ai.path.navigation.AmphibiousNavigation;
import net.picopress.mc.mods.zombietactics2.attachments.MiningData;
import net.picopress.mc.mods.zombietactics2.ai.goals.mining.DestroyBlockGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.mining.MonsterBreakBlockGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.move.AvoidEnemyGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.target.DamagedByGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.target.GoToWantedItemGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.target.FindAllTargetsGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.move.SelectiveFloatGoal;
import net.picopress.mc.mods.zombietactics2.ai.goals.move.ZombieGoal;
import net.picopress.mc.mods.zombietactics2.impl.GoalPlane;
import net.picopress.mc.mods.zombietactics2.impl.Plane;

import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import oshi.util.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;


@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements Plane {
    @Unique private static final List<Pair<Class<? extends LivingEntity>, Integer>> zombietactics2$target_priority = new ArrayList<>();
    @Unique private static final Set<Class<? extends LivingEntity>> zombietactics2$target_class = new HashSet<>();

    @Unique @Nullable private MiningData zombietactics2$miningData;
    @Unique private BreakDoorGoal zombietactics2$door_goal;
    @Unique private DamagedByGoal zombietactics2$damaged_by;
    @Unique private SelectiveFloatGoal zombietactics2$selective_float;
    @Unique private boolean zombietactics2$glowing = false;
    @Unique private boolean zombietactics2$flying = false;
    @Unique private boolean zombietactics2$target_alert = false;

    @Final @Shadow private static Predicate<Difficulty> DOOR_BREAKING_PREDICATE;
    @Shadow public abstract boolean canBreakDoors(); // This just makes path finding

    @Shadow private int inWaterTime;

    public ZombieMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean zombietactics2$floating() {
        if(zombietactics2$selective_float != null) return zombietactics2$selective_float.floating;
        return false;
    }

    @Override
    public boolean zombietactics2$isDigging() {
        if(zombietactics2$miningData == null) return false;
        return zombietactics2$miningData.doMining;
    }

    @Override
    public boolean zombietactics2$breakingDoor() {
        if(zombietactics2$door_goal == null) return false;
        // if the zombie is breaking a door, it is breaking
        return ((GoalPlane)zombietactics2$door_goal).zombietactics2$isBreakingDoor();
    }

    @Override
    public boolean zombietactics2$shouldAlert() {
        return zombietactics2$target_alert;
    }

    @Override
    public void zombietactics2$setInterrupt(boolean b) {
        if(zombietactics2$damaged_by != null) {
            zombietactics2$damaged_by.interrupt = true;
        }
    }

    @Override
    public void zombietactics2$setAlert(boolean b) {
        zombietactics2$target_alert = b;
    }

    @Override
    public MiningData zombietactics2$getMiningData() {
        return zombietactics2$miningData;
    }

    @ModifyReturnValue(method="createAttributes", at=@At("RETURN"))
    private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
        // if a zombie cannot fly, it is just nothing
        return original.add(Attributes.FLYING_SPEED, Config.flySpeed);
    }

    // I do not want to see that zombies burn
    @ModifyReturnValue(method="isSunSensitive", at=@At("RETURN"))
    public boolean isSunSensitive(boolean original) {
        return Config.sunSensitive && original;
    }

    @Inject(method="wantsToPickUp", at=@At("RETURN"), cancellable=true)
    public void wantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // selecting a weapon
        // cir.setReturnValue(Tactics.Item.isBetter(this, stack));
        // ??
        cir.setReturnValue(this.canReplaceCurrentItem(stack, this.getItemBySlot(this.getEquipmentSlotForItem(stack))));
    }

    @Inject(method="hurt", at=@At("HEAD"), cancellable=true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // why are avoid_list's elements removed even if I didn't remove?
        // fucking hell
        // zombies do strange behavior

        // allows kill by commands or creative players
        if(Config.neverDie && amount >= this.getHealth() && !source.is(DamageTypes.GENERIC_KILL) && !source.isCreativePlayer()) {
            // just make them unkillable things
            var s = this.level().getServer();this.goDownInWater();
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
        if(Config.canFly) { // I can fly
            this.zombietactics2$flying = true;
            this.moveControl = new FlyingMoveControl(this, 360, true);
            this.navigation = new FlyingPathNavigation(this, level);
            Objects.requireNonNull(this.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(Config.flySpeed);
        } else if(Config.canSwim) { // I can swim
            this.navigation = new AmphibiousNavigation(this, level);
            this.navigation.setCanFloat(true);
        }

        Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(Config.followRange);

        // I can see all zombies through blocks
        if(Config.glowZombie) {
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 1, false, false));
        }
        // change default health
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(Config.defaultHealth);
        this.setHealth(Config.defaultHealth);

        // ???!!!??!??!!?!!?
        Objects.requireNonNull(this.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY)).setBaseValue(Config.swimSpeed);
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

        if(Config.waterBreathing) this.inWaterTime = 0; // please don't be drowned

        if(this.getVehicle() instanceof Boat && this.getTarget() != null) {
            this.stopRiding();
        }
    }

    // fixes that doing both mining and attacking
    @Inject(method="doHurtTarget", at=@At("HEAD"))
    public void doHurtTargetHead(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(zombietactics2$miningData != null) zombietactics2$miningData.doMining = false;
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

    /**
     * ZombieMineGoal doesn't use zombie-exclusive things
     * @author PICOPress
     * @reason it's very hard to inject for each parameter
     */
    @Overwrite
    public void addBehaviourGoals() {
        zombietactics2$miningData = new MiningData();
        // inserting a new instance of Pair in HashSet is not a good idea
        if(Config.targetAnimals && !zombietactics2$target_class.contains(Animal.class)) {
            zombietactics2$target_priority.add(new Pair<>(Animal.class, 5));
            zombietactics2$target_priority.add(new Pair<>(AmbientCreature.class, 2));
            zombietactics2$target_class.add(Animal.class);
            zombietactics2$target_class.add(AmbientCreature.class);
        }
        if(Config.mineBlocks) this.goalSelector.addGoal(1, new MonsterBreakBlockGoal<>(this, zombietactics2$miningData));
        if(Config.canFloat) this.goalSelector.addGoal(5, zombietactics2$selective_float = new SelectiveFloatGoal(this));
        if(Config.canFly) this.goalSelector.addGoal(10, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        else this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0));
        if(Config.avoidance) this.goalSelector.addGoal(0, new AvoidEnemyGoal<>(this, Mob.class, 8, 1, Config.aggressiveSpeed));

        this.targetSelector.addGoal(3, new FindAllTargetsGoal(zombietactics2$target_priority, this, false));
        this.goalSelector.addGoal(1, new ZombieGoal((Zombie)(Monster)this, Config.aggressiveSpeed, true));
        this.goalSelector.addGoal(7, new MoveThroughVillageGoal(this, 1.0, false, 4, this::canBreakDoors));
        this.targetSelector.addGoal(1, zombietactics2$damaged_by = (DamagedByGoal)(new DamagedByGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.goalSelector.addGoal(1, zombietactics2$door_goal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE));
        this.goalSelector.addGoal(Config.pickUpPriority, new GoToWantedItemGoal(this, this::wantsToPickUp));
        this.goalSelector.addGoal(6, new DestroyBlockGoal(this, zombietactics2$miningData, Blocks.CHEST, Config.findChest));
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
