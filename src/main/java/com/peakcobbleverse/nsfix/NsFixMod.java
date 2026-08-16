package com.peakcobbleverse.nsfix;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class NsFixMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            IGiveCommand.register(dispatcher);
            ISummonCommand.register(dispatcher);
            IEffectCommand.register(dispatcher);
            ISoundAndParticleCommands.register(dispatcher);
            IBlockCommands.register(dispatcher);
        });
    }
}
