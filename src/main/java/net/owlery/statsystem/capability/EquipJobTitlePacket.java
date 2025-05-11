package net.owlery.statsystem.capability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class EquipJobTitlePacket {
    private final String type; // "job" or "title"
    private final String value;

    public EquipJobTitlePacket(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public EquipJobTitlePacket(FriendlyByteBuf buf) {
        this.type = buf.readUtf();
        this.value = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(type);
        buf.writeUtf(value);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                    if (type.equals("job") && cap.getUnlockedJobs().contains(value)) {
                        cap.equipJob(value);
                    } else if (type.equals("title") && cap.getUnlockedTitles().contains(value)) {
                        cap.equipTitle(value);
                    }
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