package com.coenn.cozycompanions.client.screen;

import com.coenn.cozycompanions.dialogue.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.ArrayList;
import java.util.List;

/**
 * An interactive dialogue UI for conversing with villagers.  This screen
 * features a typewriter effect, animated portrait, scrollable history and
 * branching choices based on the player's bond with the villager.  It does
 * not pause the game and draws its own dimmed background without using
 * Screen.renderBackground, thus avoiding the blur crash on modern versions
 * of Minecraft.  Many additional systems such as mood, memory, gifts and
 * mini‑quests can be layered on top of the underlying dialogue registry.
 */
public class VillagerDialogueScreen extends Screen {
    private final Villager villager;
    /** Current bond value with this villager.  On the client this is a
     * placeholder – persistence and syncing should be implemented server side. */
    private int bond = 0;
    /** The villager's current mood. */
    private Mood mood = Mood.NEUTRAL;
    /** History of previously spoken entries. */
    private final List<DialogueEntry> history = new ArrayList<>();
    /** Scroll offset for the history panel. */
    private int scrollOffset = 0;
    /** The entry currently being typed out. */
    private DialogueEntry currentEntry;
    /** The portion of the current entry's text that has been typed so far. */
    private String typedText = "";
    /** Index of the next character to type. */
    private int typewriterIndex = 0;
    /** Tick counter used to control typewriter speed. */
    private int typewriterTick = 0;
    /** Ticks per character; punctuation triggers longer pauses. */
    private int ticksPerChar = 2;
    /** Track blink state for the portrait animation. */
    private int blinkTimer = 0;
    private boolean eyesClosed = false;
    /** Track mouth animation while talking. */
    private boolean mouthOpen = false;
    /** Buttons representing choices; re‑created for each entry. */
    private final List<Button> optionButtons = new ArrayList<>();

   /** Tick counter used to drive portrait bobbing. */
    private int bobTick = 0;

    /** Texture for the villager portrait. */
    private static final ResourceLocation PORTRAIT_TEX = new ResourceLocation("cozycompanions", "textures/gui/villager_portrait.png");
 
  
    public VillagerDialogueScreen(Villager villager) {
     
        super(Component.translatable("screen.cozyconversation.title"));
        this.villager = villager;
    }

    @Override
    protected void init() {
        // When the screen is opened we select the first dialogue entry based on
        // the current bond and mood.  Clear any prior state.
        this.history.clear();
        this.scrollOffset = 0;
        this.selectNextEntry();
        this.rebuildOptionButtons();
    }

    /**
     * Selects a new entry from the registry and resets the typewriter state.
     */
    private void selectNextEntry() {
        this.currentEntry = DialogueRegistry.getRandomEntry(villager, bond, mood);
        if (this.currentEntry != null) {
            this.history.add(this.currentEntry);
            this.typedText = "";
            this.typewriterIndex = 0;
            this.typewriterTick = 0;
        }
    }

    /**
     * Clears and recreates the choice buttons for the current entry.  Each
     * button applies its own bond and mood changes then selects a new entry.
     */
    private void rebuildOptionButtons() {
        // Remove existing option buttons
        for (Button btn : optionButtons) {
            this.removeWidget(btn);
        }
        this.optionButtons.clear();

        if (this.currentEntry == null) return;
        int panelWidth = 260;
        int x = (this.width - panelWidth) / 2;
        int baseY = (this.height + 100) / 2; // place near bottom of panel
        int btnHeight = 20;
        int spacing = 4;
        int i = 0;
        for (DialogueChoice choice : this.currentEntry.choices) {
            int btnWidth = panelWidth - 20;
            int y = baseY + i * (btnHeight + spacing);
            Button btn = Button.builder(choice.text(), b -> {
                // Play a subtle click sound using built‑in UI sound
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                // Apply bond and mood changes
                bond += choice.bondChange();
                if (choice.moodChange() != null) {
                    mood = choice.moodChange();
                }
                // Clamp bond between 0 and 100
                if (bond < 0) bond = 0;
                if (bond > 100) bond = 100;
  
                // Immediately select a new entry and update buttons
                selectNextEntry();
                rebuildOptionButtons();
            }).bounds(x + 10, y, btnWidth, btnHeight).build();
            this.addRenderableWidget(btn);
            this.optionButtons.add(btn);
            i++;
        }
    }

