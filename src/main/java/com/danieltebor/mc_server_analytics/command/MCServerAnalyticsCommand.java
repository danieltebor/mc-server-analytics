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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Objects;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Daniel Tebor
 */
public abstract class MCServerAnalyticsCommand {

    private final String name;
    private final String[][] argNames;
    private final String description;

    MCServerAnalyticsCommand(final String name, final String[][] argNames, final String description) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.argNames = Objects.requireNonNull(argNames, "argNames must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(getArgumentBuilderImpl()));
    }

    protected abstract LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl();

    protected final LiteralArgumentBuilder<ServerCommandSource> getDefaultArgumentBuilder() {
        return CommandManager.literal(name).executes(this::executeDefault);
    }

    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }

    protected int executeParameterized(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
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
}