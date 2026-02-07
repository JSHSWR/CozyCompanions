package com.coenn.cozycompanions.dialogue;

/**
 * Represents the emotional state of a villager.  The mood can influence both the
 * appearance of the portrait (colour tint or expression) as well as the
 * content chosen from the dialogue registry.  Bond‑changing actions and
 * random events may transition the villager between different moods.
 */
public enum Mood {
    /**
     * The villager is content and happy.  Happy moods tend to produce
     * upbeat and friendly dialogue and may grant the player small perks.
     */
    HAPPY,
    /**
     * The villager is neutral – neither particularly pleased nor annoyed.
     * This is the baseline state.
     */
    NEUTRAL,
    /**
     * The villager is annoyed.  Dialogue will be curt and the villager may
     * request space or react negatively to certain choices.
     */
    ANNOYED,
    /**
     * The villager is shy.  Shy moods produce shorter, more hesitant
     * responses.  Over time, shy villagers may warm up as the bond increases.
     */
    SHY,
    /**
     * The villager is excited, either because of a special event or high bond.
     * Excited dialogue is lively and animated.
     */
    EXCITED;
}