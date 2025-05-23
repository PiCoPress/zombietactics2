package net.picopress.mc.mods.zombietactics2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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

    // for 1.21.5 or maybe later
    // 1.21.5 has changed the way to get item properties
    // for example, ArmorItem and SwordItem were disappeared
    public static class ItemUtil {
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

        public static boolean isBetter(Mob mob, @NotNull ItemStack stack) {
            // selecting a weapon
            if(stack.is(ItemTags.WEAPON_ENCHANTABLE)) {
                ItemStack my = mob.getMainHandItem();

                if(my.is(ItemTags.WEAPON_ENCHANTABLE)) {
                    var my_weapon = ItemUtil.getItemAttr(my, "attack_damage");
                    var other = ItemUtil.getItemAttr(stack, "attack_damage");

                    if(my_weapon == null || other == null) return false; // null check
                    return my_weapon.amount() < other.amount();
                } else return my.is(Items.AIR); // if I don't have a weapon
            } else if(stack.is(ItemTags.ARMOR_ENCHANTABLE)) { // selecting armor
                ItemStack slot = mob.getItemBySlot(Objects.requireNonNull(stack.getItem().components().get(DataComponents.EQUIPPABLE)).slot());

                if(slot.is(Items.AIR)) return true; // if I don't have armor
                else if(slot.getItem().components().has(DataComponents.EQUIPPABLE)) {
                    var dropped = ItemUtil.getItemAttr(stack, "armor_toughness");
                    var equipped = ItemUtil.getItemAttr(slot, "armor_toughness");

                    // and the both have to not be null
                    if(dropped != null && equipped != null) {
                        return equipped.amount() < dropped.amount();
                    } else System.out.println("what the fuck [mine: " + equipped + ", other:" + dropped);
                }
            }
            return false;
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