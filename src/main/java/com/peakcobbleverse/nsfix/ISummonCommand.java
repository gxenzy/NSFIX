package com.peakcobbleverse.nsfix;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public final class ISummonCommand {

    private ISummonCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isummon")
                .requires(Permissions.require("nsfix.isummon", 2))
                .then(Commands.argument("entity", StringArgumentType.word())
                        .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.ENTITY_TYPE, builder))
                        .executes(ctx -> summon(ctx.getSource(), StringArgumentType.getString(ctx, "entity")))
                )
        );
    }

    private static int summon(CommandSourceStack source, String bareName) {
        Optional<EntityType<?>> type = NamespaceResolver.resolveExact(BuiltInRegistries.ENTITY_TYPE, bareName);
        if (type.isEmpty()) {
            source.sendFailure(Component.literal("No entity found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        Entity entity = type.get().create(source.getLevel());
        if (entity == null) {
            source.sendFailure(Component.literal("Could not create entity for '" + bareName + "'."));
            return 0;
        }
        entity.snapTo(source.getPosition().x, source.getPosition().y, source.getPosition().z, entity.getYRot(), entity.getXRot());
        source.getLevel().addFreshEntity(entity);
        source.sendSuccess(() -> Component.literal("Summoned " + entity.getName().getString()), true);
        return 1;
    }
}
