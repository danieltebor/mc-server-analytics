/*
 * Copyright (C) 2023 Daniel Tebor
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

import com.danieltebor.mc_server_analytics.command.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Daniel Tebor
 */
public final class ConfigUtil {

    public static final String ENABLED = "CommandEnabled";
    public static final String REQUIRES_OP = "CommandRequiresOp";
    
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILENAME = "mc-server-analytics.properties";
    private static final Map<String, String> DEFAULT_CONFIG = new LinkedHashMap<>();

    private static Properties configCache = null;

    static {
        DEFAULT_CONFIG.put("coloredCommandOutputs", "true");

        DEFAULT_CONFIG.put(ChunkInfoCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(ChunkInfoCommand.NAME + REQUIRES_OP, "true");
        
        DEFAULT_CONFIG.put(CPUCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(CPUCommand.NAME + REQUIRES_OP, "true");

        DEFAULT_CONFIG.put(EntityInfoCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(EntityInfoCommand.NAME + REQUIRES_OP, "true");

        DEFAULT_CONFIG.put(HelpCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(HelpCommand.NAME + REQUIRES_OP, "false");

        DEFAULT_CONFIG.put(MEMCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(MEMCommand.NAME + REQUIRES_OP, "true");

        DEFAULT_CONFIG.put(MSPTCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(MSPTCommand.NAME + REQUIRES_OP, "false");

        DEFAULT_CONFIG.put(PerformanceSummaryCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(PerformanceSummaryCommand.NAME + REQUIRES_OP, "true");

        DEFAULT_CONFIG.put(PingAvgCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(PingAvgCommand.NAME + REQUIRES_OP, "true");

        DEFAULT_CONFIG.put(PingCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(PingCommand.NAME + REQUIRES_OP, "false");
        DEFAULT_CONFIG.put(PingCommand.NAME + REQUIRES_OP + "ToPingOthers", "true");

        DEFAULT_CONFIG.put(TPSCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(TPSCommand.NAME + REQUIRES_OP, "false");

        DEFAULT_CONFIG.put(WorldSizeCommand.NAME + ENABLED, "true");
        DEFAULT_CONFIG.put(WorldSizeCommand.NAME + REQUIRES_OP, "true");
    }

    private ConfigUtil() {}

    public static Properties readConfig() {
        if (configCache != null) {
            return configCache;
        }

        Path configDir = Paths.get(System.getProperty("user.dir"), CONFIG_DIR);
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectory(configDir);
            } catch(IOException e) {
                LoggerUtil.sendError("Unable to create " + CONFIG_DIR + " directory", e);
            }
        }

        Path configFile = configDir.resolve(CONFIG_FILENAME);
        if (!Files.exists(configFile)) {
            writeDefaultConfig(configFile);
        }

        Properties config = new Properties();
        try (InputStream in = Files.newInputStream(configFile)) {
            config.load(in);
        } catch (IOException e) {
            LoggerUtil.sendError("Unable to read mc-server-analytics.cfg", e);
        }
        configCache = config;
        return config;
    }

    private static void writeDefaultConfig(Path configFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            writer.write("# MC Server Analytics Properties");
            writer.newLine();
            for (Map.Entry<String, String> entry : DEFAULT_CONFIG.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            LoggerUtil.sendError("Unable to write " + CONFIG_FILENAME, e);
        }
    }
}
