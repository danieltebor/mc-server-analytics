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

package com.danieltebor.mc_server_analytics.command.commands;

import com.danieltebor.mc_server_analytics.MCServerAnalytics;
import com.danieltebor.mc_server_analytics.command.CommandOutputBuilder;
import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;
import com.danieltebor.mc_server_analytics.util.CPUInfoTracker;

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
public final class CPUCommand extends MCServerAnalyticsCommand {

    public static final String NAME = "cpu";
    public static final String[][] ARG_NAMES = {};
    public static final String DESCRIPTION = "Shows cpu thread load, overall load, and temperature";

    public CPUCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("cpu")
            .executes(this::executeDefault));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendError(Text.literal("cpu command is not available in server console"));
            return 0;
        }

        final CPUInfoTracker cpuInfoTracker = MCServerAnalytics.getInstance().getCpuInfoTracker();
        final double[] threadLoads = cpuInfoTracker.getThreadLoads();
        final double overallLoad = cpuInfoTracker.getOverallLoad() * 100;

        if (!cpuInfoTracker.loadIsAvailable()) {
            context.getSource().sendError(Text.literal("/cpu command is not available. This could be due to driver or hardware issues."));
            return 0;
        }

        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder("\n", false);
        
        outputBuilder.append("         CPU Info", CommandOutputBuilder.Color.AQUA);
        outputBuilder.append("\n===================\n");
        
        // CPU thread loads.
        for (int i = 0; i < threadLoads.length; i++) {
            double threadLoad = threadLoads[i] * 100;

            outputBuilder.buildUtilizationBarAndAppend(threadLoad, 0, 100, 5, CommandOutputBuilder.Color.LIGHT_PURPLE);
            outputBuilder.append(" ");

            outputBuilder.rateByLowerBoundAndAppend(threadLoad, 90, 95, 98, true, false);
            outputBuilder.append("% (");

            outputBuilder.append("CPU ", CommandOutputBuilder.Color.GOLD);
            outputBuilder.append(i, CommandOutputBuilder.Color.GOLD);
            outputBuilder.append(")\n");
        }

        // Overall CPU load.
        outputBuilder.append("Overall Load", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        outputBuilder.rateByLowerBoundAndAppend(overallLoad, 90, 95, 98, true, false);
        outputBuilder.append("%\n");

        // CPU temp.
        final double tempCelc = cpuInfoTracker.getTempCelc();
        final boolean tempCelcIsAvailable = tempCelc != 0.0;

        outputBuilder.append("Temp", CommandOutputBuilder.Color.GOLD);
        outputBuilder.append(": ");

        if (tempCelcIsAvailable) {
            outputBuilder.rateByLowerBoundAndAppend(tempCelc, 70, 80, 90, true, false);
        }
        else {
            outputBuilder.append("UNAVAILABLE", CommandOutputBuilder.Color.DARK_RED);
        }
        if (tempCelcIsAvailable) {
            outputBuilder.append("°C");
        }

        context.getSource().sendMessage(Text.literal(outputBuilder.toString()));
        return 1;
    }
}