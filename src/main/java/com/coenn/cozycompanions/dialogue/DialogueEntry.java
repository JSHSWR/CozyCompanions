package com.coenn.cozycompanions.dialogue;

import net.minecraft.network.chat.Component;
import java.util.Collections;
import java.util.List;

/**
 * Represents a line of dialogue that can be spoken by a villager.  Dialogue
 * entries are data‑driven: each entry describes the text, applicable bond
 * range, optional profession, biome or time conditions, the mood associated
 * with the line and any choices that should be presented after the line
 * finishes typing out.
 */
public class DialogueEntry {
    /** An identifier for this entry.  Useful for debugging and analytics. */
    public final String id;
    /** The text to display.  This should be a translation key; use
     * Component.translatable for localisation. */
    public final Component text;
    /** Minimum and maximum bond values (inclusive) for which this entry is
     * appropriate. */
    public final int bondMin;
    public final int bondMax;
    /** Optional profession key restricting this entry to certain villager
     * professions.  If null the entry is universal. */
    /**
     * Optional profession key restricting this entry to certain villager
     * professions.  If null the entry is universal.  We store this as a
     * simple string rather than a ResourceLocation to avoid mapping
     * differences between environments.
     */
    public final String professionKey;
    /** Optional mood associated with this entry.  Mood influences the
     * expression overlay on the portrait. */
    public final Mood mood;
    /** Optional list of choices presented after this line.  Choices may be
     * null or empty if there are no branching options. */
    public final List<DialogueChoice> choices;

    public DialogueEntry(String id,
                         Component text,
                         int bondMin,
                         int bondMax,
                         String professionKey,
                         Mood mood,
                         List<DialogueChoice> choices) {
        this.id = id;
        this.text = text;
        this.bondMin = bondMin;
        this.bondMax = bondMax;
        this.professionKey = professionKey;
        this.mood = mood;
        this.choices = choices == null ? Collections.emptyList() : List.copyOf(choices);
    }

    /**
     * Returns whether this entry is valid for the provided bond value.  Note
     * that profession and other conditions are not checked here.
     */
    public boolean matchesBond(int bond) {
        return bond >= bondMin && bond <= bondMax;
    }
}