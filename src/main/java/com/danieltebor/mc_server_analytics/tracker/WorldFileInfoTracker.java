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

import com.danieltebor.mc_server_analytics.util.LoggerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Daniel Tebor
 */
public final class WorldFileInfoTracker extends Tracker {

    private final static int MAX_RETRIES = 3;
    private final static long RETRY_DELAY = 1000;
    
    private final Path worldPath;
    private long cachedWorldSize = -1;

    public WorldFileInfoTracker() {
        final String serverPath = System.getProperty("user.dir");
        final Path serverPropertiesPath = Paths.get(serverPath, "server.properties");

        String levelName = null;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(serverPropertiesPath)) {
            properties.load(input);
            levelName = properties.getProperty("level-name");
        } catch (IOException e) {
            LoggerUtil.sendError("Unable to read server.properties file", e);
        }

        if (levelName != null) {
            worldPath = Paths.get(serverPath.toString(), levelName);
        } else {
            worldPath = null;
        }
    }

    @Override
    protected void trackImpl() throws InterruptedException {
        long timeToWait = 1000 * 60;
        long adjustedTimeToWait;
        long startTime;
        long timeTaken;
        
        long worldSize;
        int retries = 0;

        if (worldPath == null) {
            close();
            return;
        }

        while (shouldTrack()) {
            try {
                startTime = System.currentTimeMillis();

                worldSize = Files.find(worldPath, Integer.MAX_VALUE, (path, attrs) -> attrs.isRegularFile())
                    .parallel()
                    .mapToLong(p -> p.toFile().length())
                    .sum();
                synchronized (this) {
                    cachedWorldSize = worldSize;
                }

                timeTaken = System.currentTimeMillis() - startTime;
                adjustedTimeToWait = timeToWait - timeTaken;
                if (adjustedTimeToWait < 0) {
                    adjustedTimeToWait = 0;
                    timeToWait *= 2;
                }

                synchronized (lock) {
                    lock.wait(adjustedTimeToWait);
                }
            } catch (IOException e) {
                if (++retries > MAX_RETRIES){
                    LoggerUtil.sendError("Unable to access world directory", e);
                    close();
                } else {
                    synchronized(lock) {
                        lock.wait(RETRY_DELAY);
                    }
                }
            }
        }
    }

    public synchronized long getWorldSizeBytes() {
        return cachedWorldSize;
    }

    public float getWorldSizeMB() {
        return (float) getWorldSizeBytes() / (1024 * 1024);
    }

    public float getWorldSizeGB() {
        return (float) getWorldSizeBytes() / (1024 * 1024 * 1024);
    }
}