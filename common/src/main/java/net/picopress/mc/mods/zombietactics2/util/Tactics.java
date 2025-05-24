package net.picopress.mc.mods.zombietactics2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// because it is utility class
@SuppressWarnings("unused")
public class Tactics {
    public static final BlockPos UNIT_FRONT = new BlockPos(0, 0, 1);

    public static Rotation getRelativeRotation(Mob mob) {
        Vec3i norm = mob.getNearestViewDirection().getUnitVec3i();
        int x = norm.getX(), z = norm.getZ();
        if(x == 0 && z == 1) return Rotation.NONE;
        else if(x == 0 && z == -1) return Rotation.CLOCKWISE_180;
        else if(x == -1 && z == 0) return Rotation.CLOCKWISE_90;
        else return Rotation.COUNTERCLOCKWISE_90; // x = 1, z = 0
    }

    public static class ItemUtil {
        static double getDefensePoint(ArmorItem armor) {
            return (armor.getDefense() + 1) * (armor.getToughness() + 1);
        }

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

        // default namespace
        static public @Nullable AttributeModifier getItemAttr(ItemStack stack, String path) {
            return getItemAttr(stack, path, "minecraft");
        }

        public static boolean isBetter(Mob me, @NotNull ItemStack dropped) {
            // selecting a weapon
            var my = me.getMainHandItem();
            double test1 = 0, test2 = 0;

            if(dropped.is(ItemTags.WEAPON_ENCHANTABLE)) {
                if(my.is(Items.AIR)) return my.is(Items.AIR);

                var my_weapon = ItemUtil.getItemAttr(my, "generic.attack_damage");
                var other = ItemUtil.getItemAttr(dropped, "generic.attack_damage");

                if(my_weapon == null || other == null) return false; // null check
                test1 = my_weapon.amount();
                test2 = other.amount();
                // if I don't have a weapon
            } else if(dropped.getItem() instanceof ArmorItem others) { // selecting armor
                if(EnchantmentHelper.has(my, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) return false;

                var slot = me.getItemBySlot(others.getEquipmentSlot());
                if(slot.is(Items.AIR)) return true; // if I don't have armor
                if(slot.getItem() instanceof ArmorItem equipped) {
                    test1 = getDefensePoint(equipped);
                    test2 = getDefensePoint(others);
                }
            }

            if(test1 < test2) return true;
            if(test1 == test2) {
                if(my.getDamageValue() > dropped.getDamageValue()) {
                    return true; // if my weapon is more damaged
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
}
