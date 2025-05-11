package net.owlery.statsystem;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.owlery.statsystem.item.ModCreativeModTabs;
import org.slf4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.owlery.statsystem.capability.PlayerStatSyncPacket;
import net.owlery.statsystem.capability.EquipJobTitlePacket;
import net.owlery.statsystem.capability.UpgradeStatPacket;
import net.owlery.statsystem.capability.BuyPointPacket;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.player.Player;
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StatSystemMod.MOD_ID)
public class StatSystemMod{
    public static final String MOD_ID = "statsystem";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public StatSystemMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModCreativeModTabs.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        context.getModEventBus().addListener(this::setup);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM THE SETUP");
        LOGGER.info("NOT DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.SPRUCE_LOG));
    }

    private void setup(final FMLCommonSetupEvent event) {
        int id = 0;
        NETWORK.registerMessage(
            id++,
            PlayerStatSyncPacket.class,
            PlayerStatSyncPacket::toBytes,
            PlayerStatSyncPacket::new,
            (msg, ctx) -> { msg.handle(ctx); }
        );
        NETWORK.registerMessage(
            id++,
            EquipJobTitlePacket.class,
            EquipJobTitlePacket::toBytes,
            EquipJobTitlePacket::new,
            (msg, ctx) -> { msg.handle(ctx); }
        );
        NETWORK.registerMessage(
            id++,
            UpgradeStatPacket.class,
            UpgradeStatPacket::toBytes,
            UpgradeStatPacket::new,
            (msg, ctx) -> { msg.handle(ctx); }
        );
        NETWORK.registerMessage(
            id++,
            BuyPointPacket.class,
            BuyPointPacket::toBytes,
            BuyPointPacket::new,
            (msg, ctx) -> { msg.handle(ctx); }
        );
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event){
        LOGGER.info("im edg- i mean starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
        }
    }
}
