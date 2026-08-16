package com.peakcobbleverse.nsfix;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Shared helper: given a bare word like "dream_ball" (no namespace), searches
 * every registered namespace in a given vanilla registry for a matching path
 * and resolves it, preferring "minecraft:" if there's a tie.
 */
public final class NamespaceResolver {

    private NamespaceResolver() {
    }

    /** Finds the entry whose path exactly equals bareName, across all namespaces. */
    public static <T> Optional<T> resolveExact(Registry<T> registry, String bareName) {
        List<ResourceLocation> matches = new ArrayList<>();
        for (ResourceLocation id : registry.keySet()) {
            if (id.getPath().equals(bareName)) {
                matches.add(id);
            }
        }
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        // Prefer vanilla "minecraft:" if multiple mods happen to share the same bare name.
        matches.sort(Comparator.comparing(id -> !id.getNamespace().equals("minecraft")));
        return registry.getOptional(matches.get(0));
    }

    /** Tab-complete suggestions: bare path names (deduplicated) matching what's typed so far. */
    public static <T> CompletableFuture<Suggestions> suggest(Registry<T> registry, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        Set<String> seen = new LinkedHashSet<>();
        for (ResourceLocation id : registry.keySet()) {
            String path = id.getPath();
            if (path.startsWith(remaining) && seen.add(path)) {
                builder.suggest(path);
            }
        }
        return builder.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggest(Registry<T> registry, CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return suggest(registry, builder);
    }
}
