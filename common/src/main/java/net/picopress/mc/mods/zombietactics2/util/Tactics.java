package net.picopress.mc.mods.zombietactics2.util;

import net.picopress.mc.mods.zombietactics2.impl.Plane;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


// because it is utility class
@SuppressWarnings("unused")
public class Tactics {
    public static final BlockPos UNIT_FRONT = new BlockPos(0, 0, 1);

    public static Rotation getRelativeRotation(Mob mob) {
        Vec3i norm = mob.getNearestViewDirection().getNormal();
        int x = norm.getX(), z = norm.getZ();
        if(x == 0 && z == 1) return Rotation.NONE;
        else if(x == 0 && z == -1) return Rotation.CLOCKWISE_180;
        else if(x == -1 && z == 0) return Rotation.CLOCKWISE_90;
        else return Rotation.COUNTERCLOCKWISE_90; // x = 1, z = 0
    }

    public static float getExactDamage(@NotNull Mob mob, LivingEntity target) {
        ServerLevel serverLevel = getSl(mob);
        var tmp = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if(tmp == null) return 0;
        float dam = (float)tmp.getValue();
        if(serverLevel == null) return dam;
        return EnchantmentHelper.modifyDamage(serverLevel, mob.getWeaponItem(), target, mob.damageSources().mobAttack(mob), dam);
    }

    // thonk
    public static class Heuristic {
        /**
         * calculates the power of the mob
         * @param target the target entity
         * @return the power of the mob, which is calculated as (attack + 1) * (health / 5) * speed + 1
         */
        public static int getEnemyPower(LivingEntity target) {
            var attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
            return (int)((attack != null? attack.getValue(): 0) / 2 * target.getHealth() / 5 * target.getSpeed() + 1);
        }

        /**
         *
         * @param clazz the class of the mob
         * @param mob attacker
         * @param target the target entity
         * @return true if the mob can win against the target, false otherwise
         */
        public static boolean simulate(Class<? extends LivingEntity> clazz, Mob mob, LivingEntity target) {
            // friends list
            var peers = mob.level().getEntitiesOfClass(clazz, ((Plane)mob).zombietactics2$getFollowingArea(), (liv) -> liv != mob);
            // win by outnumbering
            if(peers.size() > 15) return true;
            int opponent = getEnemyPower(target); // enemy
            int me = getEnemyPower(mob); // just me
            int peer_power = 0; // friends power

            for(LivingEntity peer: peers) {
                peer_power += getEnemyPower(peer);
            }
            return me + peer_power >= opponent;
        }

        // alternative for simulate
        public static boolean needAvoid(Mob mob, LivingEntity target) {
            if(target == null) return false;
            var attack = target.getAttribute(Attributes.ATTACK_DAMAGE);
            if(attack != null) {
                return mob.getHealth() <= attack.getValue() && target.getHealth() > mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
            return false;
        }
    }

    public static class ItemUtil {
        /**
         * returns the defense point
         * @param armor armor item
         * @return the defense point of the armor, which is calculated as (defense + 1) * (toughness + 1)
         */
        static double getDefensePoint(ArmorItem armor) {
            return (armor.getDefense() + 1) * (armor.getToughness() + 1);
        }

        /**
         * it retrieves an attribute modifier of the item stack by its path and namespace.
         * you should check if it is null
         * @param stack an item stack
         * @param path the path of the attribute, e.g. "generic.attack_damage"
         * @param namespace the namespace of the attribute, e.g. "minecraft"
         * @return the attribute modifier of the item stack, or null if not found
         */
        public static @Nullable AttributeModifier getItemAttr(ItemStack stack, String path, String namespace) {
            ItemAttributeModifiers component = stack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
            if(component == null) return null;

            List<ItemAttributeModifiers.Entry> list = component.modifiers();
            ItemAttributeModifiers.Entry entry = null;
            for(var attr: list) {
                if(attr.attribute().is(ResourceLocation.fromNamespaceAndPath(namespace, path))) {
                    entry = attr;
                    break;
                }
            }
            if(entry == null) return null;
            return entry.modifier();
        }

