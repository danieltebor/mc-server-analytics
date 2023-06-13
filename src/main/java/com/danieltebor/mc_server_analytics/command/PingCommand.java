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

public class PingCommand extends MCServerAnalyticsCommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess registryAccess,
                         RegistrationEnvironment registrationEnvironment) {
        System.out.println("registered");
        dispatcher.register(CommandManager.literal("ping")
            .then(CommandManager.argument("player", EntityArgumentType.players()).executes(this::run)));
    }

    @Override
    protected int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        context.getSource().sendMessage(Text.literal(player.getEntityName() + "'s ping: " + player.pingMilliseconds));
        return 1;
    }
}