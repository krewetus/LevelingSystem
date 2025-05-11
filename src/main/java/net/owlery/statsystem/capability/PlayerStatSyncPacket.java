package net.owlery.statsystem.capability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PlayerStatSyncPacket {
    private final Set<String> unlockedTitles;
    private final Set<String> unlockedJobs;
    private final String equippedTitle;
    private final String equippedJob;
    private final int usedPoints;
    private final int availablePoints;
    private final int pointsPurchased;
    private final int healthStat;
    private final int strengthStat;
    private final int agilityStat;
    private final int charismaStat;

    public PlayerStatSyncPacket(Set<String> unlockedTitles, Set<String> unlockedJobs, String equippedTitle, String equippedJob, int usedPoints, int availablePoints, int pointsPurchased, int healthStat, int strengthStat, int agilityStat, int charismaStat) {
        this.unlockedTitles = unlockedTitles;
        this.unlockedJobs = unlockedJobs;
        this.equippedTitle = equippedTitle;
        this.equippedJob = equippedJob;
        this.usedPoints = usedPoints;
        this.availablePoints = availablePoints;
        this.pointsPurchased = pointsPurchased;
        this.healthStat = healthStat;
        this.strengthStat = strengthStat;
        this.agilityStat = agilityStat;
        this.charismaStat = charismaStat;
    }

    public PlayerStatSyncPacket(FriendlyByteBuf buf) {
        this.unlockedTitles = new HashSet<>();
        int titleCount = buf.readVarInt();
        for (int i = 0; i < titleCount; i++) this.unlockedTitles.add(buf.readUtf());
        this.unlockedJobs = new HashSet<>();
        int jobCount = buf.readVarInt();
        for (int i = 0; i < jobCount; i++) this.unlockedJobs.add(buf.readUtf());
        this.equippedTitle = buf.readUtf();
        this.equippedJob = buf.readUtf();
        this.usedPoints = buf.readVarInt();
        this.availablePoints = buf.readVarInt();
        this.pointsPurchased = buf.readVarInt();
        this.healthStat = buf.readVarInt();
        this.strengthStat = buf.readVarInt();
        this.agilityStat = buf.readVarInt();
        this.charismaStat = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(unlockedTitles.size());
        for (String t : unlockedTitles) buf.writeUtf(t);
        buf.writeVarInt(unlockedJobs.size());
        for (String j : unlockedJobs) buf.writeUtf(j);
        buf.writeUtf(equippedTitle);
        buf.writeUtf(equippedJob);
        buf.writeVarInt(usedPoints);
        buf.writeVarInt(availablePoints);
        buf.writeVarInt(pointsPurchased);
        buf.writeVarInt(healthStat);
        buf.writeVarInt(strengthStat);
        buf.writeVarInt(agilityStat);
        buf.writeVarInt(charismaStat);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
                    cap.getUnlockedTitles().clear();
                    cap.getUnlockedTitles().addAll(unlockedTitles);
                    cap.getUnlockedJobs().clear();
                    cap.getUnlockedJobs().addAll(unlockedJobs);
                    cap.equipTitle(equippedTitle);
                    cap.equipJob(equippedJob);
                    cap.setUsedPoints(usedPoints);
                    cap.setAvailablePoints(availablePoints);
                    cap.setPointsPurchased(pointsPurchased);
                    cap.setHealthStat(healthStat);
                    cap.setStrengthStat(strengthStat);
                    cap.setAgilityStat(agilityStat);
                    cap.setCharismaStat(charismaStat);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
} 