        /**
         *
         * @param stack an item stack
         * @param path the path of the attribute with the namespace "minecraft"
         * @return the attribute modifier of the item stack, or null if not found
         */
        static public @Nullable AttributeModifier getItemAttr(ItemStack stack, String path) {
            return getItemAttr(stack, path, "minecraft");
        }

        /**
         *
         * @param me the mob holding the item
         * @param dropped dropped stuff
         * @return true if the dropped item is better than the mob's one, false otherwise
         */
        public static boolean isBetter(Mob me, ItemStack dropped) {
            // selecting a weapon
            var my = me.getMainHandItem();
            double test1 = 0, test2 = 0;
            boolean is_armor = false;

            if(dropped.is(ItemTags.WEAPON_ENCHANTABLE)) {
                if(my.is(Items.AIR)) return my.is(Items.AIR);

                var my_weapon = ItemUtil.getItemAttr(my, "generic.attack_damage");
                var other = ItemUtil.getItemAttr(dropped, "generic.attack_damage");

                if(my_weapon == null || other == null) return false; // null check
                test1 = my_weapon.amount();
                test2 = other.amount();
                // if I don't have a weapon
            } else if(dropped.getItem() instanceof ArmorItem others) { // selecting armor
                is_armor = true;
                if(EnchantmentHelper.has(my, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) return false;

                var slot = me.getItemBySlot(others.getEquipmentSlot());
                if(slot.is(Items.AIR)) return true; // if I don't have armor
                if(slot.getItem() instanceof ArmorItem equipped) {
                    test1 = getDefensePoint(equipped);
                    test2 = getDefensePoint(others);
                }
            }

            if(test1 < test2) return true;
            if(test1 == test2 && !is_armor) {
                // this damage is not the attack of damage
                if(my.getDamageValue() > dropped.getDamageValue()) {
                    return true; // my weapon is more damaged
                } else {
                    return checkDamageable(dropped) && !checkDamageable(my);
                }
            }
            return false;
        }

        private static boolean checkDamageable(ItemStack stack) {
            DataComponentMap dataComponentMap = stack.getComponents();
            int i = dataComponentMap.size();
            return i > 1 || i == 1 && !dataComponentMap.has(DataComponents.DAMAGE);
        }
    }

    public static ServerLevel getSl(Mob mob) {
        var stuff = mob.getServer();
        return stuff != null? stuff.getLevel(mob.level().dimension()): null; // I'm not in the server
    }

    // alias
    public static ServerLevel getServerLevel(Mob mob) {
        return getSl(mob);
    }

    public static class World {
        // chunk xz = 16*16
        public static LevelChunk[] getNearbyChunks(Level level, BlockPos pos) {
            LevelChunk[] list = new LevelChunk[9];
            int idx = 0;
            for(int i = -1; i <= 1; ++ i) {
                for(int j = -1; j <= 1; ++ j) {
                    list[idx] = level.getChunkAt(pos.offset(16 * i, 0, 16 * j));
                    ++ idx;
                }
            }
            return list;
        }

        // if a block is null, return all blocks in the AABB
        public static List<BlockPos> findBlocks(Level level, AABB aabb, @Nullable Block block) {
            return findBlocks(level, block,
                    (int)aabb.minX, (int)aabb.minY, (int)aabb.minZ,
                    (int)aabb.maxX, (int)aabb.maxY, (int)aabb.maxZ);
        }

        public static List<BlockPos> findBlocks(Level level, @Nullable Block block, int x1, int y1, int z1, int x2, int y2, int z2) {
            List<BlockPos> list = new ArrayList<>();
            BlockPos.MutableBlockPos point = new BlockPos.MutableBlockPos();
            for(int x = x1; x <= x2; ++ x) {
                for(int y = y1; y <= y2; ++ y) {
                    for(int z = z1; z <= z2; ++ z) {
                        point.set(x, y, z);
                        if(level.getBlockState(point).is(block) || block == null) {
                            list.add(point.immutable());
                        }
                    }
                }
            }
            return list;
        }

        public static int ManhattanDistance(BlockPos p1, BlockPos p2) {
            return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()) + Math.abs(p1.getZ() - p2.getZ());
        }
    }

    private Tactics() {}
}