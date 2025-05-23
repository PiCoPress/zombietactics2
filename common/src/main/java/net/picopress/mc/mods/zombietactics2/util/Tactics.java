package net.picopress.mc.mods.zombietactics2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

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

    public static class Item {
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

        public static boolean isBetter(Mob me, ItemStack dropped) {
            // selecting a weapon
            if(dropped.is(ItemTags.WEAPON_ENCHANTABLE)) {
                var my = me.getMainHandItem();

                if(my.is(ItemTags.WEAPON_ENCHANTABLE)) {
                    var my_weapon = Tactics.Item.getItemAttr(my, "generic.attack_damage");
                    var other = Tactics.Item.getItemAttr(dropped, "generic.attack_damage");

                    if(my_weapon == null || other == null) return false; // null check
                    return my_weapon.amount() < other.amount();
                } else return me.getMainHandItem().is(Items.AIR); // if I don't have a weapon
            } else if(dropped.getItem() instanceof ArmorItem others) { // selecting armor
                var slot = me.getItemBySlot(others.getEquipmentSlot());

                if(slot.is(Items.AIR)) return true; // if I don't have armor
                if(slot.getItem() instanceof ArmorItem equipped) {
                    return equipped.getDefense() < others.getDefense();
                }
            }
            return false;
        }
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
