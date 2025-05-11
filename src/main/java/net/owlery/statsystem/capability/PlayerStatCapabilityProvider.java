package net.owlery.statsystem.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.owlery.statsystem.StatSystemMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;

public class PlayerStatCapabilityProvider implements ICapabilityProvider {
    public static Capability<PlayerStatCapability> PLAYER_STAT_CAPABILITY = null;
    private final PlayerStatCapability backend = new PlayerStatCapability();
    private final LazyOptional<PlayerStatCapability> optional = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_STAT_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerStatCapability.class);
    }

    // Attach to player
    @Mod.EventBusSubscriber(modid = StatSystemMod.MOD_ID)
    public static class Events {
        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(rl("player_stat"), new PlayerStatCapabilityProvider());
            }
        }

        @SubscribeEvent
        public static void playerClone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PLAYER_STAT_CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(PLAYER_STAT_CAPABILITY).ifPresent(newStore -> {
                    CompoundTag tag = new CompoundTag();
                    oldStore.saveNBTData(tag);
                    newStore.loadNBTData(tag);
                });
            });
        }
    }

    public static LazyOptional<PlayerStatCapability> get(Player player) {
        return player.getCapability(PLAYER_STAT_CAPABILITY);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(StatSystemMod.MOD_ID, path);
    }
} 