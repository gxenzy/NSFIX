package com.peakcobbleverse.nsfix.mixin;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Vanilla's SharedSuggestionProvider#filterResources only lets you tab-complete
 * a bare, unqualified path (e.g. "diamond" instead of "minecraft:diamond") for
 * items whose namespace happens to be "minecraft". Every modded namespace
 * (cobblemon:, lumymon:, etc.) is skipped unless you type the full
 * "namespace:path" yourself.
 * <p>
 * This mixin removes that restriction: if no ":" has been typed yet, ANY
 * resource whose namespace OR path matches what you've typed so far is
 * suggested, regardless of which mod registered it.
 */
@Mixin(SharedSuggestionProvider.class)
public interface SharedSuggestionProviderMixin {

    /**
     * @author PEAKCobbleverse
     * @reason Suggest resources from every mod namespace when typing an
     * unqualified (no ":") path, instead of only the "minecraft" namespace.
     */
    @Overwrite
    static <T> void filterResources(Iterable<T> resources, String remaining, Function<T, ResourceLocation> function, Consumer<T> action) {
        boolean hasNamespace = remaining.indexOf(':') > -1;

        for (T resource : resources) {
            ResourceLocation id = function.apply(resource);

            if (hasNamespace) {
                // User already typed a namespace, e.g. "cobblemon:dre" -> match the full "namespace:path" string.
                if (matchesSubStr(remaining, id.toString())) {
                    action.accept(resource);
                }
            } else {
                // No namespace typed yet -> match against EITHER the namespace itself (typing "cobble" matches
                // "cobblemon:...") OR the bare path from ANY namespace (typing "dream" matches
                // "cobblemon:dream_ball" too, not just something under "minecraft:").
                if (matchesSubStr(remaining, id.getNamespace()) || matchesSubStr(remaining, id.getPath())) {
                    action.accept(resource);
                }
            }
        }
    }

    /**
     * Same underscore-aware substring matcher vanilla uses internally, so
     * "oak_seed" style partial matches (matching after an underscore) keep working.
     */
    private static boolean matchesSubStr(String remaining, String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); i++) {
            i = candidate.indexOf('_', i);
            if (i < 0) {
                return false;
            }
        }
        return true;
    }
}
