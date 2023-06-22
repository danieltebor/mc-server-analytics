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

package com.danieltebor.mc_server_analytics.mixin;

import com.danieltebor.mc_server_analytics.accessor.MinecraftServerAccessor;
import com.danieltebor.mc_server_analytics.tracker.TickInfoTracker;

import net.minecraft.server.MinecraftServer;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * @author Daniel Tebor
 */
@Mixin(MinecraftServer.class)
@Implements({@Interface(iface = MinecraftServerAccessor.class, prefix = "mcServerAnalytics$")})
public abstract class MinecraftServerMixin {
    private final TickInfoTracker tickTimesNS5s = new TickInfoTracker(5);
    private final TickInfoTracker tickTimesNS15s = new TickInfoTracker(15);
    private final TickInfoTracker tickTimesNS1m = new TickInfoTracker(60);
    private final TickInfoTracker tickTimesNS5m = new TickInfoTracker(60 * 5);
    private final TickInfoTracker tickTimesNS15m = new TickInfoTracker(60 * 15);

    @Inject(method = "tick", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci, final long tickStartTimeNS, final long tickDurationNS) {
        tickTimesNS5s.submitTickTimeNS(tickDurationNS);
        tickTimesNS15s.submitTickTimeNS(tickDurationNS);
        tickTimesNS1m.submitTickTimeNS(tickDurationNS);
        tickTimesNS5m.submitTickTimeNS(tickDurationNS);
        tickTimesNS15m.submitTickTimeNS(tickDurationNS);
    }

    public float mcServerAnalytics$getTPS5s() {
        return tickTimesNS5s.getTPS();
    }

    public float mcServerAnalytics$getTPS15s() {
        return tickTimesNS15s.getTPS();
    }

    public float mcServerAnalytics$getTPS1m() {
        return tickTimesNS1m.getTPS();
    }

    public float mcServerAnalytics$getTPS5m() {
        return tickTimesNS5m.getTPS();
    }

    public float mcServerAnalytics$getTPS15m() {
        return tickTimesNS15m.getTPS();
    }

    public float mcServerAnalytics$getMSPT5s() {
        return tickTimesNS5s.getMSPT();
    }

    public float mcServerAnalytics$getMSPT15s() {
        return tickTimesNS15s.getMSPT();
    }

    public float mcServerAnalytics$getMSPT1m() {
        return tickTimesNS1m.getMSPT();
    }

    public float mcServerAnalytics$getMSPT5m() {
        return tickTimesNS5m.getMSPT();
    }

    public float mcServerAnalytics$getMSPT15m() {
        return tickTimesNS15m.getMSPT();
    }
}