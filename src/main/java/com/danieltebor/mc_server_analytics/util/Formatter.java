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

import java.util.List;
import java.util.OptionalDouble;

/**
 * @author Daniel Tebor
 */
public class Formatter {
    public static enum Color {
        DARK_RED("dark_red", "§4"),
        RED("red", "§c"),
        GOLD("gold", "§6"),
        YELLOW("yellow", "§e"),
        DARK_GREEN("dark_green", "§2"),
        GREEN("green", "§a"),
        AQUA("aqua", "§b"),
        DARK_AQUA("dark_aqua", "§3"),
        DARK_BLUE("dark_blue", "§1"),
        BLUE("blue", "§9"),
        LIGHT_PURPLE("light_purple", "§d"),
        DARK_PURPLE("dark_purple", "§5"),
        WHITE("white", "§f"),
        GRAY("gray", "§7"),
        DARK_GRAY("dark_gray", "§8"),
        BLACK("black", "§0");

        private final String name;
        private final String chatCode;

        Color(String name, String chatCode) {
            this.name = name;
            this.chatCode = chatCode;
        }

        public String getName() {
            return name;
        }

        public String getChatCode() {
            return chatCode;
        }
    }

    public static String buildUtilizationBar(final double utilization, final int lowerBound, final int upperBound, final int stepSize,
                                             final Color color, final boolean shouldFormatColor) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("lowerBound must be less than or equal to upperBound");
        }
        else if (utilization < lowerBound || utilization > upperBound) {
            throw new IllegalArgumentException("utilization must be between lowerBound and upperBound");
        }
        
        StringBuilder utilizationBar = new StringBuilder("[");
        int barProgress = lowerBound;

        while (barProgress < utilization && barProgress < upperBound) {
            utilizationBar.append(
                color != null && shouldFormatColor
                    ? Formatter.formatColor("|", color)
                    : "|");
            barProgress += stepSize;
        }
        while (barProgress < upperBound) {
            utilizationBar.append(
                shouldFormatColor
                    ? Formatter.formatColor(".", Formatter.Color.GRAY)
                    : ".");
            barProgress += stepSize;
        }
        utilizationBar.append("]");

        return utilizationBar.toString();
    }

    public static String buildUtilizationBar(final List<Double> utilizations, final int lowerBound, final int upperBound, final int stepSize,
                                             final List<Color> colors, final boolean shouldFormatColor) {
        OptionalDouble largestUtilization = utilizations.stream().mapToDouble(Double::doubleValue).max();
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("lowerBound must be less than or equal to upperBound");
        }
        else if (!largestUtilization.isPresent()) {
            throw new IllegalArgumentException("utilizations must not be empty");
        }
        else if (largestUtilization.getAsDouble() < lowerBound || largestUtilization.getAsDouble() > upperBound) {
            throw new IllegalArgumentException("utilization must be between lowerBound and upperBound");
        }
        else if (utilizations.size() != colors.size()) {
            throw new IllegalArgumentException("utilizations and colors must be the same size");
        }
        
        StringBuilder utilizationBar = new StringBuilder("[");
        int barProgress = lowerBound;

        for (int i = 0; i < utilizations.size(); i++) {
            while (barProgress < utilizations.get(i) && barProgress < upperBound) {
                utilizationBar.append(
                    colors.get(i) != null && shouldFormatColor
                        ? Formatter.formatColor("|", colors.get(i))
                        : "|");
                barProgress += stepSize;
            }
        }
        while (barProgress < upperBound) {
            utilizationBar.append(
                shouldFormatColor
                    ? Formatter.formatColor(".", Formatter.Color.GRAY)
                    : ".");
            barProgress += stepSize;
        }
        utilizationBar.append("]");

        return utilizationBar.toString();
    }

    public static String formatColor(final String str, final Formatter.Color color) {
        StringBuilder formattedString = new StringBuilder(color.chatCode);
        formattedString.append(str);
        formattedString.append("§r");
        return formattedString.toString();
    }

    public static String formatDecimal(double dec) {
        if (dec < 0.05) {
            dec = Math.ceil(dec * 10) / 10;
        }
        return String.format("%.1f", dec);
    }

    public static Color rateNumByUpperBound(final double num, final double goodThreshold, 
                                            final double moderateThreshold, final double badThreshold) {
        if (goodThreshold < moderateThreshold) {
            throw new IllegalArgumentException("goodThreshold must be greater than or equal to moderateThreshold");
        }
        else if (moderateThreshold < badThreshold) {
            throw new IllegalArgumentException("moderateThreshold must be greater than or equal to badThreshold");
        }
        
        if (num >= goodThreshold) {
            return Formatter.Color.GREEN;
        }
        else if (num >= moderateThreshold) {
            return Formatter.Color.YELLOW;
        }
        if (num >= badThreshold) {
            return Formatter.Color.RED;
        }
        else {
            return Formatter.Color.DARK_RED;
        }
    }

    public static Color rateNumByLowerBound(final double num, final double goodThreshold,
                                            final double moderateThreshold, final double badThreshold) {
        if (goodThreshold > moderateThreshold) {
            throw new IllegalArgumentException("goodThreshold must be less than or equal to moderateThreshold");
        }
        else if (moderateThreshold > badThreshold) {
            throw new IllegalArgumentException("moderateThreshold must be less than or equal to badThreshold");
        }

        if (num <= goodThreshold) {
            return Formatter.Color.GREEN;
        }
        else if (num <= moderateThreshold) {
            return Formatter.Color.YELLOW;
        }
        if (num <= badThreshold) {
            return Formatter.Color.RED;
        }
        else {
            return Formatter.Color.DARK_RED;
        }
    }
}
