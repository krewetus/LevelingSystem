package net.owlery.statsystem.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.owlery.statsystem.StatSystemMod;

@Mod.EventBusSubscriber(modid = StatSystemMod.MOD_ID)
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SLStatCommand.register(event.getDispatcher());
    }
} 