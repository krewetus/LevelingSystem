package net.owlery.statsystem.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

public class PlayerStatCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<PlayerStatCapability> PLAYER_STAT_CAPABILITY = null;
    private final PlayerStatCapability backend = new PlayerStatCapability();
    private final LazyOptional<PlayerStatCapability> optional = LazyOptional.of(() -> backend);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_STAT_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        System.out.println("[StatSystem] Serializing PlayerStatCapability!");
        CompoundTag tag = new CompoundTag();
        backend.saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        System.out.println("[StatSystem] Deserializing PlayerStatCapability!");
        backend.loadNBTData(nbt);
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

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            player.getCapability(PLAYER_STAT_CAPABILITY).ifPresent(cap -> {
                StatSystemMod.NETWORK.sendTo(
                    new PlayerStatSyncPacket(
                        cap.getUnlockedTitles(),
                        cap.getUnlockedJobs(),
                        cap.getEquippedTitle(),
                        cap.getEquippedJob(),
                        cap.getUsedPoints(),
                        cap.getAvailablePoints(),
                        cap.getPointsPurchased(),
                        cap.getHealthStat(),
                        cap.getStrengthStat(),
                        cap.getAgilityStat(),
                        cap.getCharismaStat()
                    ),
                    ((net.minecraft.server.level.ServerPlayer) player).connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            Player player = event.getEntity();
            player.getCapability(PLAYER_STAT_CAPABILITY).ifPresent(cap -> {
                // Reapply attributes based on stats
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(20.0 + (cap.getHealthStat() * 1.0));
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                    .setBaseValue(1.0 + (cap.getStrengthStat() * 0.2));
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                    .setBaseValue(0.1 + (cap.getAgilityStat() - 1) * (0.1 / 99.0));
                // Optionally, heal to max if needed
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
                StatSystemMod.NETWORK.sendTo(
                    new PlayerStatSyncPacket(
                        cap.getUnlockedTitles(),
                        cap.getUnlockedJobs(),
                        cap.getEquippedTitle(),
                        cap.getEquippedJob(),
                        cap.getUsedPoints(),
                        cap.getAvailablePoints(),
                        cap.getPointsPurchased(),
                        cap.getHealthStat(),
                        cap.getStrengthStat(),
                        cap.getAgilityStat(),
                        cap.getCharismaStat()
                    ),
                    ((net.minecraft.server.level.ServerPlayer) player).connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }

    public static LazyOptional<PlayerStatCapability> get(Player player) {
        return player.getCapability(PLAYER_STAT_CAPABILITY);
    }

    public static void markDirty(Player player) {
        player.getPersistentData().putBoolean("statsystem_dirty", true);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(StatSystemMod.MOD_ID, path);
    }
} 