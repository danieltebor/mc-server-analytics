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
import com.danieltebor.mc_server_analytics.accessor.MinecraftServerAccessor;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import java.util.AbstractMap;
import java.util.stream.Stream;

import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Daniel Tebor
 */
public final class TPSCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "tps";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows avg server TPS for 5s, 15s, 1m, 5m, and 15m";

    public TPSCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder();
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("TPS", 
            CommandOutputBuilder.Color.AQUA, isServerConsoleOutput);
        
        appendOutput(outputBuilder, CommandOutputBuilder.Color.GOLD);

        sendOutput(context, outputBuilder.toString(), isServerConsoleOutput);
        return 1;
    }

    protected static void appendOutput(final CommandOutputBuilder outputBuilder, CommandOutputBuilder.Color labelColor) {
        final MinecraftServerAccessor tickInfoAccessor = (MinecraftServerAccessor) MCServerAnalytics.getInstance().getServer();

        Stream.of(
            new AbstractMap.SimpleImmutableEntry<String, Float>("5s", tickInfoAccessor.getTPS5s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15s", tickInfoAccessor.getTPS15s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("1m", tickInfoAccessor.getTPS1m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("5m", tickInfoAccessor.getTPS5m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15m", tickInfoAccessor.getTPS15m())
        ).forEach((tpsInfo) -> {
            outputBuilder.append(" | ");
            outputBuilder.append(tpsInfo.getKey(), labelColor);
            outputBuilder.append(": ");

            outputBuilder.rateByUpperBoundAndAppend(tpsInfo.getValue(), 18, 12, 6, false);
        });
    }
}