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

package com.danieltebor.mc_server_analytics.command;

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.accessor.MinecraftServerAccessor;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.AbstractMap;
import java.util.stream.Stream;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public class MSPTCommand extends MCServerAnalyticsCommand {

    public static String NAME = "mspt";
    public static String[][] ARG_NAMES = {};
    public static String DESCRITPION = "Shows milliseconds per tick (mspt)";

    MSPTCommand() {
        super(NAME, ARG_NAMES, DESCRITPION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder();
    }
    
    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("MSPT", 
            CommandOutputBuilder.Color.AQUA, !context.getSource().isExecutedByPlayer());
        
        appendOutput(outputBuilder, CommandOutputBuilder.Color.GOLD);

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }

    protected static void appendOutput(final CommandOutputBuilder outputBuilder, CommandOutputBuilder.Color labelColor) {
        final MinecraftServerAccessor tickInfoAccessor = (MinecraftServerAccessor) MCServerAnalytics.getInstance().getServer();

        Stream.of(
            new AbstractMap.SimpleImmutableEntry<String, Float>("5s", tickInfoAccessor.getMSPT5s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15s", tickInfoAccessor.getMSPT15s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("1m", tickInfoAccessor.getMSPT1m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("5m", tickInfoAccessor.getMSPT5m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15m", tickInfoAccessor.getMSPT15m())
        ).forEach((msptInfo) -> {
            outputBuilder.append(" | ");
            outputBuilder.append(msptInfo.getKey(), labelColor);
            outputBuilder.append(": ");

            outputBuilder.rateByLowerBoundAndAppend(msptInfo.getValue(), 40, 45, 50, false);
        });
    }
}
