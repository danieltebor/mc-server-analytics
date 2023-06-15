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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public final class PingCommand extends MCServerAnalyticsCommand {
    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("ping")
            .executes(this::executeDefault)
            .then(CommandManager.argument("player", EntityArgumentType.players())
            .executes(this::executeParameterized)));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()){
            context.getSource().sendError(Text.literal("Invalid command usage"));
            return 0;
        }
        
        context.getSource().sendMessage(Text.literal(
                buildFormattedPlayerPing(context.getSource().getPlayer(), true, true)));
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        boolean playerIsCommander = player.equals(context.getSource().getPlayer());
        boolean shouldFormatColor = context.getSource().isExecutedByPlayer();
        
        context.getSource().sendMessage(Text.literal(buildFormattedPlayerPing(player, playerIsCommander, shouldFormatColor)));
        return 1;
    }

    private String buildFormattedPlayerPing(final ServerPlayerEntity player, final boolean playerIsCommander, final boolean shouldFormatColor) {
        StringBuilder formattedPlayerPing = new StringBuilder();
        
        if (playerIsCommander) {
            formattedPlayerPing.append(
                shouldFormatColor
                    ? Formatter.formatColor("Your ", Formatter.Color.GOLD)
                    : player.getEntityName());
        }
        else {
            formattedPlayerPing.append(
                shouldFormatColor
                    ? Formatter.formatColor(player.getEntityName(), Formatter.Color.GOLD)
                    : player.getEntityName());
            formattedPlayerPing.append("'s ");
        }

        formattedPlayerPing.append(
            shouldFormatColor
                ? Formatter.formatColor("Ping", Formatter.Color.AQUA)
                : "ping");
        formattedPlayerPing.append(": ");

        formattedPlayerPing.append(
            shouldFormatColor
                ? Formatter.formatColor(String.valueOf(player.pingMilliseconds),
                    Formatter.rateNumByLowerBound(player.pingMilliseconds, 50, 150, 300))
                : player.pingMilliseconds);
        formattedPlayerPing.append("ms");

        return formattedPlayerPing.toString();
    }
}