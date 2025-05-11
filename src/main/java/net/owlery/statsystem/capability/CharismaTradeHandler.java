package net.owlery.statsystem.capability;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.owlery.statsystem.StatSystemMod;

@Mod.EventBusSubscriber(modid = StatSystemMod.MOD_ID)
public class CharismaTradeHandler {
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        // This event does not provide the player, so we cannot apply a player-specific discount here.
        // Charisma discounts must be handled when the trade is actually executed (see below for a possible approach).
    }
} 