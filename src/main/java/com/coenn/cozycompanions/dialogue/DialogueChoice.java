package com.coenn.cozycompanions.dialogue;

import net.minecraft.network.chat.Component;

/**
 * A dialogue choice represents an option the player can select when
 * interacting with a villager.  Each choice has a display component,
 * a change in bond value, and optionally a change in mood.  When
 * selected, the choice may trigger follow‑up dialogue or a mini‑quest.
 */
public record DialogueChoice(Component text, int bondChange, Mood moodChange) {
    /**
     * Creates a simple choice that only modifies the bond value without
     * altering mood.
     *
     * @param text the option text shown to the player
     * @param bondChange the amount of bond to add (or subtract)
     */
    public DialogueChoice(Component text, int bondChange) {
        this(text, bondChange, null);
    }
}