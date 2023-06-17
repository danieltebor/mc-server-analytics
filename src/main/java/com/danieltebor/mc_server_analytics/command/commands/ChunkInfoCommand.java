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
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class ChunkInfoCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "chunk-info";
    public static final String[][] ARG_NAMES = {{"dimension"}};
    public static final String DESCRIPTION = "Shows loaded chunks";

    public ChunkInfoCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("chunk-info")
            .executes(this::executeDefault)
            .then(CommandManager.argument(ARG_NAMES[0][0], DimensionArgumentType.dimension())
            .executes(this::executeParameterized)));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();

        context.getSource().sendMessage(Text.literal(
            buildOutput(server, null, isServerConsoleOutput)));
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final String dimArgument = DimensionArgumentType.getDimensionArgument(context, ARG_NAMES[0][0])
            .getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        
        context.getSource().sendMessage(Text.literal(
            buildOutput(server, dimArgument, isServerConsoleOutput)));
        
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
        }
        else {
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
            } catch(Exception e) {}
            final int chunksLoadedCount = world.getChunkManager().getTotalChunksLoadedCount();
            final int chunksForceLoadedCount = world.getForcedChunks().size();

            chunksLoadedSums[0] += chunksLoadedCount;
            chunksLoadedSums[1] += chunksForceLoadedCount;

            if (dimArgument == null) {
                outputBuilder.append("\n");
            }

            if (dimArgument != null || chunksLoadedCount + chunksForceLoadedCount != 0) {
                appendChunksLoadedSegment(outputBuilder, dimName, chunksLoadedCount, chunksForceLoadedCount);
                dimsWithLoadedChunksCount[0] += 1;
            }
            else {
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
                                           final int chunksLoadedCount,
                                           final int chunksForceLoadedCount) {
        outputBuilder.append(label, CommandOutputBuilder.Color.GOLD);
        outputBuilder.append("\n| ");

        outputBuilder.append("Loaded", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(chunksLoadedCount, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("\n| ");

        outputBuilder.append("Force-loaded", CommandOutputBuilder.Color.DARK_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(chunksForceLoadedCount, CommandOutputBuilder.Color.BLUE);
    }
}