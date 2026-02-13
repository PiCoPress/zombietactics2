package net.picopress.mc.mods.zombietactics2.neoforge;

import net.picopress.mc.mods.zombietactics2.impl.IMain;
import net.picopress.mc.mods.zombietactics2.commands.CommandSumZ;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;


@EventBusSubscriber(modid=Main.MOD_ID)
@Mod(value=Main.MOD_ID)
public class Main implements IMain {
    public Main(IEventBus ignoredModEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.SPEC);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandSumZ.register(event.getDispatcher());
    }
}
