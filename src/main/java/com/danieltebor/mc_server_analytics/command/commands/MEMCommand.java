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
import com.danieltebor.mc_server_analytics.util.MemInfo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class MEMCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "mem";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows server memory usage";

    public MEMCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("mem")
            .executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("mem command is not available in server console"));
            return 0;
        }

        final long maxMemory = MemInfo.toMB(MemInfo.getMaxMemory());

        final long usedHeapMemory = MemInfo.toMB(MemInfo.getUsedHeapMemory());
        final long committedHeapMemory = MemInfo.toMB(MemInfo.getCommittedHeapMemory());

        final long usedNonHeapMemory = MemInfo.toMB(MemInfo.getUsedNonHeapMemory());
        final long committedNonHeapMemory = MemInfo.toMB(MemInfo.getCommittedNonHeapMemory());

        final long usedTotalMemory = MemInfo.toMB(MemInfo.getTotalUsedMemory());
        final long committedTotalMemory = MemInfo.toMB(MemInfo.getTotalCommittedMemory());

        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("\n", false);
        
        outputBuilder.append("        Memory Info", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append("\n=====================\n");
        
        appendMemoryUsageSegment(outputBuilder, "Heap",
            usedHeapMemory, committedHeapMemory, maxMemory, false);

        appendMemoryUsageSegment(outputBuilder, "Non-heap",
            usedNonHeapMemory, committedNonHeapMemory, maxMemory, false);

        appendMemoryUsageSegment(outputBuilder, "Total",
            usedTotalMemory, committedTotalMemory, maxMemory, false);

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }

    public static void appendMemoryUsageSegment(final CommandOutputBuilder outputBuilder, final String memoryType,
                                          final long usedMemory, final long committedMemory, final long maxMemory, final boolean isServerConsoleOutput) {
        final List<Double> memoryUtilizations = Arrays.asList(
            (double) usedMemory, (double) committedMemory);

        final List<CommandOutputBuilder.Color> colors = Arrays.asList(
            CommandOutputBuilder.Color.LIGHT_PURPLE, CommandOutputBuilder.Color.DARK_PURPLE);
        
        outputBuilder.append(memoryType, CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(" Memory", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append("\n| ");                                       

        if (!isServerConsoleOutput) {
            outputBuilder.buildUtilizationBarAndAppend(memoryUtilizations,
                0, (int) maxMemory, (int) maxMemory / 40, colors);
            outputBuilder.append("\n| ");
        }

        appendLabeledMemoryUsage(outputBuilder, "Used", colors.get(0),
            usedMemory, maxMemory);
        outputBuilder.append("\n| ");

        appendLabeledMemoryUsage(outputBuilder, "Committed", colors.get(1),
            committedMemory, maxMemory);
        outputBuilder.append("\n");
    }

    private static void appendLabeledMemoryUsage(final CommandOutputBuilder outputBuilder, final String label, final CommandOutputBuilder.Color color,
                                          final long memoryUtilization, final long maxMemory) {
        outputBuilder.append(label, color);
        outputBuilder.append(": ");

        outputBuilder.rateByLowerBoundAndAppend(
            memoryUtilization,
            maxMemory - 3 * (maxMemory / 10),
            maxMemory - 2 * (maxMemory / 10),
            maxMemory - maxMemory / 10,
            false, true);
        outputBuilder.append("MB/");

        outputBuilder.append(maxMemory, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("MB");
    }
}