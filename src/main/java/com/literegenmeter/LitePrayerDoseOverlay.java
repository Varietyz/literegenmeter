package com.literegenmeter;

import lombok.AccessLevel;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Skill;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

class LitePrayerDoseOverlay extends Overlay
{
    private static final float PULSE_TIME = 2f * Constants.GAME_TICK_LENGTH;
    private static final double DIAMETER = 26D;
    private static final int OFFSET = 27;

    private final Client client;
    private final LiteRegenMeterPlugin plugin;
    private final LiteRegenMeterConfig config;
    private Instant startOfLastTick = Instant.now();
    private boolean trackTick = true;

    @Setter(AccessLevel.PACKAGE)
    private int restoreAmount;

    @Inject
    private LitePrayerDoseOverlay(final Client client, final LiteRegenMeterPlugin plugin, final LiteRegenMeterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    void onTick()
    {
        if (trackTick)
        {
            startOfLastTick = Instant.now(); // Reset the tick timer
            trackTick = false;
        }
        else
        {
            trackTick = true;
        }
    }
    @Override
    public Dimension render(Graphics2D graphics)
    {
        final Widget xpOrb = client.getWidget(ComponentID.MINIMAP_QUICK_PRAYER_ORB);
        if (xpOrb == null || xpOrb.isHidden())
        {
            return null;
        }

        final Rectangle bounds = xpOrb.getBounds();
        if (bounds.getX() <= 0)
        {
            return null;
        }

        if (!config.showPrayerDoseIndicator() || restoreAmount == 0)
        {
            return null;
        }

        final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        final int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);

        final int prayerPointsMissing = maxPrayer - currentPrayer;
        if (prayerPointsMissing <= 0 || prayerPointsMissing < restoreAmount)
        {
            return null;
        }

        // Get user-configured bar width and line thickness
        LiteRegenMeterConfig.BarWidth barWidthOption = config.getBarWidth();
        final int BAR_WIDTH = (barWidthOption == LiteRegenMeterConfig.BarWidth.NORMAL) ? 25 : 34;
        int lineThickness = config.getLineThickness().getValue();

        int barX;
        switch (config.barXPosition()) {
            case LEFT:
                barX = (int) (bounds.x + OFFSET - 1 - 25); // Left config
                break;
            case MIDDLE:
                // Adjust barX based on the width configuration for MIDDLE
                if (config.getBarWidth() == LiteRegenMeterConfig.BarWidth.WIDER) {
                    barX = (int) (bounds.x + OFFSET - 1 - 20); // Wider option
                } else {
                    barX = (int) (bounds.x + OFFSET - 1 - 15); // Normal option
                }
                break;
            case RIGHT:
                barX = (int) (bounds.x + OFFSET - 3 - (BAR_WIDTH - 25) - 4); // Right config
                break;
            default:
                barX = (int) (bounds.x + OFFSET - 1); // Fallback
        }

// Determine bar Y position based on configuration
        int barY;
        switch (config.barYPosition()) {
            case DETACHED:
                barY = (int) (bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 2); // Floating
                break;
            case ATTACHED:
            default:
                barY = (int) (bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2); // Attached
        }

        final long timeSinceLastTick = Duration.between(startOfLastTick, Instant.now()).toMillis();
        final float tickProgress = Math.min(timeSinceLastTick / PULSE_TIME, 1); // Cap between 0 and 1

        // Calculate the flashing effect for visibility
        float flash = (float) Math.sin(tickProgress * Math.PI * 2); // Flashing effect using sine wave
        float alpha = Math.max(0, 0.5f + 0.5f * flash); // Alpha value ranging from 0 to 1

        // Create a color for the bar that maintains its original RGB values but adjusts alpha
        Color barColor = config.prayerDoseOrbStartColor();
        Color flashingColor = new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), (int) (alpha * 255)); // Adjust alpha for fill

        Color outlineColor = new Color(0x171717); // Dark gray outline color
        Color flashingOutlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (alpha * 255)); // Adjust alpha for outline


        // Fixed bar width based on configuration
        double fixedBarWidth = BAR_WIDTH; // Keep fixed width

        // Create a rectangle for the bar
        Rectangle bar = new Rectangle(barX, barY, (int) fixedBarWidth, lineThickness); // Create the bar

        // Draw the background (outer)
        Rectangle background = new Rectangle(barX - 1, barY - 1, (int) fixedBarWidth + 2, lineThickness + 2); // Background width is fixed to BAR_WIDTH + 2 for the outline

        // Fill the entire background with the outline color
        graphics.setColor(flashingOutlineColor);
        graphics.fill(background); // Draw the outer background first

        // Fill the entire bar with the flashing color
        graphics.setColor(flashingColor);
        graphics.fill(bar); // Fill the bar with flashing color

        // Draw the outline with the flashing outline color
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(flashingOutlineColor); // Set outline color to flashing
        graphics.drawRect(barX - 1, barY - 1, (int) fixedBarWidth + 1, lineThickness + 1); // Draw outline

        return null;
    }
}