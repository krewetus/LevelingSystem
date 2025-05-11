package net.owlery.statsystem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.owlery.statsystem.ModJobs;
import net.owlery.statsystem.ModTitles;
import net.owlery.statsystem.capability.PlayerStatCapabilityProvider;
import net.owlery.statsystem.StatSystemMod;
import net.owlery.statsystem.capability.PlayerStatSyncPacket;
import java.util.concurrent.CompletableFuture;

public class SLStatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("slstat")
                .requires(source -> source.hasPermission(2)) // Only allow OPs
                .then(Commands.literal("add")
                    .then(Commands.literal("ALL")
                        .executes(SLStatCommand::addAll))
                    .then(Commands.literal("title")
                        .then(Commands.argument("title", StringArgumentType.string())
                            .suggests(SLStatCommand::suggestTitles)
                            .executes(SLStatCommand::addTitle)))
                    .then(Commands.literal("job")
                        .then(Commands.argument("job", StringArgumentType.string())
                            .suggests(SLStatCommand::suggestJobs)
                            .executes(SLStatCommand::addJob))))
                .then(Commands.literal("remove")
                    .then(Commands.literal("title")
                        .then(Commands.argument("title", StringArgumentType.string())
                            .suggests(SLStatCommand::suggestTitles)
                            .executes(SLStatCommand::removeTitle)))
                    .then(Commands.literal("job")
                        .then(Commands.argument("job", StringArgumentType.string())
                            .suggests(SLStatCommand::suggestJobs)
                            .executes(SLStatCommand::removeJob))))
                .then(Commands.literal("reset")
                    .executes(SLStatCommand::resetStats))
        );
    }

    private static int addAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            for (String t : ModTitles.TITLES) cap.unlockTitle(t);
            for (String j : ModJobs.JOBS) cap.unlockJob(j);
            PlayerStatCapabilityProvider.markDirty(player);
            context.getSource().sendSuccess(() -> Component.literal("Added all jobs and titles!"), true);
            StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob(), cap.getUsedPoints(), cap.getAvailablePoints(), cap.getPointsPurchased(), cap.getHealthStat(), cap.getStrengthStat(), cap.getAgilityStat(), cap.getCharismaStat()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        });
        return 1;
    }

    private static int addTitle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        final String inputTitle = StringArgumentType.getString(context, "title");
        
        // Check if title exists
        String foundTitle = null;
        for (String t : ModTitles.TITLES) {
            if (t.equalsIgnoreCase(inputTitle)) {
                foundTitle = t;
                break;
            }
        }
        
        if (foundTitle == null) {
            context.getSource().sendFailure(Component.literal("Title '" + inputTitle + "' does not exist"));
            return 0;
        }
        
        final String title = foundTitle;
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            cap.unlockTitle(title);
            cap.equipTitle(title);
            PlayerStatCapabilityProvider.markDirty(player);
            System.out.println("[StatSystem] After unlockTitle/equipTitle: " + cap.getUnlockedTitles() + ", equipped: " + cap.getEquippedTitle());
            context.getSource().sendSuccess(() -> Component.literal("Added title: " + title), true);
            StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob(), cap.getUsedPoints(), cap.getAvailablePoints(), cap.getPointsPurchased(), cap.getHealthStat(), cap.getStrengthStat(), cap.getAgilityStat(), cap.getCharismaStat()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        });
        
        return 1;
    }

    private static int addJob(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        final String inputJob = StringArgumentType.getString(context, "job");
        
        // Check if job exists
        String foundJob = null;
        for (String j : ModJobs.JOBS) {
            if (j.equalsIgnoreCase(inputJob)) {
                foundJob = j;
                break;
            }
        }
        
        if (foundJob == null) {
            context.getSource().sendFailure(Component.literal("Job '" + inputJob + "' does not exist"));
            return 0;
        }
        
        final String job = foundJob;
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            cap.unlockJob(job);
            cap.equipJob(job);
            PlayerStatCapabilityProvider.markDirty(player);
            System.out.println("[StatSystem] After unlockJob/equipJob: " + cap.getUnlockedJobs() + ", equipped: " + cap.getEquippedJob());
            context.getSource().sendSuccess(() -> Component.literal("Added job: " + job), true);
            StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob(), cap.getUsedPoints(), cap.getAvailablePoints(), cap.getPointsPurchased(), cap.getHealthStat(), cap.getStrengthStat(), cap.getAgilityStat(), cap.getCharismaStat()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
        });
        
        return 1;
    }

    private static int removeTitle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        final String inputTitle = StringArgumentType.getString(context, "title");
        
        // Check if title exists
        String foundTitle = null;
        for (String t : ModTitles.TITLES) {
            if (t.equalsIgnoreCase(inputTitle)) {
                foundTitle = t;
                break;
            }
        }
        
        if (foundTitle == null) {
            context.getSource().sendFailure(Component.literal("Title '" + inputTitle + "' does not exist"));
            return 0;
        }
        
        // Don't allow removing default titles
        if (foundTitle.equals("Noob")) {
            context.getSource().sendFailure(Component.literal("Cannot remove the default title"));
            return 0;
        }
        
        final String title = foundTitle;
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            if (cap.getUnlockedTitles().remove(title)) {
                // If the removed title was equipped, switch to default
                if (title.equals(cap.getEquippedTitle())) {
                    cap.equipTitle("Noob");
                }
                PlayerStatCapabilityProvider.markDirty(player);
                context.getSource().sendSuccess(() -> Component.literal("Removed title: " + title), true);
                StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob(), cap.getUsedPoints(), cap.getAvailablePoints(), cap.getPointsPurchased(), cap.getHealthStat(), cap.getStrengthStat(), cap.getAgilityStat(), cap.getCharismaStat()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
            } else {
                context.getSource().sendFailure(Component.literal("Title '" + title + "' was not unlocked"));
            }
        });
        
        return 1;
    }

    private static int removeJob(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        final String inputJob = StringArgumentType.getString(context, "job");
        
        // Check if job exists
        String foundJob = null;
        for (String j : ModJobs.JOBS) {
            if (j.equalsIgnoreCase(inputJob)) {
                foundJob = j;
                break;
            }
        }
        
        if (foundJob == null) {
            context.getSource().sendFailure(Component.literal("Job '" + inputJob + "' does not exist"));
            return 0;
        }
        
        // Don't allow removing default jobs
        if (foundJob.equals("Jobless")) {
            context.getSource().sendFailure(Component.literal("Cannot remove the default job"));
            return 0;
        }
        
        final String job = foundJob;
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            if (cap.getUnlockedJobs().remove(job)) {
                // If the removed job was equipped, switch to default
                if (job.equals(cap.getEquippedJob())) {
                    cap.equipJob("Jobless");
                }
                PlayerStatCapabilityProvider.markDirty(player);
                context.getSource().sendSuccess(() -> Component.literal("Removed job: " + job), true);
                StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob(), cap.getUsedPoints(), cap.getAvailablePoints(), cap.getPointsPurchased(), cap.getHealthStat(), cap.getStrengthStat(), cap.getAgilityStat(), cap.getCharismaStat()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
            } else {
                context.getSource().sendFailure(Component.literal("Job '" + job + "' was not unlocked"));
            }
        });
        
        return 1;
    }

    private static int resetStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerStatCapabilityProvider.get(player).ifPresent(cap -> {
            // Calculate XP to refund
            final int totalXP = calculateRefundXP(cap.getUsedPoints());
            // Reset stats
            cap.setHealthStat(0);
            cap.setStrengthStat(0);
            cap.setAgilityStat(0);
            cap.setCharismaStat(0);
            cap.setUsedPoints(0);
            cap.setAvailablePoints(0);
            cap.setPointsPurchased(0);
            // Reset attributes
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20.0 + (cap.getHealthStat() * 1.0));
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(1.0);
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
            // Heal player to new max if they were at max before
            if (player.getHealth() == player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            // Refund XP
            player.giveExperiencePoints(totalXP);
            PlayerStatCapabilityProvider.markDirty(player);
            context.getSource().sendSuccess(() -> Component.literal("Reset all stats and refunded " + totalXP + " XP"), true);
            // Sync back to client
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
                player.connection.connection,
                net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
            );
        });
        return 1;
    }

    private static int calculateRefundXP(int usedPoints) {
        int totalXP = 0;
        for (int i = 0; i < usedPoints; i++) {
            int cost = 100;
            for (int j = 0; j < i; j++) {
                cost = (int) (Math.round(cost * 1.3 + 50) / 100.0) * 100;
            }
            totalXP += cost;
        }
        return totalXP;
    }

    private static CompletableFuture<Suggestions> suggestTitles(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String t : ModTitles.TITLES) {
            builder.suggest('"' + t + '"');
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestJobs(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String j : ModJobs.JOBS) {
            builder.suggest('"' + j + '"');
        }
        return builder.buildFuture();
    }
} 