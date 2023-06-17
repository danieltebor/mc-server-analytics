package com.danieltebor.mc_server_analytics.command.commands;

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.command.CommandOutputBuilder;
import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;
import com.danieltebor.mc_server_analytics.util.WorldFileInfoTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class WorldSizeCommand extends MCServerAnalyticsCommand {
    public static final String NAME = "ping";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows your ping or ping of specified player";

    public WorldSizeCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("world-size")
            .executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final WorldFileInfoTracker worldFileInfoTracker = MCServerAnalytics.getInstance().getWorldFileInfoTracker();
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("World Size", 
            CommandOutputBuilder.Color.AQUA, !context.getSource().isExecutedByPlayer());

        if (!worldFileInfoTracker.isTracking() && worldFileInfoTracker.getWorldSizeBytes() == -1) {
            context.getSource().sendError(Text.literal("World size is unavailable"));
            return 0;
        }

        appendOutput(outputBuilder, worldFileInfoTracker);

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }

    public static void appendOutput(final CommandOutputBuilder outputBuilder, final WorldFileInfoTracker worldFileInfoTracker) {
        outputBuilder.append(" | ");
        outputBuilder.append(worldFileInfoTracker.getWorldSizeMB(), CommandOutputBuilder.Color.BLUE);
        
        outputBuilder.append("MB (");
        outputBuilder.append(worldFileInfoTracker.getWorldSizeGB(), true, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("GB)");

        if (!worldFileInfoTracker.isTracking()) {
            outputBuilder.append(" *NOT ACTIVELY TRACKING", CommandOutputBuilder.Color.DARK_RED);
        }
    }
}
