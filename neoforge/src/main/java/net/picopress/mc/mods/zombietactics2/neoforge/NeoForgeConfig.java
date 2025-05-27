package net.picopress.mc.mods.zombietactics2.neoforge;

import net.picopress.mc.mods.zombietactics2.Config;
import net.picopress.mc.mods.zombietactics2.attachments.FindTargetType;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;


@EventBusSubscriber(modid=Main.MOD_ID, bus=EventBusSubscriber.Bus.MOD)
public class NeoForgeConfig {
    private static final Pair<MCSBuilder, ModConfigSpec> BUILDER = new ModConfigSpec.Builder().configure(MCSBuilder::new);
    private static ModConfigSpec.BooleanValue TARGET_ANIMALS;
    private static ModConfigSpec.BooleanValue ATTACK_INVISIBLE;
    private static ModConfigSpec.BooleanValue MINE_BLOCKS;
    private static ModConfigSpec.DoubleValue MIN_DISTANCE;
    private static ModConfigSpec.DoubleValue MAX_DISTANCE;
    private static ModConfigSpec.BooleanValue DROP_BROKEN_BLOCKS;
    private static ModConfigSpec.BooleanValue ZOMBIE_CLIMBING;
    private static ModConfigSpec.DoubleValue CLIMBING_SPEED;
    private static ModConfigSpec.DoubleValue MINING_SPEED;
    private static ModConfigSpec.DoubleValue MAX_HARDNESS;
    private static ModConfigSpec.DoubleValue HARDNESS_MULTIPLIER;
    private static ModConfigSpec.DoubleValue HEAL_AMOUNT;
    private static ModConfigSpec.IntValue ATTACK_COOLDOWN;
    private static ModConfigSpec.DoubleValue AGGRESSIVE_SPEED;
    private static ModConfigSpec.BooleanValue SUN_SENSITIVE;
    private static ModConfigSpec.BooleanValue NO_MERCY;
    private static ModConfigSpec.DoubleValue ATTACK_RANGE;
    private static ModConfigSpec.DoubleValue PERSISTENCE_CHANCE;
    private static ModConfigSpec.IntValue MAX_THRESHOLD;
    private static ModConfigSpec.IntValue BLOCK_COST;
    private static ModConfigSpec.BooleanValue CAN_FLOAT;
    private static ModConfigSpec.IntValue CLIMB_LIMIT_TICKS;
    private static ModConfigSpec.DoubleValue JUMP_ACCELERATION;
    private static ModConfigSpec.BooleanValue HYPER_CLIMBING;
    private static ModConfigSpec.BooleanValue JUMP_BLOCK;
    private static ModConfigSpec.IntValue FOLLOW_RANGE;
    private static ModConfigSpec.EnumValue<FindTargetType> TARGET_TYPE;
    private static ModConfigSpec.BooleanValue SPAWN_UNDER_SUN;
    private static ModConfigSpec.BooleanValue CAN_FLY;
    private static ModConfigSpec.DoubleValue FLY_SPEED;
    private static ModConfigSpec.IntValue PATH_ACCURACY;
    private static ModConfigSpec.IntValue PICKUP_RANGE;
    private static ModConfigSpec.BooleanValue RANDOM_CLIMB;
    private static ModConfigSpec.BooleanValue STRICT_MINE;
    private static ModConfigSpec.BooleanValue NO_DESPAWN;
    private static ModConfigSpec.BooleanValue NO_IDLE;
    private static ModConfigSpec.IntValue FIND_CHEST_RANGE;
    private static ModConfigSpec.IntValue DEFAULT_HEALTH;
    private static ModConfigSpec.BooleanValue AVOIDANCE;
    private static ModConfigSpec.BooleanValue SIMULATE;
    private static ModConfigSpec.BooleanValue DISSEMINATE;

    private static ModConfigSpec.BooleanValue SHOW_NODES;
    private static ModConfigSpec.BooleanValue SHOW_DELTA_MOVEMENT;
    private static ModConfigSpec.BooleanValue NEVER_DIE;
    private static ModConfigSpec.BooleanValue GLOW_ZOMBIE;

    static final ModConfigSpec SPEC = BUILDER.getRight();

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
    }

    /*
        I super hard coded.
        Translation!!
     */
    public static class MCSBuilder {
        static final String MOD_CFG = Main.MOD_ID + ".midnightconfig.";
        public MCSBuilder(ModConfigSpec.Builder b) {
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
