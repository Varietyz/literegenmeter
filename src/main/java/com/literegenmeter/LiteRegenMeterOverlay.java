/*
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

import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

class LiteRegenMeterOverlay extends Overlay
{
	private static final Color HITPOINTS_COLOR = brighter(0x9B0703);
	private static final Color SPECIAL_COLOR = brighter(0x1E95B0);
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
			// Use user-configured color for hitpoints bar
			Color hitpointsColor = config.getHitpointsColor();
			renderRegen(g, ComponentID.MINIMAP_HEALTH_ORB, plugin.getHitpointsPercentage(), hitpointsColor);
		}

		if (config.showSpecial())
		{
			// Use user-configured color for special attack bar
			Color specialColor = config.getSpecialColor();
			renderRegen(g, ComponentID.MINIMAP_SPEC_ORB, plugin.getSpecialPercentage(), specialColor);
		}

		return null;
	}

	private void renderRegen(Graphics2D g, @Component int componentId, double percent, Color color) {
		Widget widget = client.getWidget(componentId);
		if (widget == null || widget.isHidden()) {
			return;
		}
		Rectangle bounds = widget.getBounds();

		// Get user-configured line thickness
		LineThickness lineThickness = config.getLineThickness();

		// Get user-configured bar width
		LiteRegenMeterConfig.BarWidth barWidthOption = config.getBarWidth();

		// Set BAR_WIDTH based on user selection
		final int BAR_WIDTH = (barWidthOption == LiteRegenMeterConfig.BarWidth.NORMAL) ? 25 : 34;

		// Calculate the bar width based on percentage
		double barWidth = BAR_WIDTH * percent; // Calculate the width based on percentage

		// Ensure the bar width does not exceed the fixed width
		barWidth = Math.min(barWidth, BAR_WIDTH);

		// Determine bar X position based on configuration
		double barX;
		switch (config.barXPosition()) {
			case LEFT:
				barX = bounds.x + OFFSET - 1 - 22; // Left config
				break;
			case MIDDLE:
				// Adjust barX based on the width configuration for MIDDLE
				if (config.getBarWidth() == LiteRegenMeterConfig.BarWidth.WIDER) {
					barX = bounds.x + OFFSET - 1 - 17; // Wider option
				} else {
					barX = bounds.x + OFFSET - 1 - 12; // Normal option
				}
				break;
			case RIGHT:
				barX = bounds.x + OFFSET - 3 - (BAR_WIDTH - 25) - 1; // Right config
				break;
			default:
				barX = bounds.x + OFFSET - 1; // Fallback
		}


		// Determine bar Y position based on configuration
		double barY;
		switch (config.barYPosition()) {
			case DETACHED:
				barY = bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 3; // Floating
				break;
			case ATTACHED:
			default:
				barY = bounds.y + (bounds.height / 2) + (DIAMETER / 2) - 2 + 1; // Attached
		}

		// Create a rectangle for the bar
		Rectangle bar = new Rectangle((int) barX, (int) barY, (int) barWidth, lineThickness.getValue()); // Set height based on user choice

		// Create a rectangle for the background (outer)
		Rectangle background = new Rectangle((int) (barX - 1), (int) (barY - 1), BAR_WIDTH + 2, lineThickness.getValue() + 2); // Background width is fixed to BAR_WIDTH and add 1 pixel to top and bottom

		// Draw the background and the bar only if the percentage is greater than 0
		if (percent > 0) {
			if (config.showBackdrops()) {
				// Draw the outer border using the default backdrop color
				g.setColor(new Color(0x171717)); // Use the border color
				g.fill(background); // Draw the outer background first
			}

			// Set inner backdrop height based on the line thickness
			int innerHeight = lineThickness.getValue();

			// Check if the backdrop should be drawn based on user configuration
			if (config.showBackdrops()) {
				// Use the user-configured backdrop color if set, otherwise use default
				Color backdropColor = config.getBackdropColor(); // Get the backdrop color from config

				// Create a rectangle for the inner backdrop
				Rectangle innerBackground = new Rectangle((int) (barX), (int) (barY), BAR_WIDTH, innerHeight); // Adjust position for the inner background
				g.setColor(backdropColor); // Use the user-configured backdrop color
				g.fill(innerBackground); // Draw the inner backdrop
			}

			// Set stroke and color for drawing the bar
			g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			g.setColor(color);

			// Draw the bar
			g.fill(bar);
		}
	}
}