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

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.command.CommandOutputBuilder;
import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;
import com.danieltebor.mc_server_analytics.extension.MinecraftServerTPSExtension;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.AbstractMap;
import java.util.stream.Stream;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("tps").executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("TPS", 
            CommandOutputBuilder.Color.AQUA, !context.getSource().isExecutedByPlayer());
        
        appendOutput(outputBuilder, CommandOutputBuilder.Color.GOLD);

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }

    public static void appendOutput(final CommandOutputBuilder outputBuilder, CommandOutputBuilder.Color labelColor) {
        final MinecraftServerTPSExtension tpsExtension = MCServerAnalytics.getInstance().getTPSExtension();

        Stream.of(
            new AbstractMap.SimpleImmutableEntry<String, Float>("5s", tpsExtension.getTPS5s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15s", tpsExtension.getTPS15s()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("1m", tpsExtension.getTPS1m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("5m", tpsExtension.getTPS5m()),
            new AbstractMap.SimpleImmutableEntry<String, Float>("15m", tpsExtension.getTPS15m())
        ).forEach((tpsInfo) -> {
            outputBuilder.append(" | ");
            outputBuilder.append(tpsInfo.getKey(), labelColor);
            outputBuilder.append(": ");

            outputBuilder.rateByUpperBoundAndAppend(tpsInfo.getValue(), 18, 12, 6, true, false);
        });
    }
}