package com.danieltebor.mc_server_analytics.extension;

public interface MinecraftServerTPSExtension {
    public float getTPS5s();
    public float getTPS10s();
    public float getTPS1m();
    public float getTPS5m();
    public String formatTPS(float tps);
}