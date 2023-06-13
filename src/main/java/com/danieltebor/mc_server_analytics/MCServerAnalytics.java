package com.danieltebor.mc_server_analytics;

import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;
import com.danieltebor.mc_server_analytics.command.*;
import com.danieltebor.mc_server_analytics.extension.MinecraftServerTPSExtension;
import java.util.stream.Stream;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class MCServerAnalytics implements DedicatedServerModInitializer {
    private static MCServerAnalytics instance;
    
    private MinecraftServer server;
    
    @Override
    public void onInitializeServer() {
        MCServerAnalytics.instance = this;

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> this.server = server);

        Stream.of(
            new PingCommand(),
            new TPSCommand()
        ).forEach(this::registerCommand);
    }

    private void registerCommand(MCServerAnalyticsCommand command) {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> command.register(dispatcher, registryAccess, environment));
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public MinecraftServerTPSExtension getTPSExtension() {
        return (MinecraftServerTPSExtension) this.server;
    }

    public static MCServerAnalytics getInstance() {
        return MCServerAnalytics.instance;
    }
}