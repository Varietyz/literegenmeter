/*
 * Copyright (c) 2024, Smoke (Smoked today) <https://github.com/Varietyz>
 * Copyright (c) 2018 Abex
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
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import com.literegenmeter.orbmeters.LineThickness;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class LiteRegenMeterOverlay extends Overlay
{
	private static final double DIAMETER = 26D;
	private static final int OFFSET = 27;

	private final Client client;
	private final LiteRegenMeterPlugin plugin;
	private final LiteRegenMeterConfig config;

	private static Color brighter(int color)
	{
		float[] hsv = new float[3];
		Color.RGBtoHSB(color >>> 16, (color >> 8) & 0xFF, color & 0xFF, hsv);
		return Color.getHSBColor(hsv[0], 1.f, 1.f);
	}

	@Inject
	public LiteRegenMeterOverlay(Client client, LiteRegenMeterPlugin plugin, LiteRegenMeterConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		if (config.showHitpoints())
		{
			Color hitpointsColor = config.getHitpointsColor();
			renderRegen(g, ComponentID.MINIMAP_HEALTH_ORB, plugin.getHitpointsPercentage(), hitpointsColor);
		}

		if (config.showSpecial())
		{
			Color specialColor = config.getSpecialColor();
			renderRegen(g, ComponentID.MINIMAP_SPEC_ORB, plugin.getSpecialPercentage(), specialColor);
		}

		return null;
	}

	private void renderRegen(Graphics2D g, @Component int componentId, double percent, Color color) {
		if (!config.enableRegenMeters()) {
			return;
		}

		Widget widget = client.getWidget(componentId);
		if (widget == null || widget.isHidden()) {
			return;
		}
		Rectangle bounds = widget.getBounds();

		LineThickness lineThickness = config.getLineThickness();

		LiteRegenMeterConfig.BarWidth barWidthOption = config.getBarWidth();

		final int BAR_WIDTH = (barWidthOption == LiteRegenMeterConfig.BarWidth.NORMAL) ? 25 : 34;

		double barWidth = BAR_WIDTH * percent;

		barWidth = Math.min(barWidth, BAR_WIDTH);

		LiteRegenMeterConfig.PackMode mode = config.packMode();
		int vanillaXOffset = (mode == LiteRegenMeterConfig.PackMode.VANILLA) ? -3 : 0;
		int vanillaYOffset = (mode == LiteRegenMeterConfig.PackMode.VANILLA) ? 2 : 0;

		double barX;
		switch (config.barXPosition()) {
			case LEFT:
				barX = bounds.x + OFFSET - 1 - 22 + vanillaXOffset;
				break;
			case MIDDLE:
				if (config.getBarWidth() == LiteRegenMeterConfig.BarWidth.WIDER) {
					barX = bounds.x + OFFSET - 1 - 17;
				} else {
					barX = bounds.x + OFFSET - 1 - 12;
				}
				break;
			case RIGHT:
				barX = bounds.x + OFFSET - 3 - (BAR_WIDTH - 25) - 1 + vanillaXOffset;
				break;
			default:
				barX = bounds.x + OFFSET - 1 + vanillaXOffset;
		}

		double barY;
		switch (config.barYPosition()) {
			case DETACHED:
				if (mode == LiteRegenMeterConfig.PackMode.VANILLA)
				{
					barY = bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 1 + vanillaYOffset;
				} else {
					barY = bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 3 + vanillaYOffset;
				}
				break;
			case ATTACHED:
			default:
				barY = bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 1 + vanillaYOffset;
		}

		Rectangle bar = new Rectangle((int) barX, (int) barY, (int) barWidth, lineThickness.getValue());

		Rectangle background = new Rectangle((int) (barX - 1), (int) (barY - 1), BAR_WIDTH + 2, lineThickness.getValue() + 2);

		if (percent > 0) {
			if (config.showBackdrops()) {
				g.setColor(new Color(0x171717));
				g.fill(background);
			}

			int innerHeight = lineThickness.getValue();

			if (config.showBackdrops()) {
				Color backdropColor = config.getBackdropColor();

				Rectangle innerBackground = new Rectangle((int) (barX), (int) (barY), BAR_WIDTH, innerHeight);
				g.setColor(backdropColor);
				g.fill(innerBackground);
			}

			g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			g.setColor(color);

			g.fill(bar);
		}
	}

}
