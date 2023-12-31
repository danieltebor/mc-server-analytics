/*
 * Copyright (C) 2023 Daniel Tebor
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.danieltebor.mc_server_analytics.command;

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Daniel Tebor
 */
public final class PingCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "ping";
    public static final String[][] ARG_NAMES = {{"player"}};
    public static final String DESCRIPTION = "Shows your ping or ping of specified player";

    public PingCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder()
            .then(CommandManager.argument(ARG_NAMES[0][0], EntityArgumentType.players())
            .executes(this::executeParameterizedWrapper));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        if (isServerConsoleOutput) {
            sendErrorOutput(context, "Invalid command usage");
            return 0;
        }
        
        sendOutput(context, buildOutput(context.getSource().getPlayer(), true, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) throws CommandSyntaxException {
        final ServerPlayerEntity playerArgument = EntityArgumentType.getPlayer(context, "player");
        final boolean playerIsCommander = playerArgument.equals(context.getSource().getPlayer());
        
        if (!isServerConsoleOutput) {
            GameProfile playerProfile = context.getSource().getPlayer().getGameProfile();
            MinecraftServer server = context.getSource().getServer();
            
            if (MCServerAnalytics.getInstance().getConfigProperty("pingCommandRequiresOpToPingOthers").equals("true")
                && !playerIsCommander
                && server.getPermissionLevel(playerProfile) != server.getOpPermissionLevel()) {
                sendErrorOutput(context, "pinging others requires op");
                return 0;
            }
        }

        sendOutput(context, buildOutput(playerArgument, playerIsCommander, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    private String buildOutput(final ServerPlayerEntity playerArgument, final boolean playerIsCommander, final boolean isServerConsoleOutput) {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder(isServerConsoleOutput);
        
        if (playerIsCommander) {
            outputBuilder.append("Your ", CommandOutputBuilder.Color.GOLD);
        } else {
            outputBuilder.append(playerArgument.getEntityName(), CommandOutputBuilder.Color.GOLD);
            outputBuilder.append("'s ");
        }

        outputBuilder.append("Ping", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append(": ");

        outputBuilder.rateByLowerBoundAndAppend(playerArgument.pingMilliseconds, 50, 150, 300, true);
        outputBuilder.append("ms");

        return outputBuilder.toString();
    }
}