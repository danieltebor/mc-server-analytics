package com.danieltebor.mc_server_analytics.util;

import java.util.Queue;

import java.util.ArrayDeque;

public class TPSTracker {
    public final static int DESIRED_TPS = 20;
    public final static long SEC_AS_NANO = 1_000_000_000;
    public final static long DESIRED_TICK_TIME_NS = SEC_AS_NANO / DESIRED_TPS;

    private final int TICKS_TO_TRACK;

    private Queue<Long> tickTimesNS;

    public TPSTracker(int secondsToTrack) {
        if (secondsToTrack <= 0) {
            throw new RuntimeException("secondsToTrack must be greater than 0.");
        }

        this.TICKS_TO_TRACK = secondsToTrack * DESIRED_TPS;
        this.tickTimesNS = new ArrayDeque<Long>(TICKS_TO_TRACK);
        this.tickTimesNS.add(DESIRED_TICK_TIME_NS);
    }

    public void submitTickTimeNS(long tickTimeNS) {
        if (this.tickTimesNS.size() == this.TICKS_TO_TRACK) {
            this.tickTimesNS.remove();
        }
        this.tickTimesNS.add(tickTimeNS);
    }

    public float getTPS() {
        long sum = this.tickTimesNS.stream().mapToLong(Long::longValue).sum();
        float avgTickTime = (float) sum / this.tickTimesNS.size();
        return (float) SEC_AS_NANO / avgTickTime;
    }
}