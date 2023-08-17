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
import com.danieltebor.mc_server_analytics.tracker.WorldFileInfoTracker;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public class WorldSizeCommand extends MCServerAnalyticsCommand {
    public static final String NAME = "world-size";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows world file size";

    public WorldSizeCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder();
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final WorldFileInfoTracker worldFileInfoTracker = MCServerAnalytics.getInstance().getWorldFileInfoTracker();
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("World Size", 
            CommandOutputBuilder.Color.AQUA, isServerConsoleOutput);

        if (!worldFileInfoTracker.isAlive() && worldFileInfoTracker.getWorldSizeBytes() == -1) {
            context.getSource().sendError(Text.literal("World size is unavailable"));
            return 0;
        }

        appendOutput(outputBuilder, worldFileInfoTracker);

        sendOutput(context, outputBuilder.toString(), isServerConsoleOutput);
        return 1;
    }

    protected static void appendOutput(final CommandOutputBuilder outputBuilder, final WorldFileInfoTracker worldFileInfoTracker) {
        outputBuilder.append(" | ");
        outputBuilder.append((int) worldFileInfoTracker.getWorldSizeMB(), CommandOutputBuilder.Color.BLUE);
        
        outputBuilder.append("MB (");
        outputBuilder.append(worldFileInfoTracker.getWorldSizeGB(), true, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("GB)");

        if (!worldFileInfoTracker.isAlive()) {
            outputBuilder.append(" *NOT ACTIVELY TRACKING", CommandOutputBuilder.Color.DARK_RED);
        }
    }
}
