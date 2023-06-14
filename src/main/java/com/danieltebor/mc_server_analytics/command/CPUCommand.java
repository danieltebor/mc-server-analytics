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
import com.danieltebor.mc_server_analytics.util.CPUInfoTracker;
import com.danieltebor.mc_server_analytics.util.Formatter;
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
        
        CPUInfoTracker cpuInfoTracker = MCServerAnalytics.getInstance().getCpuInfoTracker();
        double[] threadLoads = cpuInfoTracker.getThreadLoads();
        double threadLoadSum = 0;
        
        StringBuilder cpuLoadInfo = new StringBuilder(
            Formatter.formatColor("        CPU Load        ", Formatter.Color.AQUA));
        cpuLoadInfo.append("\n===================\n");
        
        for (int i = 0; i < threadLoads.length; i++) {
            double threadLoad = threadLoads[i] * 100;

            cpuLoadInfo.append(
                Formatter.buildUtilizationBar(threadLoad, 0, 100, 5, Formatter.Color.LIGHT_PURPLE, true));
            cpuLoadInfo.append(" ");

            cpuLoadInfo.append(
                Formatter.formatColor(Formatter.formatDecimal(threadLoad),
                    Formatter.rateNumByLowerBound(threadLoad, 95, 100, 100)));
            cpuLoadInfo.append("% ");

            StringBuilder cpuCore = new StringBuilder("(CPU ");
            cpuCore.append(i);
            cpuCore.append(")");
            cpuLoadInfo.append(
                Formatter.formatColor(cpuCore.toString(), Formatter.Color.GOLD));
            cpuLoadInfo.append("\n");

            threadLoadSum += threadLoads[i];
        }

        if (threadLoadSum == 0) {
            context.getSource().sendError(Text.literal("/cpu command is not available. This could be due to driver or hardware issues."));
            return 0;
        }

        cpuLoadInfo.append(
            Formatter.formatColor("Overall Load", Formatter.Color.GOLD));
        cpuLoadInfo.append(": ");

        double overallLoad = cpuInfoTracker.getOverallLoad() * 100;
        boolean overallLoadIsAvailable = true;
        if (overallLoad == 0.0) {
            overallLoadIsAvailable = false;
        }
        cpuLoadInfo.append(
            overallLoadIsAvailable
                ? Formatter.formatColor(Formatter.formatDecimal(overallLoad),
                    Formatter.rateNumByLowerBound(overallLoad, 95, 100, 100))
                : Formatter.formatColor("UNAVAILABLE", Formatter.Color.DARK_RED));
        cpuLoadInfo.append(overallLoadIsAvailable ? "%\n" : "\n");

        cpuLoadInfo.append(
            Formatter.formatColor("Temp", Formatter.Color.GOLD));
        cpuLoadInfo.append(": ");

        double tempCelc = cpuInfoTracker.getTempCelc();
        boolean tempCelcIsAvailable = true;
        if (tempCelc == 0.0) {
            tempCelcIsAvailable = false;
        }
        cpuLoadInfo.append(
            overallLoadIsAvailable
                ? Formatter.formatColor("UNAVAILABLE", Formatter.Color.DARK_RED)
                : Formatter.formatColor(Formatter.formatDecimal(tempCelc),
                    Formatter.rateNumByLowerBound(tempCelc, 70, 80, 90)));
        if (tempCelcIsAvailable) {
            cpuLoadInfo.append(" °C");
        }

        context.getSource().sendMessage(Text.literal(cpuLoadInfo.toString()));
        return 1;
    }
}