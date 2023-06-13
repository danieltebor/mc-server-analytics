package com.danieltebor.mc_server_analytics.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import static net.minecraft.server.command.CommandManager.literal;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TPSCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("ups").executes(TPSCommand::run));
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var upsWrapper = new Object() { float value = 100 / context.getSource().getServer().getTickTime(); };
        context.getSource().sendMessage(Text.literal(String.format("UPS: %.2f", upsWrapper.value)));
        return 1;
    }
}
