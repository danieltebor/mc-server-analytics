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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Daniel Tebor
 */
public final class PingAvgCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "ping-avg";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows avg player ping";

    public PingAvgCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder();
    }
    
    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("Average Ping", 
            CommandOutputBuilder.Color.AQUA, isServerConsoleOutput);
        final List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
            
        outputBuilder.append(": ");
        appendOutput(outputBuilder, players);

        sendOutput(context, outputBuilder.toString(), isServerConsoleOutput);
        return 1;
    }

    protected static void appendOutput(final CommandOutputBuilder outputBuilder, final List<ServerPlayerEntity> players) {
        final List<Integer> playerPings = new ArrayList<>(players.size());
        players.forEach((player) -> playerPings.add(player.pingMilliseconds));
        final float avgPing;
        if (playerPings.size() > 0) {
            avgPing = (float) playerPings.stream().reduce(0, ((pingA, pingB) -> pingA + pingB)) / playerPings.size();
        } else {
            avgPing = 0;
        }
        
        outputBuilder.rateByLowerBoundAndAppend(avgPing, 50, 150, 300, false);
        outputBuilder.append("ms");
    }
}