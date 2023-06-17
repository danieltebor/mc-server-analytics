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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class HelpCommand extends MCServerAnalyticsCommand {
    public static final String NAME = "mcsa-help";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "NaN";

    public HelpCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("mcsa-help")
            .executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("\n", isServerConsoleOutput);

        outputBuilder.append(isServerConsoleOutput ? "MC" : "        MC",
            CommandOutputBuilder.Color.GREEN);
        outputBuilder.append("-");
        outputBuilder.append("SA ", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append("Help", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append("\n====================");

        MCServerAnalytics.getInstance().getRegisteredCommands().forEach((command) -> {
            if (command.getName().equals(NAME)) {
                return;
            }

            appendCommandInfoSegment(outputBuilder, command);
        });

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }

    private void appendCommandInfoSegment(final CommandOutputBuilder outputBuilder, final MCServerAnalyticsCommand command) {
        if (command.getName().length() == 0) {
            throw new IllegalArgumentException("name must have a length greater than 0");
        }
        else if (getDescription().length() == 0) {
            throw new IllegalAccessError("description must have a length greater than 0");
        }
          
        outputBuilder.append("\n/");
        outputBuilder.append(command.getName(), CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(" ");
        
        for (int j = 0; j < command.getArgNames().length; j++) {
            outputBuilder.append("<");
            for (int k = 0; k < command.getArgNames()[j].length - 1; k++) {
                outputBuilder.append(command.getArgNames()[j][k], CommandOutputBuilder.Color.DARK_AQUA);
                outputBuilder.append(" | ");
            }
            outputBuilder.append(
                command.getArgNames()[j][command.getArgNames()[j].length - 1],
                CommandOutputBuilder.Color.DARK_AQUA);
            outputBuilder.append("> ");
        }
        outputBuilder.append("- ");

        outputBuilder.append(command.getDescription(), CommandOutputBuilder.Color.BLUE);
    }
}