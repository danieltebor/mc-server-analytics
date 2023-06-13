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

@Mixin(MinecraftServer.class)
@Implements({@Interface(iface = MinecraftServerTPSExtension.class, prefix = "mcServerAnalytics$")})
public abstract class MinecraftServerMixin {
    private final TPSTracker TICK_TIMES_NS_5s = new TPSTracker(5);
    private final TPSTracker TICK_TIMES_NS_10s = new TPSTracker(10);
    private final TPSTracker TICK_TIMES_NS_1m = new TPSTracker(60);
    private final TPSTracker TICK_TIMES_NS_5m = new TPSTracker(60 * 5);

    private long prevTickEndTimeNS = System.nanoTime();

    @Inject(method = "tick", at = @At("RETURN"))
    private void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        long tickStartTimeNS = System.nanoTime();
        long tickTimeNS = tickStartTimeNS - prevTickEndTimeNS;
        prevTickEndTimeNS = tickStartTimeNS;

        this.TICK_TIMES_NS_5s.submitTickTimeNS(tickTimeNS);
        this.TICK_TIMES_NS_10s.submitTickTimeNS(tickTimeNS);
        this.TICK_TIMES_NS_1m.submitTickTimeNS(tickTimeNS);
        this.TICK_TIMES_NS_5m.submitTickTimeNS(tickTimeNS);
    }

    public float mcServerAnalytics$getTPS5s() {
        return this.TICK_TIMES_NS_5s.getTPS();
    }

    public float mcServerAnalytics$getTPS10s() {
        return this.TICK_TIMES_NS_10s.getTPS();
    }

    public float mcServerAnalytics$getTPS1m() {
        return this.TICK_TIMES_NS_1m.getTPS();
    }

    public float mcServerAnalytics$getTPS5m() {
        return this.TICK_TIMES_NS_5m.getTPS();
    }

    public String mcServerAnalytics$formatTPS(float tps) {
        return String.format("%.1f", tps);
    }
}
