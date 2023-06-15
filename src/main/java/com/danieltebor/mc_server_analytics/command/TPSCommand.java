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
import com.danieltebor.mc_server_analytics.extension.MinecraftServerTPSExtension;
import com.danieltebor.mc_server_analytics.util.Formatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.stream.Stream;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class TPSCommand extends MCServerAnalyticsCommand {
    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("tps").executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServerTPSExtension tpsExtension = MCServerAnalytics.getInstance().getTPSExtension();
        ArrayList<String> formattedTPSList = new ArrayList<>(5);
        boolean shouldFormatColor = context.getSource().isExecutedByPlayer();

        Stream.of(
            new AbstractMap.SimpleEntry<String, Float>("5s", tpsExtension.getTPS5s()),
            new AbstractMap.SimpleEntry<String, Float>("15s", tpsExtension.getTPS15s()),
            new AbstractMap.SimpleEntry<String, Float>("1m", tpsExtension.getTPS1m()),
            new AbstractMap.SimpleEntry<String, Float>("5m", tpsExtension.getTPS5m()),
            new AbstractMap.SimpleEntry<String, Float>("15m", tpsExtension.getTPS15m())
        ).forEach((tpsInfo) -> formattedTPSList.add(buildFormattedTPS(tpsInfo, shouldFormatColor)));

        StringBuilder formattedTPSInfo = new StringBuilder(
            shouldFormatColor
                ? Formatter.formatColor("TPS", Formatter.Color.AQUA)
                : "TPS");
        formattedTPSList.stream().forEach(formattedTPSInfo::append);

        context.getSource().sendMessage(Text.literal(formattedTPSInfo.toString()));
        return 1;
    }

    private String buildFormattedTPS(final AbstractMap.SimpleEntry<String, Float> tpsInfo, final boolean shouldFormatColor) {
        StringBuilder formattedTPS = new StringBuilder(" | ");

        formattedTPS.append(
            shouldFormatColor
                ? Formatter.formatColor(tpsInfo.getKey(), Formatter.Color.GOLD)
                : tpsInfo.getKey());
        formattedTPS.append(": ");

        formattedTPS.append(
            shouldFormatColor
                ? Formatter.formatColor(Formatter.formatDecimal(tpsInfo.getValue()),
                    Formatter.rateNumByUpperBound(tpsInfo.getValue(), 18, 12, 6))
                : Formatter.formatDecimal(tpsInfo.getValue()));

        return formattedTPS.toString();
    }

    
}