# Namespaceless Suggestions (PEAKCobbleverse custom mod)

Removes the "minecraft namespace only" restriction on unqualified tab-complete,
so typing `dream` after `/give ZenIX cobblemon:` (or even without the prefix at
all while typing) suggests items from ANY mod's namespace, not just vanilla.

Built for **Minecraft 1.21.1, Fabric**.

## How to get a working .jar from this (no local setup needed)

1. **Create a new empty repository on GitHub** (e.g. `peakcobbleverse-nsfix`).
2. **Upload every file from this folder**, keeping the exact folder structure
   (the `.github/workflows/build.yml` file especially — GitHub only picks up
   workflows from that exact path).
3. **Push/commit to the `main` branch.** GitHub Actions will trigger
   automatically — you'll see a run start under the repo's **Actions** tab.
4. **Wait ~2-5 minutes.** If it goes green, click into the run →
   **Summary** → under **Artifacts**, download `namespaceless-suggestions-jar`.
   Unzip it — inside is the actual `.jar` file.
5. **Drop that jar into your server's `/mods` folder** alongside Fabric API,
   restart, done. No client-side install needed for this one (it only affects
   what the server suggests back to you when tab-completing).

## If the build fails (red ❌ instead of green ✅)

Click into the failed run → click the `build` job → read the red error text.
The two most likely causes, both are one-line fixes:

- **Yarn mappings version not found** — `gradle.properties` currently pins
  `yarn_mappings=1.21.1+build.3`. If that exact build number doesn't exist,
  Loom's error will say so directly. Check
  https://maven.fabricmc.net/net/fabricmc/yarn/ for the actual latest
  `1.21.1+build.N` folder and update the number in `gradle.properties`.
- **Loom plugin version mismatch** — `build.gradle` pins
  `id 'fabric-loom' version '1.7-SNAPSHOT'`. If Gradle can't resolve it, swap
  for the latest stable release listed at
  https://fabricmc.net/develop/ (Loom section).

Paste me the exact red error text from the Actions log and I'll fix the
specific line — I can't run this build myself since I don't have the internet
access from my side to hit Fabric's Maven repo, but GitHub's runners do, so
that log is the real source of truth once it exists.

## What it actually changes

Mixes into `SharedSuggestionProvider#filterResources` (the same method the
proven, MIT-licensed [SuggestionProviderFix](https://github.com/Harleyoc1/SuggestionProviderFix)
mod patches) and removes the check that limited unqualified path-matching to
the `minecraft` namespace only. See the comments in
`SharedSuggestionProviderMixin.java` for the exact logic.

**Note:** you still have to type the full `namespace:item_id` to actually
*run* `/give` — Minecraft's command parser requires that regardless. This mod
only fixes what shows up in the Tab-complete suggestion list while you're
typing, same as the mods we looked at earlier that don't support 1.21.1.
