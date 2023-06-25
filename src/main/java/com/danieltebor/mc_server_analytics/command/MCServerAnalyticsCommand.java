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

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.util.LoggerUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * @author Daniel Tebor
 */
public abstract class MCServerAnalyticsCommand {

    private static final boolean SHOULD_BROADCAST_CONSOLE_TO_OPS;

    static {
        final String serverPath = System.getProperty("user.dir");
        final Path serverPropertiesPath = Paths.get(serverPath, "server.properties");

        boolean shouldBroadcastToOps = false;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(serverPropertiesPath)) {
            properties.load(input);
            shouldBroadcastToOps = Boolean.parseBoolean(properties.getProperty("broadcast-console-to-ops"));
        } catch (IOException e) {
            LoggerUtil.sendError("Unable to read server.properties file", e);
        }

        SHOULD_BROADCAST_CONSOLE_TO_OPS = shouldBroadcastToOps;
    }
    
    private final String name;
    private final String[][] argNames;
    private final String description;
    private final boolean isEnabled;
    private final boolean opIsRequired;

    MCServerAnalyticsCommand(final String name, final String[][] argNames, final String description) {
        if (MCServerAnalytics.getInstance().getConfigProperty(name + "CommandEnabled").equals("true")) {
            isEnabled = true;
        } else {
            isEnabled = false;
        }

        if (MCServerAnalytics.getInstance().getConfigProperty(name + "CommandRequiresOp").equals("true")) {
            opIsRequired = true;
        } else {
            opIsRequired = false;
        }

        this.name = Objects.requireNonNull(name, "name must not be null");
        this.argNames = Objects.requireNonNull(argNames, "argNames must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");

        if (isEnabled) {
            CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(getArgumentBuilderImpl()));
        }
    }

    protected abstract LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl();

    protected final LiteralArgumentBuilder<ServerCommandSource> getDefaultArgumentBuilder() {
        return CommandManager.literal(name).executes(this::executeDefaultWrapper);
    }

    private final boolean shouldExecute(final CommandContext<ServerCommandSource> context) {
        if (context.getSource().isExecutedByPlayer() && opIsRequired) {
            GameProfile playerProfile = context.getSource().getPlayer().getGameProfile();
            MinecraftServer server = context.getSource().getServer();
            
            if (server.getPermissionLevel(playerProfile) == server.getOpPermissionLevel()) {
                return true;
            }

            sendErrorOutput(context, name + " command requires op");
            return false;
        }
        return true;
    }

    protected final void handleError(final Exception e) throws CommandSyntaxException {
        if (e.getClass() == CommandSyntaxException.class) {
            throw (CommandSyntaxException) e;
        }

        LoggerUtil.sendError("Encountered error running " + name + " command", e);
    }

    /**
     * This method wraps the executeDefault method in a try-catch block and should be used
     * as an argument for the .executes method when building the command.
     */
    protected final int executeDefaultWrapper(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!shouldExecute(context)) {
            return 0;
        }

        try {
            return executeDefault(context, !context.getSource().isExecutedByPlayer());
        } catch (Exception e) {
            handleError(e);
            sendErrorOutput(context, "Encountered unexpected error running " + name + " command");
            return 0;
        }
    }

    /**
     * This method wraps the executeParameterized method in a try-catch block and should be used
     * as an argument for the .executes method when building the command.
     */
    protected final int executeParameterizedWrapper(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!shouldExecute(context)) {
            return 0;
        }
        
        try {
            return executeParameterized(context, !context.getSource().isExecutedByPlayer());
        } catch (Exception e) {
            handleError(e);
            return 0;
        }
    }

    /**
     * This method should not be used directly as an argument for the .executes method.
     * Use the executeDefaultWrapper method instead.
     */
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) throws CommandSyntaxException {
        return 1;
    }

    /**
     * This method should not be used directly as an argument for the .executes method.
     * Use the executeParameterizedWrapper method instead.
     */
    protected int executeParameterized(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) throws CommandSyntaxException  {
        return 1;
    }

    protected final void sendOutput(final CommandContext<ServerCommandSource> context, final String output, final boolean isServerConsoleOutput) {
        context.getSource().sendMessage(Text.literal(output));
        if (opIsRequired || isServerConsoleOutput) {
            String commandSourcePlayerName = context.getSource().getName();
            String msg = "[" + commandSourcePlayerName + ": Used " + name + " command]";

            Arrays.asList(context.getSource().getServer().getPlayerManager().getOpNames()).forEach((playerName) -> {
                if (!commandSourcePlayerName.equals(playerName)
                    && (!isServerConsoleOutput || (isServerConsoleOutput && opIsRequired && SHOULD_BROADCAST_CONSOLE_TO_OPS))) {
                    CommandOutputBuilder outputBuilder = new CommandOutputBuilder(msg, 
                        CommandOutputBuilder.Color.GRAY, CommandOutputBuilder.Font.ITALICIZED, false);
                    context.getSource().getServer().getPlayerManager().getPlayer(playerName)
                        .sendMessage(Text.literal(outputBuilder.toString()));
                }
            });
            
            if (!isServerConsoleOutput) {
                LoggerUtil.sendInfo(msg, false);
            }
        }
    }

    protected final void sendErrorOutput(final CommandContext<ServerCommandSource> context, final String output) {
        context.getSource().sendError(Text.literal(output));
    }

    public String getName() {
        return name;
    }

    public String[][] getArgNames() {
        return argNames;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}