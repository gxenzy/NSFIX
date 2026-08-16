package com.peakcobbleverse.nsfix;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;

public final class ISoundAndParticleCommands {

    private ISoundAndParticleCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("iplaysound")
                .requires(Permissions.require("nsfix.iplaysound", 2))
                .then(Commands.argument("sound", StringArgumentType.word())
                        .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.SOUND_EVENT, builder))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> playSound(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "sound")))
                        )
                )
        );

        dispatcher.register(Commands.literal("iparticle")
                .requires(Permissions.require("nsfix.iparticle", 2))
                .then(Commands.argument("particle", StringArgumentType.word())
                        .suggests((ctx, builder) -> NamespaceResolver.suggest(BuiltInRegistries.PARTICLE_TYPE, builder))
                        .executes(ctx -> spawnParticle(ctx.getSource(), StringArgumentType.getString(ctx, "particle")))
                )
        );
    }

    private static int playSound(CommandSourceStack source, ServerPlayer target, String bareName) {
        Optional<SoundEvent> sound = NamespaceResolver.resolveExact(BuiltInRegistries.SOUND_EVENT, bareName);
        if (sound.isEmpty()) {
            source.sendFailure(Component.literal("No sound found matching '" + bareName + "' in any loaded mod."));
            return 0;
        }
        target.playNotifySound(sound.get(), SoundSource.MASTER, 1.0F, 1.0F);
        source.sendSuccess(() -> Component.literal("Played " + bareName + " to " + target.getName().getString()), true);
        return 1;
    }

    private static int spawnParticle(CommandSourceStack source, String bareName) {
        Optional<ParticleType<?>> particleType = NamespaceResolver.resolveExact(BuiltInRegistries.PARTICLE_TYPE, bareName);
        if (particleType.isEmpty() || !(particleType.get() instanceof ParticleOptions options)) {
            source.sendFailure(Component.literal("No simple particle found matching '" + bareName + "' (complex particles that need extra data aren't supported by this shortcut)."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        level.sendParticles(options, source.getPosition().x, source.getPosition().y, source.getPosition().z, 10, 0.5, 0.5, 0.5, 0.0);
        source.sendSuccess(() -> Component.literal("Spawned particle " + bareName), true);
        return 1;
    }
}
