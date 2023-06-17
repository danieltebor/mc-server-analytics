package com.danieltebor.mc_server_analytics.command.commands;

import com.danieltebor.mc_server_analytics.command.CommandOutputBuilder;
import com.danieltebor.mc_server_analytics.command.MCServerAnalyticsCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;

public class EntityInfoCommand extends MCServerAnalyticsCommand {
    public static final String NAME = "entity-info";
    public static final String[][] ARG_NAMES = {{"dimension"}};
    public static final String DESCRIPTION = "Shows number of entities";

    public EntityInfoCommand() {
        super(NAME, ARG_NAMES, DESCRIPTION);
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher,
                         final CommandRegistryAccess registryAccess,
                         final RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal(NAME)
            .executes(this::executeDefault)
            .then(CommandManager.argument(ARG_NAMES[0][0], DimensionArgumentType.dimension())
            .executes(this::executeParameterized)));
    }

    @Override
    protected int executeDefault(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();

        context.getSource().sendMessage(Text.literal(
            buildOutput(server, null, isServerConsoleOutput)));
        return 1;
    }

    @Override
    protected int executeParameterized(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final MinecraftServer server = context.getSource().getServer();
        final String dimArgument = DimensionArgumentType.getDimensionArgument(context, ARG_NAMES[0][0])
            .getDimensionEntry().getKey().get().getValue().toString().split(":")[1];
        final boolean isServerConsoleOutput = !context.getSource().isExecutedByPlayer();
        
        context.getSource().sendMessage(Text.literal(
            buildOutput(server, dimArgument, isServerConsoleOutput)));
        
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
        }
        else {
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
            } catch(Exception e) {}
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