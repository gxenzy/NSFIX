package com.peakcobbleverse.nsfix;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class IBlockCommands {

    private static final int MAX_FILL_VOLUME = 32768;

    private IBlockCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isetblock")
                .requires(Permissions.require("nsfix.isetblock", 2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("block", StringArgumentType.word())
                                .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.BLOCK, builder))
                                .executes(ctx -> setBlock(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos"), StringArgumentType.getString(ctx, "block")))
                        )
                )
        );

        dispatcher.register(Commands.literal("ifill")
                .requires(Permissions.require("nsfix.ifill", 2))
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .then(Commands.argument("block", StringArgumentType.word())
                                        .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.BLOCK, builder))
                                        .executes(ctx -> fill(ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "from"),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "to"),
                                                StringArgumentType.getString(ctx, "block")))
                                )
                        )
                )
        );
    }

    private static int setBlock(CommandSourceStack source, BlockPos pos, String bareName) {
        Optional<Block> block = NamespaceResolver.resolveExact(BuiltInRegistries.BLOCK, bareName);
        if (block.isEmpty()) {
            source.sendFailure(Component.literal("No block found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        source.getLevel().setBlockAndUpdate(pos, block.get().defaultBlockState());
        source.sendSuccess(() -> Component.literal("Set block at " + pos.toShortString() + " to " + bareName), true);
        return 1;
    }

    private static int fill(CommandSourceStack source, BlockPos from, BlockPos to, String bareName) {
        Optional<Block> block = NamespaceResolver.resolveExact(BuiltInRegistries.BLOCK, bareName);
        if (block.isEmpty()) {
            source.sendFailure(Component.literal("No block found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());

        long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > MAX_FILL_VOLUME) {
            source.sendFailure(Component.literal("Fill volume too large (" + volume + " blocks, max " + MAX_FILL_VOLUME + ")."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        BlockState state = block.get().defaultBlockState();
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), state);
                    count++;
                }
            }
        }
        int finalCount = count;
        source.sendSuccess(() -> Component.literal("Filled " + finalCount + " blocks with " + bareName), true);
        return count;
    }
}
