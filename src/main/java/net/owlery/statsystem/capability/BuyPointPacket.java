package net.owlery.statsystem.capability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.owlery.statsystem.config.StatSystemConfig;
import java.util.function.Supplier;

public class BuyPointPacket {
    public BuyPointPacket() {}
    public BuyPointPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                    int bought = cap.getPointsPurchased();
                    int cost = StatSystemConfig.BASE_XP_COST.get() + StatSystemConfig.XP_INCREMENT.get() * bought;
                    if (player.totalExperience < cost) return;
                    player.giveExperiencePoints(-cost);
                    cap.setPointsPurchased(bought + 1);
                    cap.setAvailablePoints(cap.getAvailablePoints() + 1);
                    PlayerStatCapabilityProvider.markDirty(player);
                    // Sync back to client
                    net.owlery.statsystem.StatSystemMod.NETWORK.sendTo(
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
                        player.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
} 