    @Override
    public void tick() {
        super.tick();
                bobTick++;
        
        // Typewriter logic: progress along the text at a fixed pace, with
        // longer delays for punctuation to simulate natural speech.
        if (this.currentEntry != null && typewriterIndex < this.currentEntry.text.getString().length()) {
            char[] chars = this.currentEntry.text.getString().toCharArray();
            typewriterTick++;
            int delay = ticksPerChar;
            if (typewriterIndex > 0) {
                char prev = chars[typewriterIndex - 1];
                if (prev == ',' || prev == ';') delay = 6;
                if (prev == '.' || prev == '!' || prev == '?') delay = 12;
            }
            if (typewriterTick >= delay) {
                typewriterTick = 0;
                this.typedText = this.currentEntry.text.getString().substring(0, ++typewriterIndex);
                // open mouth while typing
                this.mouthOpen = !this.mouthOpen;
                // play subtle talk tick (rate limited)
                if (this.typewriterIndex % 3 == 0) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_AMBIENT, 0.3F));
                }
            }
        } else {
            // When typing is complete ensure mouth is closed
            this.mouthOpen = false;
        }
        // Blink animation: close eyes briefly every few seconds
        blinkTimer++;
        if (blinkTimer > 120) {
            eyesClosed = true;
        }
        if (blinkTimer > 125) {
            eyesClosed = false;
            blinkTimer = 0;
        }
    }

    /**
     * Handles mouse scroll input.  We intentionally avoid an @Override
     * annotation here because the method signature for mouse scrolling
     * changed between Minecraft versions (additional horizontal scroll
     * parameter was added).  By omitting @Override, this method will
     * compile on both old and new versions, and still provide scroll
     * behaviour.  Horizontal scrolling is ignored.
     *
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @param deltaX horizontal scroll amount (ignored)
     * @param deltaY vertical scroll amount
     * @return true if the scroll was handled
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Determine vertical scroll.  In versions prior to 1.21 the third
        // parameter represents vertical scroll, so treat deltaX as the
        // vertical delta if deltaY is zero.
        double vertical = deltaY;
        if (vertical == 0) {
            vertical = deltaX;
        }
        // Adjust scroll offset for history panel; clamp within bounds
        this.scrollOffset += (int) -vertical * 10;
        int max = Math.max(0, this.history.size() * 12 - 80);
        if (this.scrollOffset < 0) this.scrollOffset = 0;
        if (this.scrollOffset > max) this.scrollOffset = max;
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Dimmed background fill (safe alternative to renderBackground)
        g.fill(0, 0, this.width, this.height, 0x88000000);
        // Panel dimensions
        int panelWidth = 260;
        int panelHeight = 180;
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;
        // Outer panel background
        g.fill(x, y, x + panelWidth, y + panelHeight, 0xCC1E1E1E);
        g.fill(x + 2, y + 2, x + panelWidth - 2, y + panelHeight - 2, 0xCC2C2C2C);
        // Title and bond bar
        String name = villager.getName().getString();
        g.drawString(this.font, Component.translatable("screen.cozyconversation.talking_to", name), x + 8, y + 8, 0xFFEFE7D6);
        // Bond bar
        int barX = x + 8;
        int barY = y + 20;
        int barW = 100;
        int barH = 6;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF4A4A4A);
        int filled = (int) (barW * (bond / 100.0f));
        g.fill(barX, barY, barX + filled, barY + barH, 0xFF76C893);
        g.drawString(this.font, Component.translatable("screen.cozyconversation.bond", bond), barX + barW + 6, barY - 2, 0xFFAED6C1);
        // Portrait box at top right
        int portraitSize = 48;
        int portraitX = x + panelWidth - portraitSize - 8;
        int portraitY = y + 6;
        renderPortrait(g, portraitX, portraitY, portraitSize, portraitSize);
        // History area box
        int histX = x + 8;
        int histY = y + 32;
        int histW = panelWidth - 16;
        int histH = 60;
        g.fill(histX, histY, histX + histW, histY + histH, 0xCC141414);
        // Clip history to panel and draw entries with scroll offset
        g.enableScissor(histX, histY, histX + histW, histY + histH);
        int yy = histY + 4 - this.scrollOffset;
        for (DialogueEntry entry : this.history) {
            var lines = this.font.split(entry.text, histW - 8);
            for (var line : lines) {
                g.drawString(this.font, line, histX + 4, yy, 0xFFEFE7D6);
                yy += 10;
            }
            yy += 4;
        }
        g.disableScissor();
        // Current dialogue typing area
        int curX = histX;
        int curY = histY + histH + 4;
        int curW = histW;
        int curH = 36;
        g.fill(curX, curY, curX + curW, curY + curH, 0xCC1A1A1A);
        // Draw typed text with wrap
        if (this.currentEntry != null) {
            var lines = this.font.split(Component.literal(this.typedText), curW - 8);
            int yLine = curY + 4;
            for (var line : lines) {
                g.drawString(this.font, line, curX + 4, yLine, 0xFFEFE7D6);
                yLine += 10;
            }
        }
        // Draw choice buttons and any children
        super.render(g, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders a simple animated portrait to represent the villager.  This
     * implementation does not use the actual villager texture to remain
     * lightweight and avoid external dependencies.  Instead it renders a
     * stylised face with blinking eyes and a mouth that opens while typing.
     * An overlay tint is applied based on the villager's mood.
     */
    private void renderPortrait(GuiGraphics g, int x, int y, int w, int h) {
        // Base face rectangle
        g.fill(x, y, x + w, y + h, 0xFF896E46);
        // Mood tint overlay
        int tint;
        switch (mood) {
            case HAPPY -> tint = 0x4433DD77;
            case ANNOYED -> tint = 0x44DD3333;
            case SHY -> tint = 0x444477DD;
            case EXCITED -> tint = 0x44FFD700;
            default -> tint = 0x00000000;
        }
        g.fill(x, y, x + w, y + h, tint);
        // Eyes: either closed or open with pupils
        int eyeW = w / 6;
        int eyeH = h / 8;
        int eyeY = y + h / 3;
        int leftEyeX = x + w / 4 - eyeW / 2;
        int rightEyeX = x + 3 * w / 4 - eyeW / 2 - eyeW;
        if (eyesClosed) {
            // closed eyes are a single horizontal line
            g.fill(leftEyeX, eyeY + eyeH / 2, leftEyeX + eyeW, eyeY + eyeH / 2 + 1, 0xFF000000);
            g.fill(rightEyeX, eyeY + eyeH / 2, rightEyeX + eyeW, eyeY + eyeH / 2 + 1, 0xFF000000);
        } else {
            // sclera
            g.fill(leftEyeX, eyeY, leftEyeX + eyeW, eyeY + eyeH, 0xFFFFFFFF);
            g.fill(leftEyeX + 2, eyeY + 2, leftEyeX + eyeW - 2, eyeY + eyeH - 2, 0xFF000000);
            g.fill(rightEyeX, eyeY, rightEyeX + eyeW, eyeY + eyeH, 0xFFFFFFFF);
            g.fill(rightEyeX + 2, eyeY + 2, rightEyeX + eyeW - 2, eyeY + eyeH - 2, 0xFF000000);
        }
        // Mouth: open or closed
        int mouthW = w / 3;
        int mouthH = h / 10;
        int mouthX = x + w / 2 - mouthW / 2;
        int mouthY = y + (int) (h * 0.65f);
        if (mouthOpen) {
            g.fill(mouthX, mouthY, mouthX + mouthW, mouthY + mouthH, 0xFF000000);
        } else {
            g.fill(mouthX, mouthY + mouthH / 2, mouthX + mouthW, mouthY + mouthH / 2 + 1, 0xFF000000);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
