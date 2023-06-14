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
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess registryAccess,
                         RegistrationEnvironment registrationEnvironment) {
        System.out.println("registered");
        dispatcher.register(CommandManager.literal("cpu")
            .executes(this::run));
    }

    @Override
    protected int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()){
            context.getSource().sendError(Text.literal("/cpu command is not available in server console"));
            return 0;
        }
        
        CPUInfoTracker cpuInfoTracker = MCServerAnalytics.getInstance().getCpuInfoTracker();
        String cpuLoadInfo = "        CPU Load        \n"
            + "==================\n";
        
        double[] threadLoads = cpuInfoTracker.getThreadLoads();
        double threadLoadSum = 0;
        for (int i = 0; i < threadLoads.length; i++) {
            cpuLoadInfo += formatLoad(i, threadLoads[i]) + "\n";
            threadLoadSum += threadLoads[i];
        }

        if (threadLoadSum == 0) {
            context.getSource().sendError(Text.literal("/cpu command is not available. This could be due to driver or hardware issues."));
            return 0;
        }

        double overallLoad = cpuInfoTracker.getOverallLoad();
        cpuLoadInfo += "Overall Load: "
            + (overallLoad == 0.0f ? "UNAVAILABLE\n"
            : String.format("%.1f", overallLoad * 100) + "%\n");
        
        //double freq = cpuInfoTracker.getMaxCoreFreqGHz();
        //cpuLoadInfo += "Freq: " + (freq == 0.0f ? "UNAVAILABLE\n" : freq + " GHz\n");

        double tempCelc = cpuInfoTracker.getTempCelc();
        cpuLoadInfo += "Temp: " + (tempCelc == 0.0f ? "UNAVAILABLE" : tempCelc + " °C");

        context.getSource().sendMessage(Text.literal(cpuLoadInfo));
        return 1;
    }

    private String formatLoad(int thread, double threadLoad) {
        threadLoad *= 100;

        String utilizationBar = "[";
        int barProgress = 0;
        while (barProgress < threadLoad) {
            utilizationBar += "|";
            barProgress += 5;
        }
        while (barProgress < 100) {
            utilizationBar += ".";
            barProgress += 5;
        }
        utilizationBar += "]";

        return utilizationBar + " " + String.format("%.1f", threadLoad)
            + "% (CPU " + thread + ")";
    }
}
