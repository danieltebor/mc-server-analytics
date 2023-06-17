package com.danieltebor.mc_server_analytics.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldFileInfoTracker {

    private final Path worldDir = Paths.get(System.getProperty("user.dir"), "world");

    private final Thread worldSizeTracker = new Thread(this::trackWorldSize);
    private final Object worldSizeTrackerLock = new Object();
    private volatile long cachedWorldSize = -1;
    private volatile boolean worldSizeTrackerShouldRun = true;

    public WorldFileInfoTracker() {
        worldSizeTracker.start();
    }

    public void trackWorldSize() {
        int timeToWait = 1000 * 60;
        long adjustedTimeToWait;
        long startTime;
        long timeTaken;

        while (worldSizeTrackerShouldRun) {
            try {
                startTime = System.currentTimeMillis();

                cachedWorldSize = Files.find(worldDir, Integer.MAX_VALUE, (path, attrs) -> attrs.isRegularFile())
                    .parallel()
                    .mapToLong(p -> p.toFile().length())
                    .sum();

                timeTaken = System.currentTimeMillis() - startTime;

                adjustedTimeToWait = timeToWait - timeTaken;
                if (adjustedTimeToWait < 0) {
                    adjustedTimeToWait = 0;
                    timeToWait *= 2;
                }

                synchronized(worldSizeTrackerLock) {
                    worldSizeTrackerLock.wait(adjustedTimeToWait);
                }
            } 
            catch (IOException e) {
                worldSizeTrackerShouldRun = false;
                System.err.println("[MC Server Analytics] Unable to access world file");
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void close() {
        worldSizeTrackerShouldRun = false;
        synchronized(worldSizeTrackerLock) {
            worldSizeTrackerLock.notify();
        }

        try {
            worldSizeTracker.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long getWorldSizeBytes() {
        return cachedWorldSize;
    }

    public long getWorldSizeMB() {
        return cachedWorldSize / (1024 * 1024);
    }

    public float getWorldSizeGB() {
        return (float) cachedWorldSize / (1024 * 1024 * 1024);
    }

    public boolean isTracking() {
        return worldSizeTrackerShouldRun;
    }
}
