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

package com.danieltebor.mc_server_analytics.tracker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author Daniel Tebor
 */
public class TickInfoTracker extends Tracker {

    public final static int DESIRED_TPS = 20;

    private final int secondsToTrack, ticksToTrack;
    private Queue<Integer> rollingTPS;
    private Queue<Float> rollingMSPT;
    private List<Long> ticksNS = new ArrayList<>(DESIRED_TPS + 1);

    public TickInfoTracker(final int secondsToTrack) {
        if (secondsToTrack <= 0) {
            throw new RuntimeException("secondsToTrack must be greater than 0");
        }

        this.secondsToTrack = secondsToTrack;
        ticksToTrack = secondsToTrack * DESIRED_TPS;
        
        rollingTPS = new ArrayDeque<>(secondsToTrack);
        rollingTPS.add(DESIRED_TPS);

        rollingMSPT = new ArrayDeque<>(ticksToTrack);
        rollingMSPT.add(0f);

        start();
    }

    @Override
    protected void trackImpl() throws InterruptedException {
        long timeToWait = 1000;
        long adjustedTimeToWait;
        long startTime;
        long timeTaken;
        
        while (shouldTrack()) {
            startTime = System.currentTimeMillis();

            synchronized (this) {
                if (rollingTPS.size() == secondsToTrack) {
                    rollingTPS.remove();
                }
                rollingTPS.add(ticksNS.size());

                for (float tick : ticksNS) {
                    if (rollingMSPT.size() == ticksToTrack) {
                        rollingMSPT.remove();
                    }
                    rollingMSPT.add(tick / 1000000.0f);
                }
                ticksNS.clear();
            }

            timeTaken = System.currentTimeMillis() - startTime;
            adjustedTimeToWait = timeToWait - timeTaken;
            if (adjustedTimeToWait < 0) {
                adjustedTimeToWait = 0;
            }

            synchronized (lock) {
                lock.wait(adjustedTimeToWait);
            }
        }
    }

    public synchronized void submitTickTimeNS(final long tickTimeNS) {
        if (getState() != Thread.State.NEW) {
            ticksNS.add(tickTimeNS);
        }
    }

    public synchronized float getTPS() {
        return (float) rollingTPS.stream().reduce(0, Integer::sum) / rollingTPS.size();
    }

    public synchronized float getMSPT() {
        return rollingMSPT.stream().reduce(0f, Float::sum) / rollingMSPT.size();
    }
}