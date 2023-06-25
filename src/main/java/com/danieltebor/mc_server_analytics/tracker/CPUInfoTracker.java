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

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;

/**
 * @author Daniel Tebor
 */
public final class CPUInfoTracker extends Tracker {

    private static final long REFRESH_WAIT_TIME = 1000;
    
    private final SystemInfo si = new SystemInfo();
    private final CentralProcessor processor = si.getHardware().getProcessor();

    private double overallLoad;
    private double[] threadLoads;
    
    private final Sensors sensors = si.getHardware().getSensors();
    private boolean tempSensorIsAvailable = true;

    public CPUInfoTracker() {
        overallLoad = processor.getSystemCpuLoadBetweenTicks(processor.getSystemCpuLoadTicks());
        threadLoads = processor.getProcessorCpuLoadBetweenTicks(processor.getProcessorCpuLoadTicks());
    }

    @Override
    protected void trackImpl() throws InterruptedException {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[][] prevProcTicks = processor.getProcessorCpuLoadTicks();

        while (shouldTrack()) {
            synchronized (this) {
                overallLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
                threadLoads = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
            }

            prevTicks = processor.getSystemCpuLoadTicks();
            prevProcTicks = processor.getProcessorCpuLoadTicks();

            synchronized (lock) {
                lock.wait(REFRESH_WAIT_TIME);
            }
        }
    }

    public long[] getCoreFreqsHz() {
        return processor.getCurrentFreq();
    }

    public long getMaxCoreFreqHz() {
        final long[] coreFreqs = getCoreFreqsHz();
        long maxFreq = 0;
        
        for (long freq : coreFreqs) {
            if (freq > maxFreq) {
                maxFreq = freq;
            }
        }

        return maxFreq;
    }

    public double[] getCoreFreqsGHz() {
        final long[] coreFreqsHz = getCoreFreqsHz();
        final double[] coreFreqsGHz = new double[coreFreqsHz.length];

        for (int i = 0; i < coreFreqsHz.length; i++) {
            coreFreqsGHz[i] = coreFreqsHz[i] / 1e9;
        }

        return coreFreqsGHz;
    }

    public float getMaxCoreFreqGHz() {
        return (float) (getMaxCoreFreqHz() / 1e9);
    }

    public synchronized double getOverallLoad() {
        return overallLoad != 0.0 ? overallLoad : -1;
    }

    public synchronized double[] getThreadLoads() {
        return threadLoads;
    }

    public int getThreadCount() {
        return processor.getLogicalProcessorCount();
    }

    public double getTempCelc() {
        if (!tempSensorIsAvailable) {
            return -1;
        }

        double temp = sensors.getCpuTemperature();
        if (temp == 0.0) {
            tempSensorIsAvailable = false;
            return -1;
        }

        return temp;
    }
}