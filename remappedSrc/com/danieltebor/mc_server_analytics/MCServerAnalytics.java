package com.danieltebor.mc_server_analytics;

import com.danieltebor.mc_server_analytics.commands.*;
import net.fabricmc.api.ModInitializer;

public class MCServerAnalytics implements ModInitializer {
    @Override
    public void onInitialize() {
        TPSCommand.register(null, false);
    }
}