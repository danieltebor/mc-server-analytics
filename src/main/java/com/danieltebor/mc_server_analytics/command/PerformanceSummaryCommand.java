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
import com.danieltebor.mc_server_analytics.accessor.ServerChunkManagerAccessor;
import com.danieltebor.mc_server_analytics.tracker.CPUInfoTracker;
import com.danieltebor.mc_server_analytics.tracker.WorldFileInfoTracker;
import com.danieltebor.mc_server_analytics.util.MemInfo;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.chunk.WorldChunk;

/**
 * @author Daniel Tebor
 */
public final class PerformanceSummaryCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "perf-sum";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows summary of server telemetry";

    public PerformanceSummaryCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder();
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("\n", 
            CommandOutputBuilder.Color.AQUA, isServerConsoleOutput);

        outputBuilder.append(isServerConsoleOutput ? "Performance Summary" : "     Performance Summary", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append("\n=========================\n");

        // Chunks loaded & entity count.
        final int[] chunksLoadedSum = {0};
        final int[] entitySums = {0, 0};
        
        context.getSource().getServer().getWorlds().forEach((world) -> {
            final int[] loadedChunkInfo = {0, 0};
            ((ServerChunkManagerAccessor) world.getChunkManager()).getChunkHolderEntryIterator().forEach((chunkHolder) -> {
                WorldChunk worldChunk = chunkHolder.getWorldChunk();
                if (worldChunk != null) {
                    loadedChunkInfo[0]++;
                    loadedChunkInfo[1] = worldChunk.countVerticalSections();
                }
            });
            if (loadedChunkInfo[1] != 0){
                loadedChunkInfo[0] = loadedChunkInfo[0] / loadedChunkInfo[1];
            }
            chunksLoadedSum[0] += loadedChunkInfo[0];
 
            entitySums[0] += world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), entity -> true).size();
            entitySums[1] += world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), entity -> true).size();
        });

        outputBuilder.append("Loaded Chunks", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        outputBuilder.append(chunksLoadedSum[0], CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("\n");
        
        outputBuilder.append("Entities", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        outputBuilder.append("Mobs", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(entitySums[0], CommandOutputBuilder.Color.BLUE);
        outputBuilder.append(" | ");

        outputBuilder.append("Items", CommandOutputBuilder.Color.DARK_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(entitySums[1], CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("\n");

        // Avg player ping.
        final List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
        
        outputBuilder.append("Average Ping", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        PingAvgCommand.appendOutput(outputBuilder, players);
        outputBuilder.append("\n");

        // TPS.
        outputBuilder.append("TPS", CommandOutputBuilder.Color.GOLD);

        TPSCommand.appendOutput(outputBuilder, CommandOutputBuilder.Color.DARK_AQUA);
        outputBuilder.append("\n");

        // MSPT.
        outputBuilder.append("MSPT", CommandOutputBuilder.Color.GOLD);

        MSPTCommand.appendOutput(outputBuilder, CommandOutputBuilder.Color.DARK_AQUA);
        outputBuilder.append("\n");

        // CPU load.
        final CPUInfoTracker cpuInfoTracker = MCServerAnalytics.getInstance().getCpuInfoTracker();
        final double cpuLoad = cpuInfoTracker.getOverallLoad();
        final OptionalDouble maxThreadLoad = Arrays.stream(cpuInfoTracker.getThreadLoads()).max();

        outputBuilder.append("CPU Load", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append("\n| ");

        if (cpuLoad != -100 && maxThreadLoad.isPresent()) {
            if (!isServerConsoleOutput) {
                outputBuilder.buildUtilizationBarAndAppend(cpuLoad, 0, 100, 40, CommandOutputBuilder.Color.LIGHT_PURPLE);
                outputBuilder.append("\n| ");
            }

            outputBuilder.append("Overall Load", CommandOutputBuilder.Color.LIGHT_PURPLE);
            outputBuilder.append(": ");

            outputBuilder.rateByLowerBoundAndAppend(cpuLoad, 90, 95, 98, false);
            outputBuilder.append("%\n| ");

            outputBuilder.append("Max Thread Load", CommandOutputBuilder.Color.DARK_PURPLE);
            outputBuilder.append(": ");

            outputBuilder.rateByLowerBoundAndAppend(maxThreadLoad.getAsDouble() * 100, 90, 95, 98, false);
            outputBuilder.append("%\n");
        }
        else {
            outputBuilder.append("UNAVAILABLE", CommandOutputBuilder.Color.DARK_RED);
            outputBuilder.append("%\n");
        }

        // Memory usage.
        final long usedMemory = MemInfo.toMB(MemInfo.getTotalUsedMemory());
        final long committedMemory = MemInfo.toMB(MemInfo.getTotalCommittedMemory());
        final long maxMemory = MemInfo.toMB(MemInfo.getTotalMaxMemory());

        MEMCommand.appendMemoryUsageSegment(outputBuilder, "Used Memory", 
            usedMemory, committedMemory, maxMemory, true, isServerConsoleOutput);

        // World size.
        
        final WorldFileInfoTracker worldFileInfoTracker = MCServerAnalytics.getInstance().getWorldFileInfoTracker();
        
        outputBuilder.append("World Size", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        if (!worldFileInfoTracker.isAlive() && worldFileInfoTracker.getWorldSizeBytes() == -1) {
            outputBuilder.append("UNAVAILABLE", CommandOutputBuilder.Color.DARK_RED);
        }
        else {
            WorldSizeCommand.appendOutput(outputBuilder, worldFileInfoTracker);
        }

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }
}