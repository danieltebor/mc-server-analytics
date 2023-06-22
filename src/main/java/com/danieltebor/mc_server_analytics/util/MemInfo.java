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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * @author Daniel Tebor
 */
public abstract class MemInfo {
    
    public static long getCommittedHeapMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
    }

    public static long getUsedHeapMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    public static long getMaxHeapMemory() {
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return isMaxHeapMemoryDefined() ? heapMemoryUsage.getMax() : heapMemoryUsage.getCommitted();
    }

    public static boolean isMaxHeapMemoryDefined() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() != -1;
    }

    public static long getCommittedNonHeapMemory() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
    }

    public static long getUsedNonHeapMemory() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    public static long getMaxNonHeapMemory() {
        MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        return isMaxNonHeapMemoryDefined() ? nonHeapMemoryUsage.getMax() : nonHeapMemoryUsage.getCommitted();
    }

    public static boolean isMaxNonHeapMemoryDefined() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax() != -1;
    }

    public static long getTotalCommittedMemory() {
        return getCommittedHeapMemory() + getCommittedNonHeapMemory();
    }

    public static long getTotalUsedMemory() {
        return getUsedHeapMemory() + getUsedNonHeapMemory();
    }

    public static long getTotalMaxMemory() {
        return getMaxHeapMemory() + getMaxNonHeapMemory();
    }

    public static boolean isTotalMaxMemoryDefined() {
        return isMaxHeapMemoryDefined() || isMaxNonHeapMemoryDefined();
    }

    public static long toMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    public static float toGB(long bytes) {
        return (float) bytes / (1024 * 1024 * 1024);
    }
}
