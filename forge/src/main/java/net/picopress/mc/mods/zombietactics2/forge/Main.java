package net.picopress.mc.mods.zombietactics2.forge;

import net.minecraftforge.fml.ModLoadingContext;
import net.picopress.mc.mods.zombietactics2.impl.IMain;
import net.picopress.mc.mods.zombietactics2.commands.CommandSumZ;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.RegisterCommandsEvent;

import static net.picopress.mc.mods.zombietactics2.forge.ForgeConfig.SPEC;


@Mod.EventBusSubscriber(modid=Main.MOD_ID)
@Mod(Main.MOD_ID)
public class Main implements IMain {

    @SuppressWarnings("removal")
    public Main(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
        modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> null);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandSumZ.register(event.getDispatcher());
    }
}
