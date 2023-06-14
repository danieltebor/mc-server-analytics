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

package com.danieltebor.mc_server_analytics.util;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Daniel Tebor
 */
public class TPSTracker {
    public final static int DESIRED_TPS = 20;
    public final static long SEC_AS_NANO = 1_000_000_000;
    public final static long DESIRED_TICK_TIME_NS = SEC_AS_NANO / DESIRED_TPS;

    private final int TICKS_TO_TRACK;

    private Queue<Long> tickTimesNS;

    public TPSTracker(final int secondsToTrack) {
        if (secondsToTrack <= 0) {
            throw new RuntimeException("secondsToTrack must be greater than 0");
        }

        this.TICKS_TO_TRACK = secondsToTrack * DESIRED_TPS;
        this.tickTimesNS = new ArrayDeque<Long>(TICKS_TO_TRACK);
        this.tickTimesNS.add(DESIRED_TICK_TIME_NS);
    }

    public void submitTickTimeNS(final long tickTimeNS) {
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