package com.danieltebor.mc_server_analytics.command;

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.extension.MinecraftServerTPSExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class TPSCommand extends MCServerAnalyticsCommand {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("tps").executes(this::run));
    }

    @Override
    protected int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
        MinecraftServerTPSExtension tpsExtension = MCServerAnalytics.getInstance().getTPSExtension();
        String tpsInfo = "TPS | 5s: " + tpsExtension.formatTPS(tpsExtension.getTPS5s())
                        + " | 10s: " + tpsExtension.formatTPS(tpsExtension.getTPS10s())
                        + " | 1m: " + tpsExtension.formatTPS(tpsExtension.getTPS1m())
                        + " | 5m: " + tpsExtension.formatTPS(tpsExtension.getTPS5m()) + " |";

        context.getSource().sendMessage(Text.literal(tpsInfo));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return 1;
    }
}
