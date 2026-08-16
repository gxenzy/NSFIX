package com.peakcobbleverse.nsfix;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class IGiveCommand {

    private IGiveCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("igive")
                .requires(Permissions.require("nsfix.igive", 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("item", StringArgumentType.word())
                                .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.ITEM, builder))
                                .executes(ctx -> give(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "item"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 6400))
                                        .executes(ctx -> give(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "item"), IntegerArgumentType.getInteger(ctx, "count")))
                                )
                        )
                )
        );
    }

    private static int give(CommandSourceStack source, ServerPlayer target, String bareName, int count) {
        Optional<Item> item = NamespaceResolver.resolveExact(BuiltInRegistries.ITEM, bareName);
        if (item.isEmpty()) {
            source.sendFailure(Component.literal("No item found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        ItemStack stack = new ItemStack(item.get(), count);
        boolean added = target.getInventory().add(stack);
        if (!added) {
            target.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("Gave " + count + "x " + stack.getHoverName().getString() + " to " + target.getName().getString()), true);
        return count;
    }
}
