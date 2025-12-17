package net.picopress.mc.mods.zombietactics2.neoforge;

import net.picopress.mc.mods.zombietactics2.impl.IMain;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;


@Mod(value=Main.MOD_ID, dist=Dist.CLIENT)
public class ClientMain implements IMain {
    public ClientMain(IEventBus ignoredModEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        System.out.println("1234 1234");
    }
}
