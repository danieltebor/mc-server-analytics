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

package com.danieltebor.mc_server_analytics;

import com.danieltebor.mc_server_analytics.command.Commands;
import com.danieltebor.mc_server_analytics.tracker.CPUInfoTracker;
import com.danieltebor.mc_server_analytics.tracker.WorldFileInfoTracker;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.server.MinecraftServer;

/**
 * @author Daniel Tebor
 */
public class MCServerAnalytics implements DedicatedServerModInitializer {

    private static MCServerAnalytics instance;
    
    private MinecraftServer server;
    private CPUInfoTracker cpuInfoTracker = new CPUInfoTracker();
    private WorldFileInfoTracker worldFileInfoTracker = new WorldFileInfoTracker();
    
    public MCServerAnalytics() {
        if (instance != null) {
            throw new IllegalStateException("Only One instance of MCServerAnalytics allowed");
        }
        instance = this;
    }

    @Override
    public void onInitializeServer() {
        cpuInfoTracker.start();
        worldFileInfoTracker.start();

        Commands.registerCommands();

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            synchronized (this) {
                this.server = server;
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            cpuInfoTracker.close();
            worldFileInfoTracker.close();
        });
    }

    public CPUInfoTracker getCpuInfoTracker() {
        return cpuInfoTracker;
    }

    public WorldFileInfoTracker getWorldFileInfoTracker() {
        return worldFileInfoTracker;
    }

    public synchronized MinecraftServer getServer() {
        return server;
    }

    public static MCServerAnalytics getInstance() {
        return instance;
    }
}