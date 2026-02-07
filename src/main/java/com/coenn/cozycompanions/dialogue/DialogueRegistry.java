package com.coenn.cozycompanions.dialogue;

import com.google.common.collect.ImmutableList;
// Note: we avoid using ResourceLocation and Villager types from incorrect
// packages; see below for correct imports.
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Holds a registry of all possible dialogue entries for the Cozy Companions
 * mod.  Entries are created during static initialization and then served
 * through lookup functions.  If more complex or data‑driven behaviour is
 * desired, this class can be extended to load JSON from the resources folder.
 */
public final class DialogueRegistry {
    /** The complete list of registered entries. */
    private static final List<DialogueEntry> ALL_ENTRIES;
    /** Random source used to select entries. */
    private static final Random RANDOM = new Random();

    static {
        List<DialogueEntry> entries = new ArrayList<>();

        // Generate basic conversational lines for each bond tier.  Each
        // translation key must be present in the en_us.json file.  The id is
        // deterministic to assist with debugging and analytics.
        int[][] tiers = new int[][] {
                {0, 9}, {10, 24}, {25, 49}, {50, 74}, {75, 100}
        };
        for (int tierIndex = 0; tierIndex < tiers.length; tierIndex++) {
            int[] bounds = tiers[tierIndex];
            for (int i = 0; i < 20; i++) {
                String id = String.format(Locale.ROOT, "tier%d_line%02d", tierIndex, i);
                String key = "dialogue.cozy." + id;
                Mood mood = Mood.NEUTRAL;
                if (tierIndex >= 3) {
                    // high tiers yield happier lines
                    mood = Mood.HAPPY;
                }
                // At the highest tier occasionally generate excited lines
                if (tierIndex == 4 && i % 5 == 0) {
                    mood = Mood.EXCITED;
                }
                // Create a list of choices for higher tiers (>1)
                List<DialogueChoice> choices = new ArrayList<>();
                if (bounds[0] >= 10) {
                    // Provide three generic options: chat more, compliment, gift
                    choices.add(new DialogueChoice(Component.translatable("dialogue.choice.chat_more"), 1));
                    choices.add(new DialogueChoice(Component.translatable("dialogue.choice.compliment"), 2, Mood.HAPPY));
                    choices.add(new DialogueChoice(Component.translatable("dialogue.choice.give_gift"), 3, Mood.HAPPY));
                }
                DialogueEntry entry = new DialogueEntry(id,
                        Component.translatable(key),
                        bounds[0], bounds[1],
                        null,
                        mood,
                        choices);
                entries.add(entry);
            }
        }
        // Add a few profession‑specific lines for variety (e.g. farmer)
        String farmer = "minecraft:farmer";
        entries.add(new DialogueEntry("farmer_tip",
                Component.translatable("dialogue.cozy.farmer_tip"), 0, 100, farmer, Mood.NEUTRAL, ImmutableList.of()));
        entries.add(new DialogueEntry("farmer_compliment",
                Component.translatable("dialogue.cozy.farmer_compliment"), 10, 100, farmer, Mood.HAPPY,
                List.of(new DialogueChoice(Component.translatable("dialogue.choice.offer_help"), 2, Mood.EXCITED))));

        ALL_ENTRIES = List.copyOf(entries);
    }

    /**
     * Returns a random entry that matches the supplied bond and optional
     * villager properties.  The mood parameter can be used to bias lines
     * associated with that mood.  If no matching entry exists, null is
     * returned.
     */
    public static DialogueEntry getRandomEntry(Villager villager, int bond, Mood mood) {
        // Filter by bond
        List<DialogueEntry> candidates = ALL_ENTRIES.stream()
                .filter(e -> e.matchesBond(bond))
                // VillagerData is a record in newer versions; use the record accessor
                // profession() instead of getProfession().  Cast to string for comparison.
                .filter(e -> e.professionKey == null || villager.getVillagerData().profession().toString().equals(e.professionKey))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        // Further filter by mood to bias selection
        List<DialogueEntry> moodMatches = candidates.stream().filter(e -> e.mood == mood).collect(Collectors.toList());
        if (!moodMatches.isEmpty()) {
            return moodMatches.get(RANDOM.nextInt(moodMatches.size()));
        }
        // Otherwise pick any candidate
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    private DialogueRegistry() {}
}