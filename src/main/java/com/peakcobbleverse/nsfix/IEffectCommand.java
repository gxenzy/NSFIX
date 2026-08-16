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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public final class IEffectCommand {

    private IEffectCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ieffect")
                .requires(Permissions.require("nsfix.ieffect", 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("effect", StringArgumentType.word())
                                .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.MOB_EFFECT, builder))
                                .executes(ctx -> apply(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "effect"), 30, 0))
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000))
                                        .executes(ctx -> apply(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "seconds"), 0))
                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                .executes(ctx -> apply(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "seconds"), IntegerArgumentType.getInteger(ctx, "amplifier")))
                                        )
                                )
                        )
                )
        );
    }

    private static int apply(CommandSourceStack source, ServerPlayer target, String bareName, int seconds, int amplifier) {
        Optional<MobEffect> effect = NamespaceResolver.resolveExact(BuiltInRegistries.MOB_EFFECT, bareName);
        if (effect.isEmpty()) {
            source.sendFailure(Component.literal("No effect found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        target.addEffect(new MobEffectInstance(effect.get(), seconds * 20, amplifier));
        source.sendSuccess(() -> Component.literal("Applied " + bareName + " to " + target.getName().getString()), true);
        return 1;
    }
}
