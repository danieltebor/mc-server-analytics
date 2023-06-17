package com.danieltebor.mc_server_analytics.command;

import java.util.Collections;
import java.util.List;

public class CommandOutputBuilder {
    
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
    
    final StringBuilder outputBuilder;
    final boolean isServerConsoleOutput;

    public CommandOutputBuilder(final boolean isServerConsoleOutput) {
        outputBuilder = new StringBuilder();
        this.isServerConsoleOutput = isServerConsoleOutput;
    }

    public CommandOutputBuilder(final String str, final boolean isServerConsoleOutput) {
        outputBuilder = new StringBuilder(str);
        this.isServerConsoleOutput = isServerConsoleOutput;
    }

    public CommandOutputBuilder(final CharSequence seq, final boolean isServerConsoleOutput) {
        outputBuilder = new StringBuilder(seq);
        this.isServerConsoleOutput = isServerConsoleOutput;
    }

    public CommandOutputBuilder(final String str, final Color color, final boolean isServerConsoleOutput) {
        this.isServerConsoleOutput = isServerConsoleOutput;
        outputBuilder = new StringBuilder(makeColored(str, color));
    }

    public CommandOutputBuilder(final CharSequence seq, final Color color, final boolean isServerConsoleOutput) {
        this.isServerConsoleOutput = isServerConsoleOutput;
        outputBuilder = new StringBuilder(makeColored(seq.toString(), color));
    }

    public void append(String str) {
        outputBuilder.append(str);
    }

    public void append(char c) {
        outputBuilder.append(c);
    }

    public void append(Object obj) {
        outputBuilder.append(obj);
    }

    public void append(double dec, boolean shouldFormatDec) {
        append(shouldFormatDec ? formatDec(dec) : dec);
    }

    public void append(String str, Color color) {
        append(makeColored(str, color));
    }

    public void append(char c, Color color) {
        append(Character.toString(c), color);
    }

    public void append(Object obj, Color color) {
        append(String.valueOf(obj), color);
    }

    public void append(double dec, boolean shouldFormatDec, Color color) {
        append(shouldFormatDec ? formatDec(dec) : dec, color);
    }

    public void buildUtilizationBarAndAppend(final double utilization,
                                             final int lowerBound, final int upperBound, final int stepSize,
                                             final Color color) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("lowerBound must be less than or equal to upperBound");
        }
        else if (utilization < lowerBound || utilization > upperBound) {
            throw new IllegalArgumentException("utilization must be between lowerBound and upperBound");
        }
        
        int barProgress = lowerBound;

        append("[");
        while (barProgress < utilization && barProgress < upperBound) {
            append("|", color);
            barProgress += stepSize;
        }
        while (barProgress < upperBound) {
            append(".", Color.GRAY);
            barProgress += stepSize;
        }
        append("]");
    }

    public void buildUtilizationBarAndAppend(final List<Double> utilizations,
                                             final int lowerBound, final int upperBound, final int stepSize,
                                             final List<Color> colors) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("lowerBound must be less than or equal to upperBound");
        }
        else if (utilizations.size() == 0) {
            throw new IllegalArgumentException("utilizations must not be empty");
        }

        Collections.sort(utilizations);
        double minUtilization = utilizations.get(0);
        double maxUtilization = utilizations.get(utilizations.size() - 1);

        if (minUtilization < lowerBound || minUtilization > upperBound) {
            throw new IllegalArgumentException("utilizations must be between lowerBound and upperBound");
        }
        else if (maxUtilization < lowerBound || maxUtilization > upperBound) {
            throw new IllegalArgumentException("utilizations must be between lowerBound and upperBound");
        }
        else if (utilizations.size() != colors.size()) {
            throw new IllegalArgumentException("utilizations and colors must be the same size");
        }

        int barProgress = lowerBound;

        append("[");
        for (int i = 0; i < utilizations.size(); i++) {
            while (barProgress < utilizations.get(i) && barProgress < upperBound) {
                append("|", colors.get(i));
                barProgress += stepSize;
            }
        }
        while (barProgress < upperBound) {
            append(".", Color.GRAY);
            barProgress += stepSize;
        }
        append("]");
    }

    public void rateByUpperBoundAndAppend(final double num, final double goodThreshold, 
                                          final double moderateThreshold, final double badThreshold,
                                          final boolean shouldFormatNum, final boolean shouldFlattenNum) {
        if (goodThreshold < moderateThreshold) {
            throw new IllegalArgumentException("goodThreshold must be greater than or equal to moderateThreshold");
        }
        else if (moderateThreshold < badThreshold) {
            throw new IllegalArgumentException("moderateThreshold must be greater than or equal to badThreshold");
        }
        
        Color color = null;

        if (num >= goodThreshold) {
            color = Color.GREEN;
        }
        else if (num >= moderateThreshold) {
            color = Color.YELLOW;
        }
        else if (num >= badThreshold) {
            color = Color.RED;
        }
        else {
            color = Color.DARK_RED;
        }

        if (shouldFlattenNum) {
            append((int) num, color);
        }
        else {
            append(num, shouldFormatNum, color);
        }
    }

    public void rateByLowerBoundAndAppend(final double num, final double goodThreshold,
                                          final double moderateThreshold, final double badThreshold,
                                          final boolean shouldFormatNum, final boolean shouldFlattenNum) {
        if (goodThreshold > moderateThreshold) {
            throw new IllegalArgumentException("goodThreshold must be less than or equal to moderateThreshold");
        }
        else if (moderateThreshold > badThreshold) {
            throw new IllegalArgumentException("moderateThreshold must be less than or equal to badThreshold");
        }

        Color color = null;

        if (num <= goodThreshold) {
            color = Color.GREEN;
        }
        else if (num <= moderateThreshold) {
            color = Color.YELLOW;
        }
        else if (num <= badThreshold) {
            color = Color.RED;
        }
        else {
            color = Color.DARK_RED;
        }

        if (shouldFlattenNum) {
            append((int) num, color);
        }
        else {
            append(num, shouldFormatNum, color);
        }
    }

    public void formatSnakeCaseAndAppend(String str) {
        formatSnakeCase(str);
    }

    public void formatSnakeCaseAndAppend(String str, Color color) {
        append(formatSnakeCase(str), color);
    }

    public String formatSnakeCase(String str) {
        final String[] strComponents = str.split("_");
        final StringBuilder formattedStr = new StringBuilder();

        for (int i = 0; i < strComponents.length; i++) {
            formattedStr.append(strComponents[i].substring(0, 1).toUpperCase());
            formattedStr.append(strComponents[i].substring(1));
            formattedStr.append(" ");
        }
        formattedStr.delete(formattedStr.length() - 1, formattedStr.length());

        return formattedStr.toString();
    }

    public void delete(int start, int end) {
        outputBuilder.delete(start, end);
    }

    private String formatDec(final double dec) {
        return String.format("%.1f", dec);
    }

    private String makeColored(final String str, final Color color) {
        if (isServerConsoleOutput) {
            return str;
        }

        final StringBuilder coloredStr = new StringBuilder(color.chatCode);
        coloredStr.append(str);
        coloredStr.append("§r");

        return coloredStr.toString();
    }
    
    @Override
    public String toString() {
        return outputBuilder.toString();
    }
}
