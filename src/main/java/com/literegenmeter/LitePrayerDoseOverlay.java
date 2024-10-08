/*
 * Copyright (c) 2024, Smoke (Smoked today) <https://github.com/Varietyz>
 * Copyright (c) 2018, Ethan <https://github.com/shmeeps>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.literegenmeter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
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
            startOfLastTick = Instant.now();
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

        LiteRegenMeterConfig.BarWidth barWidthOption = config.getBarWidth();
        final int BAR_WIDTH = (barWidthOption == LiteRegenMeterConfig.BarWidth.NORMAL) ? 25 : 34;
        int lineThickness = config.getLineThickness().getValue();

        int barX;
        switch (config.barXPosition()) {
            case LEFT:
                barX = (int) (bounds.x + OFFSET - 1 - 25);
                break;
            case MIDDLE:
                if (config.getBarWidth() == LiteRegenMeterConfig.BarWidth.WIDER) {
                    barX = (int) (bounds.x + OFFSET - 1 - 20);
                } else {
                    barX = (int) (bounds.x + OFFSET - 1 - 15);
                }
                break;
            case RIGHT:
                barX = (int) (bounds.x + OFFSET - 3 - (BAR_WIDTH - 25) - 4);
                break;
            default:
                barX = (int) (bounds.x + OFFSET - 1);
        }

        int barY;
        switch (config.barYPosition()) {
            case DETACHED:
                barY = (int) (bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 2);
                break;
            case ATTACHED:
            default:
                barY = (int) (bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2);
        }

        final long timeSinceLastTick = Duration.between(startOfLastTick, Instant.now()).toMillis();
        final float tickProgress = Math.min(timeSinceLastTick / PULSE_TIME, 1);

        float flash = (float) Math.sin(tickProgress * Math.PI * 2);
        float alpha = Math.max(0, 0.5f + 0.5f * flash);

        Color barColor = config.prayerDoseOrbStartColor();
        Color flashingColor = new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), (int) (alpha * 255));

        Color outlineColor = new Color(0x171717);
        Color flashingOutlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), (int) (alpha * 255));

        double fixedBarWidth = BAR_WIDTH;

        Rectangle bar = new Rectangle(barX, barY, (int) fixedBarWidth, lineThickness);

        Rectangle background = new Rectangle(barX - 1, barY - 1, (int) fixedBarWidth + 2, lineThickness + 2);

        graphics.setColor(flashingOutlineColor);
        graphics.fill(background);

        graphics.setColor(flashingColor);
        graphics.fill(bar);

        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(flashingOutlineColor);
        graphics.drawRect(barX - 1, barY - 1, (int) fixedBarWidth + 1, lineThickness + 1);

        return null;
    }

}
