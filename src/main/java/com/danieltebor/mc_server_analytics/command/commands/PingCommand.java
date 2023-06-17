/*
 * Copyright (C) 2011 - 2020 Olivier Biot
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

package com.danieltebor.mc_server_analytics.command.commands;

import com.danieltebor.mc_server_analytics.command.CommandOutputBuilder;
import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("ping")
            .executes(this::executeDefault)
            .then(CommandManager.argument("player", EntityArgumentType.players())
            .executes(this::executeParameterized)));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        
        if (isServerConsoleOutput) {
            context.getSource().sendError(Text.literal("Invalid command usage"));
            return 0;
        }
        
        context.getSource().sendMessage(Text.literal(
                buildOutput(context.getSource().getPlayer(), true, isServerConsoleOutput)));
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity playerArgument = EntityArgumentType.getPlayer(context, "player");
        final boolean playerIsCommander = playerArgument.equals(context.getSource().getPlayer());
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        
        context.getSource().sendMessage(Text.literal(buildOutput(playerArgument, playerIsCommander, isServerConsoleOutput)));
        return 1;
    }

    private String buildOutput(final ServerPlayerEntity playerArgument, final boolean playerIsCommander, final boolean isServerConsoleOutput) {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder(isServerConsoleOutput);
        
        if (playerIsCommander) {
            outputBuilder.append("Your ", CommandOutputBuilder.Color.GOLD);
        }
        else {
            outputBuilder.append(playerArgument.getEntityName(), CommandOutputBuilder.Color.GOLD);
            outputBuilder.append("'s ");
        }

        outputBuilder.append("Ping", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append(": ");

        outputBuilder.rateByLowerBoundAndAppend(playerArgument.pingMilliseconds, 50, 150, 300, true, false);
        outputBuilder.append("ms");

        return outputBuilder.toString();
    }
}