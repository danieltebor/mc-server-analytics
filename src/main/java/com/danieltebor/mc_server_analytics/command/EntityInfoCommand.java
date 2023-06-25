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

package com.danieltebor.mc_server_analytics.command;

import com.danieltebor.mc_server_analytics.util.LoggerUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.TypeFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;

/**
 * @author Daniel Tebor
 */
public class EntityInfoCommand extends MCServerAnalyticsCommand {
    public static final String NAME = "entity-info";
    public static final String[][] ARG_NAMES = {{"dimension"}};
    public static final String DESCRIPTION = "Shows number of entities";

    public EntityInfoCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilderImpl() {
        return getDefaultArgumentBuilder()
            .then(CommandManager.argument(ARG_NAMES[0][0], DimensionArgumentType.dimension())
            .executes(this::executeParameterizedWrapper));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) {
        final MinecraftServer server = context.getSource().getServer();

        sendOutput(context, buildOutput(server, null, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context, final boolean isServerConsoleOutput) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final String dimArgument = DimensionArgumentType.getDimensionArgument(context, ARG_NAMES[0][0])
            .getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
        
        sendOutput(context, buildOutput(server, dimArgument, isServerConsoleOutput), isServerConsoleOutput);
        return 1;
    }

    private String buildOutput(final MinecraftServer server, final String dimArgument, final boolean isServerConsoleOutput) {
        final int[] entitySums = {0, 0, 0, 0};
        final int[] dimsWithEntitiesCount = {0};
        final CommandOutputBuilder outputBuilder = new CommandOutputBuilder(isServerConsoleOutput);

        if (dimArgument == null) {
            outputBuilder.append("\n");
            outputBuilder.append(isServerConsoleOutput ? "Entity Info" : "      Entity Info",
                CommandOutputBuilder.Color.AQUA);
            outputBuilder.append("\n=================");
        } else {
            if (isServerConsoleOutput) {
                outputBuilder.append("\n");
            }
            outputBuilder.append("Entity Info", CommandOutputBuilder.Color.AQUA);
            outputBuilder.append(" - ");
        }

        server.getWorlds().forEach((world) -> {
            String dimName = world.getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
            
            if (dimArgument != null && !dimArgument.equals(dimName)) {
                return;
            }

            try {
                dimName = outputBuilder.formatSnakeCase(dimName);
            } catch(Exception e) {
                LoggerUtil.sendInfo("An unexpected error occured formatting dimension name for " + NAME + " command. Using unformatted version", true);
            }
            final int passiveEntitiesCount = world.getEntitiesByType(TypeFilter.instanceOf(PassiveEntity.class), entity -> true).size();
            final int mobEntitiesCount = world.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), entity -> true).size();
            final int itemEntitiesCount = world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), entity -> true).size();
            final int otherEntitiesCount = world.getEntitiesByType(TypeFilter.instanceOf(Entity.class), 
                entity -> !(entity instanceof PassiveEntity || entity instanceof MobEntity || entity instanceof ItemEntity)).size();

            entitySums[0] += passiveEntitiesCount;
            entitySums[1] += mobEntitiesCount;
            entitySums[2] += itemEntitiesCount;
            entitySums[3] += otherEntitiesCount;

            if (dimArgument == null) {
                outputBuilder.append("\n");
            }

            if (dimArgument != null || passiveEntitiesCount + mobEntitiesCount + itemEntitiesCount + otherEntitiesCount != 0) {
                appendEntitiesSegment(outputBuilder, dimName, passiveEntitiesCount, mobEntitiesCount, itemEntitiesCount, otherEntitiesCount);
                dimsWithEntitiesCount[0] += 1;
            }
            else {
                outputBuilder.append(dimName, CommandOutputBuilder.Color.GOLD);
                outputBuilder.append("\n| ");
                outputBuilder.append("No Entities", CommandOutputBuilder.Color.DARK_AQUA);
            }
        });

        if (dimArgument == null && dimsWithEntitiesCount[0] > 1) {
            outputBuilder.append("\n");
            appendEntitiesSegment(outputBuilder, "All Dimensions",
                entitySums[0], entitySums[1], entitySums[2], entitySums[3]);
        }

        return outputBuilder.toString();
    }

    private void appendEntitiesSegment(final CommandOutputBuilder outputBuilder,
                                       final String label,
                                       final int passiveEntityCount,
                                       final int mobEntityCount,
                                       final int itemEntityCount,
                                       final int otherEntityCount) {
        outputBuilder.append(label, CommandOutputBuilder.Color.GOLD);
        outputBuilder.append("\n| ");

        outputBuilder.append("Passive", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(passiveEntityCount, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append(" | ");

        outputBuilder.append("Hostile", CommandOutputBuilder.Color.LIGHT_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(mobEntityCount, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append("\n| ");

        outputBuilder.append("Items", CommandOutputBuilder.Color.DARK_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(itemEntityCount, CommandOutputBuilder.Color.BLUE);
        outputBuilder.append(" | ");

        outputBuilder.append("Other", CommandOutputBuilder.Color.DARK_PURPLE);
        outputBuilder.append(": ");
        outputBuilder.append(otherEntityCount, CommandOutputBuilder.Color.BLUE);
    }
}