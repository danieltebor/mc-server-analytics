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

import com.danieltebor.mc_server_analytics.util.Formatter;
import com.danieltebor.mc_server_analytics.util.MemInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class MEMCommand extends MCServerAnalyticsCommand {
    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        Stream.of(
            "mem",
            "ram"
        ).forEach((name) -> {
            dispatcher.register(CommandManager.literal(name)
                .executes(this::executeDefault));
        });
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("mem command is not available in server console"));
            return 0;
        }

        try {
        StringBuilder memUsageInfo = new StringBuilder(
            Formatter.formatColor("        Memory Usage", Formatter.Color.AQUA));
        memUsageInfo.append("\n======================\n");
        
        long maxMemory = MemInfo.toMB(MemInfo.getMaxMemory());

        long usedHeapMemory = MemInfo.toMB(MemInfo.getUsedHeapMemory());
        long committedHeapMemory = MemInfo.toMB(MemInfo.getCommittedHeapMemory());
        
        memUsageInfo.append(buildFormattedMemoryInfo(
            usedHeapMemory, committedHeapMemory, maxMemory, "Heap"));

        long usedNonHeapMemory = MemInfo.toMB(MemInfo.getUsedNonHeapMemory());
        long committedNonHeapMemory = MemInfo.toMB(MemInfo.getCommittedNonHeapMemory());

        memUsageInfo.append(buildFormattedMemoryInfo(
            usedNonHeapMemory, committedNonHeapMemory, maxMemory, "Non-heap"));

        long usedTotalMemory = MemInfo.toMB(MemInfo.getTotalUsedMemory());
        long committedTotalMemory = MemInfo.toMB(MemInfo.getTotalCommittedMemory());

        memUsageInfo.append(buildFormattedMemoryInfo(
            usedTotalMemory, committedTotalMemory, maxMemory, "Total"));

        context.getSource().sendMessage(Text.literal(memUsageInfo.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private String buildFormattedMemoryInfo(final long usedMemory, final long committedMemory, final long maxMemory,
                                            final String memoryType) {
        StringBuilder formattedMemInfo = new StringBuilder();

        List<Double> memoryUtilizations = new ArrayList<>(2);
        memoryUtilizations.add((double) usedMemory);
        memoryUtilizations.add((double) committedMemory); 

        List<Formatter.Color> colors = new ArrayList<>(2);
        colors.add(Formatter.Color.LIGHT_PURPLE);
        colors.add(Formatter.Color.DARK_PURPLE);
        
        formattedMemInfo.append(Formatter.formatColor(memoryType + " Memory", Formatter.Color.GOLD));
        formattedMemInfo.append("\n| ");                                       

        formattedMemInfo.append(Formatter.buildUtilizationBar(memoryUtilizations,
            0, (int) maxMemory, (int) maxMemory / 40, colors, true));
        formattedMemInfo.append("\n| ");

        formattedMemInfo.append(buildFormattedMemoryUtilization("Used", colors.get(0),
            usedMemory, maxMemory));
        formattedMemInfo.append("\n| ");

        formattedMemInfo.append(buildFormattedMemoryUtilization("Committed", colors.get(1),
            committedMemory, maxMemory));
        formattedMemInfo.append("\n");

        return formattedMemInfo.toString();
    }

    private String buildFormattedMemoryUtilization(final String memoryName, final Formatter.Color color,
                                                   final long memoryUtilization, final long maxMemory) {
        StringBuilder formattedMemUtilization = new StringBuilder(Formatter.formatColor(memoryName, color));
        formattedMemUtilization.append(": ");

        formattedMemUtilization.append(Formatter.formatColor(
            String.valueOf(memoryUtilization),
            Formatter.rateNumByLowerBound(memoryUtilization,
                maxMemory - 3 * (maxMemory / 10),
                maxMemory - 2 * (maxMemory / 10),
                maxMemory - maxMemory / 10)));
        formattedMemUtilization.append("MB/");

        formattedMemUtilization.append(Formatter.formatColor(String.valueOf(maxMemory),
            Formatter.Color.BLUE));
        formattedMemUtilization.append("MB");

        return formattedMemUtilization.toString();
    }
}