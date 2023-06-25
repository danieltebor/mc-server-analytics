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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Tebor
 */
public final class Commands {
    
    private final static Map<Class<?>, MCServerAnalyticsCommand> REGISTERED_COMMANDS = new HashMap<>();
    
    private static boolean commandsHaveBeenRegistered = false;

    public static void registerCommands() {
        if (commandsHaveBeenRegistered == true) {
            throw new IllegalStateException("Commands have already been registered");
        }

        final MCServerAnalyticsCommand[] commandsToRegister = {
            new ChunkInfoCommand(),
            new CPUCommand(),
            new EntityInfoCommand(),
            new HelpCommand(),
            new MEMCommand(),
            new MSPTCommand(),
            new PerformanceSummaryCommand(),
            new PingAvgCommand(),
            new PingCommand(),
            new TPSCommand(),
            new WorldSizeCommand()
        };

        for (MCServerAnalyticsCommand command : commandsToRegister) {
            if (command.isEnabled()) {
                registerCommand(command);
            }
        }

        commandsHaveBeenRegistered = true;
    }

    private static void registerCommand(MCServerAnalyticsCommand command) {
        if (REGISTERED_COMMANDS.containsKey(command.getClass())) {
            throw new IllegalStateException("An instance of this class has already been created");
        }
        REGISTERED_COMMANDS.put(command.getClass(), command);
    }

    public static Collection<MCServerAnalyticsCommand> getRegisteredCommands() {
        return Collections.unmodifiableCollection(REGISTERED_COMMANDS.values());
    }

    public static Collection<MCServerAnalyticsCommand> getRegisteredCommandsAlphabetized() {
        List<MCServerAnalyticsCommand> commands = new ArrayList<>(REGISTERED_COMMANDS.values());
        Collections.sort(commands, Comparator.comparing(MCServerAnalyticsCommand::getName));
        return Collections.unmodifiableCollection(commands);
    }
}