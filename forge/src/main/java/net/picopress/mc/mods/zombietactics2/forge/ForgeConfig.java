package net.picopress.mc.mods.zombietactics2.forge;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.attachments.FindTargetType;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.apache.commons.lang3.tuple.Pair;


@Mod.EventBusSubscriber(modid=Main.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ForgeConfig {
    private static final Pair<MCSBuilder, ForgeConfigSpec> BUILDER = new ForgeConfigSpec.Builder().configure(MCSBuilder::new);
    private static ForgeConfigSpec.BooleanValue TARGET_ANIMALS;
    private static ForgeConfigSpec.BooleanValue ATTACK_INVISIBLE;
    private static ForgeConfigSpec.BooleanValue MINE_BLOCKS;
    private static ForgeConfigSpec.DoubleValue MIN_DISTANCE;
    private static ForgeConfigSpec.DoubleValue MAX_DISTANCE;
    private static ForgeConfigSpec.BooleanValue DROP_BROKEN_BLOCKS;
    private static ForgeConfigSpec.BooleanValue ZOMBIE_CLIMBING;
    private static ForgeConfigSpec.DoubleValue CLIMBING_SPEED;
    private static ForgeConfigSpec.DoubleValue MINING_SPEED;
    private static ForgeConfigSpec.DoubleValue MAX_HARDNESS;
    private static ForgeConfigSpec.DoubleValue HARDNESS_MULTIPLIER;
    private static ForgeConfigSpec.DoubleValue HEAL_AMOUNT;
    private static ForgeConfigSpec.IntValue ATTACK_COOLDOWN;
    private static ForgeConfigSpec.DoubleValue AGGRESSIVE_SPEED;
    private static ForgeConfigSpec.BooleanValue SUN_SENSITIVE;
    private static ForgeConfigSpec.BooleanValue NO_MERCY;
    private static ForgeConfigSpec.DoubleValue ATTACK_RANGE;
    private static ForgeConfigSpec.DoubleValue PERSISTENCE_CHANCE;
    private static ForgeConfigSpec.IntValue MAX_THRESHOLD;
    private static ForgeConfigSpec.IntValue BLOCK_COST;
    private static ForgeConfigSpec.BooleanValue CAN_FLOAT;
    private static ForgeConfigSpec.IntValue CLIMB_LIMIT_TICKS;
    private static ForgeConfigSpec.DoubleValue JUMP_ACCELERATION;
    private static ForgeConfigSpec.BooleanValue HYPER_CLIMBING;
    private static ForgeConfigSpec.BooleanValue JUMP_BLOCK;
    private static ForgeConfigSpec.IntValue FOLLOW_RANGE;
    private static ForgeConfigSpec.EnumValue<FindTargetType> TARGET_TYPE;
    private static ForgeConfigSpec.BooleanValue SPAWN_UNDER_SUN;
    private static ForgeConfigSpec.BooleanValue CAN_FLY;
    private static ForgeConfigSpec.DoubleValue FLY_SPEED;
    private static ForgeConfigSpec.IntValue PATH_ACCURACY;
    private static ForgeConfigSpec.IntValue PICKUP_RANGE;
    private static ForgeConfigSpec.BooleanValue RANDOM_CLIMB;
    private static ForgeConfigSpec.BooleanValue STRICT_MINE;
    private static ForgeConfigSpec.BooleanValue NO_DESPAWN;
    private static ForgeConfigSpec.BooleanValue NO_IDLE;
    private static ForgeConfigSpec.IntValue FIND_CHEST_RANGE;
    private static ForgeConfigSpec.IntValue DEFAULT_HEALTH;
    private static ForgeConfigSpec.BooleanValue AVOIDANCE;
    private static ForgeConfigSpec.BooleanValue SIMULATE;
    private static ForgeConfigSpec.BooleanValue DISSEMINATE;
    private static ForgeConfigSpec.IntValue PICKUP_PRIORITY;

    private static ForgeConfigSpec.BooleanValue SHOW_NODES;
    private static ForgeConfigSpec.BooleanValue SHOW_DELTA_MOVEMENT;
    private static ForgeConfigSpec.BooleanValue NEVER_DIE;
    private static ForgeConfigSpec.BooleanValue GLOW_ZOMBIE;

    static final ForgeConfigSpec SPEC = BUILDER.getRight();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent ignored) {
        Config.minDist = MIN_DISTANCE.get();
        Config.maxDist = MAX_DISTANCE.get();
        if(Config.minDist > Config.maxDist) {
            Config.maxDist = Config.minDist;
        } // ensure minDist is not greater than maxDist

        Config.mineBlocks = MINE_BLOCKS.get();
        Config.dropBlocks = DROP_BROKEN_BLOCKS.get();
        Config.targetAnimals = TARGET_ANIMALS.get();
        Config.attackInvisible = ATTACK_INVISIBLE.get();
        Config.break_speed = MINING_SPEED.get();
        Config.maxHardness = MAX_HARDNESS.get();
        Config.hardnessMultiplier = HARDNESS_MULTIPLIER.get();
        Config.zombiesClimbing = ZOMBIE_CLIMBING.get();
        Config.climbingSpeed = CLIMBING_SPEED.get();
        Config.healAmount = HEAL_AMOUNT.get();
        Config.attackCooldown = ATTACK_COOLDOWN.get();
        Config.aggressiveSpeed = AGGRESSIVE_SPEED.get();
        Config.sunSensitive = SUN_SENSITIVE.get();
        Config.noMercy = NO_MERCY.get();
        Config.attackRange = ATTACK_RANGE.get();
        Config.persistenceChance = PERSISTENCE_CHANCE.get();
        Config.maxThreshold = MAX_THRESHOLD.get();
        Config.blockCost = BLOCK_COST.get();
        Config.canFloat = CAN_FLOAT.get();
        Config.climbLimitTicks = CLIMB_LIMIT_TICKS.get();
        Config.jumpAcceleration = JUMP_ACCELERATION.get();
        Config.hyperClimbing = HYPER_CLIMBING.get();
        Config.jumpBlock = JUMP_BLOCK.get();
        Config.followRange = FOLLOW_RANGE.get();
        Config.findTargetType = TARGET_TYPE.get();
        Config.spawnUnderSun = SPAWN_UNDER_SUN.get();
        Config.canFly = CAN_FLY.get();
        Config.flySpeed = FLY_SPEED.get();
        Config.showNodes = SHOW_NODES.get();
        Config.accuracy = PATH_ACCURACY.get();
        Config.pickupRange = PICKUP_RANGE.get();
        Config.showDeltaMovement = SHOW_DELTA_MOVEMENT.get();
        Config.randomlyClimb = RANDOM_CLIMB.get();
        Config.strictMine = STRICT_MINE.get();
        Config.noDespawn = NO_DESPAWN.get();
        Config.noIdle = NO_IDLE.get();
        Config.findChest = FIND_CHEST_RANGE.get();
        Config.glowZombie = GLOW_ZOMBIE.get();
        Config.defaultHealth = DEFAULT_HEALTH.get();
        Config.neverDie = NEVER_DIE.get();
        Config.avoidance = AVOIDANCE.get();
        Config.simulate = SIMULATE.get();
        Config.disseminate = DISSEMINATE.get();
        Config.pickUpPriority = PICKUP_PRIORITY.get();
    }

    /*
        I super hard coded.
        Translation!!
     */
    public static class MCSBuilder {
        static final String MOD_CFG = Main.MOD_ID + ".midnightconfig.";
        public MCSBuilder(ForgeConfigSpec.Builder b) {
            b.push("Mining");
            MINE_BLOCKS = b.translation(MOD_CFG + "do_mine").define("zombiesMineBlocks", Config.mineBlocks);
            MINING_SPEED = b.translation(MOD_CFG + "mining_speed").defineInRange("miningSpeed", Config.break_speed, 0, Double.MAX_VALUE);
            MIN_DISTANCE = b.translation(MOD_CFG + "min_mine_dist").defineInRange("minDistForMining", Config.minDist, 0, Double.MAX_VALUE);
            MAX_DISTANCE = b.translation(MOD_CFG + "max_mine_dist").defineInRange("maxDistForMining", Config.maxDist, 0, Double.MAX_VALUE);
            MAX_HARDNESS = b.translation(MOD_CFG + "max_hardness").defineInRange("maxHardness", Config.maxHardness, 0, Double.MAX_VALUE);
            DROP_BROKEN_BLOCKS = b.translation(MOD_CFG + "drop_blocks").define("dropBrokenBlocks", Config.dropBlocks);
            HARDNESS_MULTIPLIER = b.translation(MOD_CFG + "hardness_multiplier").defineInRange("hardnessMultiplier", Config.hardnessMultiplier, 0, Double.MAX_VALUE);
            STRICT_MINE = b.translation(MOD_CFG + "strict_mine").define("strictMine", Config.strictMine);
            FIND_CHEST_RANGE = b.translation(MOD_CFG + "find_chest_range").defineInRange("findChestRange", Config.findChest, 0, 256);
            b.pop();
            b.push("Climbing");
            ZOMBIE_CLIMBING = b.translation(MOD_CFG + "do_climb").define("zombiesClimb", Config.zombiesClimbing);
            CLIMBING_SPEED = b.translation(MOD_CFG + "climb_speed").defineInRange("zombieClimbingSpeed", Config.climbingSpeed, 0, Double.MAX_VALUE);
            CLIMB_LIMIT_TICKS = b.comment("Zombie climbing limit ticks").translation(MOD_CFG + "climb_limit_ticks").defineInRange("climbLimitTicks", Config.climbLimitTicks, 1, Integer.MAX_VALUE);
            HYPER_CLIMBING = b.translation(MOD_CFG + "hyper_climbing").define("hyperClimbing", Config.hyperClimbing);
            RANDOM_CLIMB = b.translation(MOD_CFG + "randomly_climb").define("randomlyClimb", Config.randomlyClimb);
            b.pop();
            b.push("Spawn");
            PERSISTENCE_CHANCE = b.translation(MOD_CFG + "persistence_chance").defineInRange("persistenceChance", Config.persistenceChance, 0, 1);
            MAX_THRESHOLD = b.translation(MOD_CFG + "max_threshold").defineInRange("maxThreshold", Config.maxThreshold, 0, Integer.MAX_VALUE);
            SPAWN_UNDER_SUN = b.translation(MOD_CFG + "spawn_under_sun").define("spawnUnderSun", Config.spawnUnderSun);
            NO_DESPAWN = b.translation(MOD_CFG + "no_despawn").define("noDespawn", Config.noDespawn);
            b.pop();
            b.push("Attributes");
            DEFAULT_HEALTH = b.translation(MOD_CFG + "default_health").defineInRange("defaultHealth", Config.defaultHealth, 0, 1024);
            b.pop();
            b.push("Targeting");
            TARGET_ANIMALS = b.translation(MOD_CFG + "do_hurt_animals").define("zombiesTargetAnimals", Config.targetAnimals);
            BLOCK_COST = b.translation(MOD_CFG + "block_cost").defineInRange("blockCost", Config.blockCost, 1, 65536);
            FOLLOW_RANGE = b.translation(MOD_CFG + "follow_range").defineInRange("followRange", Config.followRange, 1, 128);
            TARGET_TYPE = b.translation(MOD_CFG + "find_target_type").defineEnum("findTargetType", Config.findTargetType);
            ATTACK_RANGE = b.translation(MOD_CFG + "attack_range").defineInRange("", Config.attackRange, 0.25, 127.);
            ATTACK_INVISIBLE = b.translation(MOD_CFG + "attack_invisible").define("targetVisibilityCheck", Config.attackInvisible);
            AVOIDANCE = b.translation(MOD_CFG + "avoidance").define("avoidance", Config.avoidance);
            SIMULATE = b.translation(MOD_CFG + "simulate").define("simulate", Config.simulate);
            DISSEMINATE = b.translation(MOD_CFG + "disseminate").define("disseminate", Config.disseminate);
            b.pop();
            b.push("Optimize");
            PATH_ACCURACY = b.translation(MOD_CFG + "accuracy").defineInRange("pathAccuracy", Config.accuracy, 0, 95);
            NO_IDLE = b.translation(MOD_CFG + "no_idle").define("noIdle", Config.noIdle);
            b.pop();
            b.push("Flying");
            CAN_FLY = b.translation(MOD_CFG + "can_fly").define("canFly", Config.canFly);
            FLY_SPEED = b.translation(MOD_CFG + "fly_speed").defineInRange("flySpeed", Config.flySpeed, 0, 32);
            b.pop();
            b.push("General");
            HEAL_AMOUNT = b.translation(MOD_CFG + "heal_amount").defineInRange("healAmount", Config.healAmount, 0, 1024);
            ATTACK_COOLDOWN = b.translation(MOD_CFG + "attack_cooldown").defineInRange("attackCooldown", Config.attackCooldown, 1, 1000);
            AGGRESSIVE_SPEED = b.translation(MOD_CFG + "aggressive_speed").defineInRange("aggressiveSpeed", Config.aggressiveSpeed, 0.01, 128);
            SUN_SENSITIVE = b.translation(MOD_CFG + "sun_sensitive").define("sunSensitive", Config.sunSensitive);
            NO_MERCY = b.translation(MOD_CFG + "no_mercy").define("noMercy", Config.noMercy);
            CAN_FLOAT = b.translation(MOD_CFG + "can_float").define("canFloat", Config.canFloat);
            JUMP_ACCELERATION = b.translation(MOD_CFG + "jump_acceleration").defineInRange("jumpAcceleration", Config.jumpAcceleration, 0, 128);
            JUMP_BLOCK = b.translation(MOD_CFG + "jump_block").define("jumpBlock", Config.jumpBlock);
            PICKUP_RANGE = b.translation(MOD_CFG + "pickup_range").defineInRange("pickupRange", Config.pickupRange, 0, 128);
            PICKUP_PRIORITY = b.translation(MOD_CFG + "pickup_priority").defineInRange("pickUpPriority", Config.pickUpPriority, -100, 100);
            b.pop();
            b.push("Debug");
            SHOW_NODES = b.define("showNodes", Config.showNodes);
            SHOW_DELTA_MOVEMENT = b.define("showDeltaMovement", Config.showDeltaMovement);
            NEVER_DIE = b.define("neverDie", Config.neverDie);
            GLOW_ZOMBIE = b.define("glowZombie", Config.glowZombie);
            b.pop();
        }
    }
}
