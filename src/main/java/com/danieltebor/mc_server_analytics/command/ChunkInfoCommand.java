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

import com.danieltebor.mc_server_analytics.accessor.ServerChunkManagerAccessor;
import com.danieltebor.mc_server_analytics.util.LoggerUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.chunk.WorldChunk;

/**
 * @author Daniel Tebor
 */
public final class ChunkInfoCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "chunk-info";
    public static final String[][] ARG_NAMES = {{"dimension"}};
    public static final String DESCRIPTION = "Shows loaded chunks";

    ChunkInfoCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder()
            .then(CommandManager.argument(ARG_NAMES[0][0], DimensionArgumentType.dimension())
            .executes(this::executeParameterizedWrapper));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final MinecraftServer server = context.getSource().getServer();

        sendOutput(context, buildOutput(server, null, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final MinecraftServer server = context.getSource().getServer();
        String dimArgument;
        try {
            dimArgument = DimensionArgumentType.getDimensionArgument(context, ARG_NAMES[0][0])
                .getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
        }
        catch (CommandSyntaxException e) {
            return 0;
        }
        
        sendOutput(context, buildOutput(server, dimArgument, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    private String buildOutput(final MinecraftServer server, final String dimArgument, final boolean isServerConsoleOutput) {
        final int[] chunksLoadedSums = {0, 0};
        final int[] dimsWithLoadedChunksCount = {0};
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder(isServerConsoleOutput);

        if (dimArgument == null) {
            outputBuilder.append("\n");
            outputBuilder.append(isServerConsoleOutput ? "Chunk Info" : "      Chunk Info", 
                CommandOutputBuilder.Color.AQUA);
            outputBuilder.append("\n=================");
        } else {
            if (isServerConsoleOutput) {
                outputBuilder.append("\n");
            }
            outputBuilder.append("Chunk Info", CommandOutputBuilder.Color.AQUA);
            outputBuilder.append(" - ");
        }

        server.getWorlds().forEach((world) -> {
            String dimName = world.getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
            
            if (dimArgument != null && !dimArgument.equals(dimName)) {
                return;
            }
            
            try {
                dimName = outputBuilder.formatSnakeCase(dimName);
            } catch(Exception e) {
                LoggerUtil.sendInfo("An unexpected error occured formatting dimension name for " + NAME + " command. Using unformatted version", true);
            }

            final int[] loadedChunkInfo = {0, 0};
            ((ServerChunkManagerAccessor) world.getChunkManager()).getChunkHolderEntryIterator().forEach((chunkHolder) -> {
                WorldChunk worldChunk = chunkHolder.getWorldChunk();
                if (worldChunk != null) {
                    loadedChunkInfo[0]++;
                    loadedChunkInfo[1] = worldChunk.countVerticalSections();
                }
            });
            if (loadedChunkInfo[1] != 0) {
                loadedChunkInfo[0] = loadedChunkInfo[0] / loadedChunkInfo[1];
            }
            final int forceLoadedChunkCount = world.getForcedChunks().size();

            chunksLoadedSums[0] += loadedChunkInfo[0];
            chunksLoadedSums[1] += forceLoadedChunkCount;

            if (dimArgument == null) {
                outputBuilder.append("\n");
            }

            if (dimArgument != null || loadedChunkInfo[0] + forceLoadedChunkCount != 0) {
                appendChunksLoadedSegment(outputBuilder, dimName, loadedChunkInfo[0], forceLoadedChunkCount);
                dimsWithLoadedChunksCount[0] += 1;
            } else {
                outputBuilder.append(dimName, CommandOutputBuilder.Color.GOLD);
                outputBuilder.append("\n| ");
                outputBuilder.append("None Loaded", CommandOutputBuilder.Color.DARK_AQUA);
            }
        });
        
        if (dimArgument == null && dimsWithLoadedChunksCount[0] > 1) {
            outputBuilder.append("\n");
            appendChunksLoadedSegment(outputBuilder, "All Dimensions", chunksLoadedSums[0], chunksLoadedSums[1]);
        }

        return outputBuilder.toString();
    }

    private void appendChunksLoadedSegment(final CommandOutputBuilder outputBuilder,
                                           final String label,
                                           final int loadedChunkCount,
                                           final int forceLoadedChunkCount) {
        outputBuilder.append(label, CommandOutputBuilder.Color.GOLD);
        outputBuilder.append("\n| ");

        outputBuilder.append("Loaded", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(loadedChunkCount, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("\n| ");

        outputBuilder.append("Force-loaded", CommandOutputBuilder.Color.DARK_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(forceLoadedChunkCount, CommandOutputBuilder.Color.BLUE);
    }
}