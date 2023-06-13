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
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess registryAccess,
                         RegistrationEnvironment registrationEnvironment) {
        System.out.println("registered");
        dispatcher.register(CommandManager.literal("ping")
            .executes(this::defaultRun)
            .then(CommandManager.argument("player", EntityArgumentType.players())
            .executes(this::run)));
    }

    @Override
    protected int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        if (player.equals(context.getSource().getPlayer())) {
            context.getSource().sendMessage(Text.literal("Your ping: " + context.getSource().getPlayer().pingMilliseconds));
        }
        else {
            context.getSource().sendMessage(Text.literal(player.getEntityName() + "'s ping: " + player.pingMilliseconds));
        }
        return 1;
    }

    private int defaultRun(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()){
            context.getSource().sendError(Text.literal("Invalid command usage"));
            return 0;
        }

        context.getSource().sendMessage(Text.literal("Your ping: " + context.getSource().getPlayer().pingMilliseconds));
        return 1;
    }
}