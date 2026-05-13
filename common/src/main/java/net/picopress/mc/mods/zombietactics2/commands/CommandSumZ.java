package net.picopress.mc.mods.zombietactics2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;


public class CommandSumZ {
    public static final Identifier SUMZ_COMMAND_IDENTIFIER = Identifier.fromNamespaceAndPath("zombietactics2", "sumz");
    public static final Permission SUMZ_PERMISSION = Permission.Atom.create(SUMZ_COMMAND_IDENTIFIER);
    private static final Permission OP_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sumz")
                .requires(CommandSourceStack::isPlayer) // doesn't always work
                .requires(source ->
                        source.permissions().hasPermission(SUMZ_PERMISSION)
                            || source.permissions().hasPermission(OP_PERMISSION))
                .then(Commands.argument("spawnCount", IntegerArgumentType.integer(1, 1024))
                        .executes(CommandSumZ::command)));
    }
    // ex) /sumz 32 ==> summon 32 zombies
    public static int command(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Component chat;

        // Checking for player, because you can execute this command from console
        if(!src.isPlayer()) {
            chat = Component.literal("Cannot execute this command unless you are in player mode");
            src.sendSystemMessage(chat);
            return 0;
        }

        ServerLevel world = src.getLevel();
        int count = ctx.getArgument("spawnCount", Integer.class);

        for(int i = 0; i < count; ++ i) {
            Zombie z = new Zombie(world);
            z.setPos(src.getPosition());
            world.addFreshEntity(z);
        }
        chat = Component.literal(count + " zombies spawned");
        src.sendSystemMessage(chat);
        return 0;
    }
}
