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

    public PlayerStatSyncPacket(Set<String> unlockedTitles, Set<String> unlockedJobs, String equippedTitle, String equippedJob) {
        this.unlockedTitles = unlockedTitles;
        this.unlockedJobs = unlockedJobs;
        this.equippedTitle = equippedTitle;
        this.equippedJob = equippedJob;
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
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(unlockedTitles.size());
        for (String t : unlockedTitles) buf.writeUtf(t);
        buf.writeVarInt(unlockedJobs.size());
        for (String j : unlockedJobs) buf.writeUtf(j);
        buf.writeUtf(equippedTitle);
        buf.writeUtf(equippedJob);
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
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
} 