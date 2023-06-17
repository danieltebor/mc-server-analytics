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

import com.danieltebor.mc_server_analytics.extension.MinecraftServerTPSExtension;
import com.danieltebor.mc_server_analytics.util.TPSTracker;

import net.minecraft.server.MinecraftServer;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Daniel Tebor
 */
@Mixin(MinecraftServer.class)
@Implements({@Interface(iface = MinecraftServerTPSExtension.class, prefix = "mcServerAnalytics$")})
public abstract class MinecraftServerMixin {
    private final TPSTracker tickTimesNS5s = new TPSTracker(5);
    private final TPSTracker tickTimesNS15s = new TPSTracker(15);
    private final TPSTracker tickTimesNS1m = new TPSTracker(60);
    private final TPSTracker tickTimesNS5m = new TPSTracker(60 * 5);
    private final TPSTracker tickTimesNS15m = new TPSTracker(60 * 15);

    private long prevTickEndTimeNS = System.nanoTime();

    @Inject(method = "tick", at = @At("RETURN"))
    private void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        long tickStartTimeNS = System.nanoTime();
        long tickTimeNS = tickStartTimeNS - prevTickEndTimeNS;
        prevTickEndTimeNS = tickStartTimeNS;

        tickTimesNS5s.submitTickTimeNS(tickTimeNS);
        tickTimesNS15s.submitTickTimeNS(tickTimeNS);
        tickTimesNS1m.submitTickTimeNS(tickTimeNS);
        tickTimesNS5m.submitTickTimeNS(tickTimeNS);
        tickTimesNS15m.submitTickTimeNS(tickTimeNS);
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
}