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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;
import java.awt.Color;
import net.runelite.client.config.Alpha;

@ConfigGroup("regenmeter")
public interface LiteRegenMeterConfig extends Config
{
	@ConfigItem(
			keyName = "showHitpoints",
			name = "Display Hitpoints Regen",
			description = "Enables a regeneration bar below the hitpoints orb to indicate hitpoint recovery.",
			position = 0
	)
	default boolean showHitpoints()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showWhenNoChange",
			name = "Always Show Hitpoints Regen",
			description = "Displays the hitpoints regeneration bar even when the hitpoints are full and there is no change.",
			position = 1
	)
	default boolean showWhenNoChange()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyBeforeHpRegenDuration",
			name = "Hitpoint Regen Notification",
			description = "Sets a notification time (in seconds) before the next hitpoint regeneration occurs. A value of 0 disables notifications.",
			position = 2
	)
	@Units(Units.SECONDS)
	default int getNotifyBeforeHpRegenSeconds()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
			keyName = "hitpointsColor",
			name = "Hitpoints Bar Color",
			description = "Color of the hitpoints regeneration bar.",
			position = 3
	)
	default Color getHitpointsColor()
	{
		return Color.RED; // Default color
	}

	@ConfigItem(
			keyName = "showSpecial",
			name = "Display Spec. Attack Regen",
			description = "Enables a regeneration bar below the Special Attack orb to indicate special attack recovery.",
			position = 4
	)
	default boolean showSpecial()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "specialColor",
			name = "Spec. Attack Bar Color",
			description = "Color of the special attack regeneration bar.",
			position = 5
	)
	default Color getSpecialColor()
	{
		return Color.CYAN; // Default color
	}

	@ConfigItem(
			keyName = "lineThickness",
			name = "Line Thickness",
			description = "Sets the thickness of the regeneration bars.",
			position = 6
	)
	@Units(Units.PIXELS)
	default LineThickness getLineThickness() {
		return LineThickness.MEDIUM; // Default thickness
	}

	@ConfigItem(
			keyName = "barWidth",
			name = "Bar Width",
			description = "Choose the width of the regeneration bars.",
			position = 7
	)
	default BarWidth getBarWidth() {
		return BarWidth.NORMAL; // Default width
	}

	// Enum for bar width options
	public enum BarWidth {
		NORMAL,
		WIDER;
	}
	@ConfigItem(
			keyName = "showBackdrops",
			name = "Show Backdrops",
			description = "Enables or disables the display of the backdrop behind the regeneration bars.",
			position = 8
	)
	default boolean showBackdrops()
	{
		return true; // Default value to show backdrops
	}
	@Alpha
	@ConfigItem(
			keyName = "backdropColor",
			name = "Regen area Color",
			description = "Color of the backdrop that represents the area to fill the regeneration bar.",
			position = 9
	)
	default Color getBackdropColor()
	{
		return new Color(0x505050); // Default backdrop color
	}

}