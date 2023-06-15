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
public class HelpCommand extends MCServerAnalyticsCommand {

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("mcsa-help")
            .executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        boolean shouldFormatColor = context.getSource().isExecutedByPlayer();
        
        StringBuilder helpInfo = new StringBuilder(
            shouldFormatColor
                ? Formatter.formatColor("        MC-SA Help", Formatter.Color.AQUA)
                : "\n        MC-SA Help");
        helpInfo.append("\n======================");

        helpInfo.append(buildFormattedHelpInfo("cpu",
            null,
            "Shows cpu thread load, overall load, and temperature",
            shouldFormatColor));
        
        String[] memCommandNames = {
            "mem",
            "ram"
        };
        helpInfo.append(buildFormattedHelpInfo(memCommandNames,
            null,
            "Shows server memory usage",
            shouldFormatColor));

        String[] pingCommandArgs = {
            "player"
        };
        helpInfo.append(buildFormattedHelpInfo("ping",
            pingCommandArgs,
            "Shows your ping or ping of specified player",
            shouldFormatColor));

        helpInfo.append(buildFormattedHelpInfo("tps",
            null,
            "Shows avg server TPS for 5s, 15s, 1m, 5m, and 15m",
            shouldFormatColor));

        context.getSource().sendMessage(Text.literal(helpInfo.toString()));
        return 1;
    }

    private String buildFormattedHelpInfo(final String name, final String[] args, final String description, final boolean shouldFormatColor) {
        StringBuilder formattedHelpInfo = new StringBuilder("\n/");
        
        formattedHelpInfo.append(
            shouldFormatColor
                ? Formatter.formatColor(name, Formatter.Color.GOLD)
                : name);

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                formattedHelpInfo.append(" <");
                formattedHelpInfo.append(
                    shouldFormatColor
                        ? Formatter.formatColor(args[i], Formatter.Color.LIGHT_PURPLE)
                        : args[i]);
                formattedHelpInfo.append(">");
            }
        }
        formattedHelpInfo.append(" - ");

        formattedHelpInfo.append(
            shouldFormatColor
                ? Formatter.formatColor(description, Formatter.Color.BLUE)
                : description);
        
        return formattedHelpInfo.toString();
    }

    private String buildFormattedHelpInfo(final String[] names, final String[] args, final String description, final boolean shouldFormatColor) {
        StringBuilder formattedHelpInfo = new StringBuilder("\n");
        
        for (int i = 0; i < names.length - 1; i++) {
            formattedHelpInfo.append("/");
            formattedHelpInfo.append(
                shouldFormatColor
                    ? Formatter.formatColor(names[i], Formatter.Color.GOLD)
                    : names[i]);
            formattedHelpInfo.append(" or ");
        }
        formattedHelpInfo.append("/");
        formattedHelpInfo.append(
            shouldFormatColor
                ? Formatter.formatColor(names[names.length - 1], Formatter.Color.GOLD)
                : names[names.length - 1]);

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                formattedHelpInfo.append(" <");
                formattedHelpInfo.append(
                    shouldFormatColor
                        ? Formatter.formatColor(args[i], Formatter.Color.LIGHT_PURPLE)
                        : args[i]);
                formattedHelpInfo.append(">");
            }
        }
        formattedHelpInfo.append(" - ");

        formattedHelpInfo.append(
            shouldFormatColor
                ? Formatter.formatColor(description, Formatter.Color.BLUE)
                : description);
                
        return formattedHelpInfo.toString();
    }
}