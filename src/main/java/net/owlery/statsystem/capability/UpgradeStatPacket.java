package net.owlery.statsystem.capability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.owlery.statsystem.config.StatSystemConfig;
import java.util.function.Supplier;

public class UpgradeStatPacket {
    private final String stat; // "health", "strength", "agility", "charisma"

    public UpgradeStatPacket(String stat) {
        this.stat = stat;
    }

    public UpgradeStatPacket(FriendlyByteBuf buf) {
        this.stat = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(stat);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                    int current;
                    switch (stat) {
                        case "health": current = cap.getHealthStat(); break;
                        case "strength": current = cap.getStrengthStat(); break;
                        case "agility": current = cap.getAgilityStat(); break;
                        case "charisma": current = cap.getCharismaStat(); break;
                        default: return;
                    }
                    if (current >= StatSystemConfig.LEVEL_CAP.get()) return;
                    if (cap.getAvailablePoints() <= 0) return;
                    // Upgrade stat
                    switch (stat) {
                        case "health": 
                            cap.setHealthStat(current + 1);
                            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20.0 + (current + 1));
                            if (player.getHealth() == player.getMaxHealth()) {
                                player.setHealth(player.getMaxHealth());
                            }
                            break;
                        case "strength": 
                            cap.setStrengthStat(current + 1);
                            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(1.0 + (current + 1) * 0.2);
                            break;
                        case "agility": 
                            cap.setAgilityStat(current + 1);
                            // At max agility, player is 0.2 (twice as fast as default 0.1)
                            double newSpeed = 0.1 + ((current + 1) - 1) * (0.1 / (StatSystemConfig.LEVEL_CAP.get() - 1));
                            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(newSpeed);
                            break;
                        case "charisma":
                            cap.setCharismaStat(current + 1);
                            break;
                    }
                    cap.setUsedPoints(cap.getUsedPoints() + 1);
                    cap.setAvailablePoints(cap.getAvailablePoints() - 1);
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