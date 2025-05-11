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
                .then(Commands.literal("add")
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
        );
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
            context.getSource().sendSuccess(() -> Component.literal("Added title: " + title), true);
            StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
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
            context.getSource().sendSuccess(() -> Component.literal("Added job: " + job), true);
            StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
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
                context.getSource().sendSuccess(() -> Component.literal("Removed title: " + title), true);
                StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
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
                context.getSource().sendSuccess(() -> Component.literal("Removed job: " + job), true);
                StatSystemMod.NETWORK.sendTo(new PlayerStatSyncPacket(cap.getUnlockedTitles(), cap.getUnlockedJobs(), cap.getEquippedTitle(), cap.getEquippedJob()), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
            } else {
                context.getSource().sendFailure(Component.literal("Job '" + job + "' was not unlocked"));
            }
        });
        
        return 1;
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