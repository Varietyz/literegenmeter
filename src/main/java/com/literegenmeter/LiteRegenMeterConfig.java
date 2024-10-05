/*
 * Copyright (c) 2024, Smoke (Smoked today) <https://github.com/Varietyz>
 * Copyright (c) 2019, Jos <Malevolentdev@gmail.com>
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

import net.runelite.client.config.*;
import com.literegenmeter.config.LiteStatBarsMode;
import com.literegenmeter.orbmeters.LineThickness;

import java.awt.*;


@ConfigGroup("regenmeter")
public interface LiteRegenMeterConfig extends Config
{
	@ConfigSection(
			name = "Display (Regen Meters)",
			description = "Choose to display the regen meters.",
			position = 0
	)
	String RegenMeterDisplaySettingsSection = "RegenMeterDisplaySettings";

	@ConfigSection(
			name = "Display (Stat Bars)",
			description = "Choose to display the stat bars.",
			position = 3
	)
	String MainBarSettingsSection = "MainBarSettings";

	@ConfigSection(
			name = "Settings (Regen Meters)",
			description = "Configuration for the regen meters.",
			position = 1
	)
	String RegenMeterSettingsSection = "RegenMeterSettings";
	@ConfigSection(
			name = "Settings (Stat Bars)",
			description = "Configuration for the stat bars.",
			position = 4
	)
	String MainSettingsSection = "MainSettings";
	@ConfigSection(
			name = "Colors (Regen Meters)",
			description = "Setup the colors for the regen meters.",
			position = 2,
			closedByDefault = true
	)
	String RegenMeterColorSettingsSection = "RegenMeterColorSettings";
	@ConfigSection(
			name = "HP Colors (Stat Bars)",
			description = "Configuration for health color options.",
			position = 5,
			closedByDefault = true
	)
	String HPColorSettingsSection = "HPColorSettings";

	@ConfigSection(
			name = "Prayer Colors (Stat Bars)",
			description = "Configuration for prayer color options.",
			position = 6,
			closedByDefault = true
	)
	String PrayerColorSettingsSection = "PrayerColorSettings";

	@ConfigSection(
			name = "Energy Colors (Stat Bars)",
			description = "Configuration for prayer color options.",
			position = 7,
			closedByDefault = true
	)
	String EnergyColorSettingsSection = "EnergyColorSettings";

	@ConfigSection(
			name = "Special Colors (Stat Bars)",
			description = "Configuration for special color options.",
			position = 8,
			closedByDefault = true
	)
	String SpecialColorSettingsSection = "SpecialColorSettings";

	@ConfigSection(
			name = "Combat Skill Colors (Stat Bars)",
			description = "Configuration for combat skill color options.",
			position = 9,
			closedByDefault = true
	)
	String CombatColorSettingsSection = "CombatColorSettings";

	@ConfigSection(
			name = "Skill Colors (Stat Bars)",
			description = "Configuration for skill color options.",
			position = 10,
			closedByDefault = true
	)
	String SkillColorSettingsSection = "SkillColorSettings";

	String GROUP = "litestatbars";

	@ConfigItem(
			keyName = "showHitpoints",
			name = "HP Regen",
			description = "Enables a regeneration meter below the hitpoints orb to indicate hitpoint recovery.",
			position = 0,
			section = RegenMeterDisplaySettingsSection
	)
	default boolean showHitpoints()
	{
		return true;
	}
	@ConfigItem(
			keyName = "showWhenNoChange",
			name = "Always Show HP Regen",
			description = "Displays the hitpoints regeneration meter even when the hitpoints are full and there is no change.",
			position = 1,
			section = RegenMeterDisplaySettingsSection
	)
	default boolean showWhenNoChange()
	{
		return false;
	}
	@ConfigItem(
			keyName = "showPrayerDoseIndicator",
			name = "Prayer Dose Indicator",
			description = "Enables the prayer dose indicator.",
			position = 2,
			section = RegenMeterDisplaySettingsSection
	)
	default boolean showPrayerDoseIndicator()
	{
		return true;
	}
	@ConfigItem(
			keyName = "showSpecial",
			name = "Spec. Attack Regen",
			description = "Enables a regeneration meter below the Special Attack orb to indicate special attack recovery.",
			position = 3,
			section = RegenMeterDisplaySettingsSection
	)
	default boolean showSpecial()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "leftBarMode",
			name = "Top Left",
			description = "Configures the top left status bar.",
			section = MainBarSettingsSection
	)
	default LiteStatBarsMode leftBarMode()
	{
		return LiteStatBarsMode.HITPOINTS;
	}
	@ConfigItem(
			position = 2,
			keyName = "rightBarMode",
			name = "Top Right",
			description = "Configures the top right status bar.",
			section = MainBarSettingsSection
	)
	default LiteStatBarsMode rightBarMode()
	{
		return LiteStatBarsMode.SPECIAL_ATTACK;
	}
	@ConfigItem(
			position = 3,
			keyName = "LeftBarMode2",
			name = "Bottom Left",
			description = "Configures the bottom left status bar.",
			section = MainBarSettingsSection
	)
	default LiteStatBarsMode LeftBarMode2()
	{
		return LiteStatBarsMode.PRAYER;
	}
	@ConfigItem(
			position = 4,
			keyName = "RightBarMode2",
			name = "Bottom Right",
			description = "Configures the bottom right status bar.",
			section = MainBarSettingsSection
	)
	default LiteStatBarsMode RightBarMode2()
	{
		return LiteStatBarsMode.DISABLED;
	}

	@ConfigItem(
			keyName = "changeHealthIcon",
			name = "Dynamic HP Orb",
			description = "Configures the hp orb icon to change color for matching poisoned/diseased.",
			section = RegenMeterSettingsSection,
			position = 0
	)
	default boolean changeHealthIcon()
	{
		return true;
	}
	@ConfigItem(
			keyName = "notifyBeforeHpRegenDuration",
			name = "HP Regen Notification",
			description = "Sets a notification time (in seconds) before the next hitpoint regeneration occurs. A value of 0 disables notifications.",
			position = 5,
			section = RegenMeterSettingsSection
	)
	@Units(Units.SECONDS)
	default int getNotifyBeforeHpRegenSeconds()
	{
		return 0;
	}
	@ConfigItem(
			keyName = "showBackdrops",
			name = "Show Backdrops",
			description = "Enables or disables the display of the backdrop behind the regeneration meters.",
			position = 0,
			section = RegenMeterSettingsSection
	)
	default boolean showBackdrops()
	{
		return true; // Default value to show backdrops
	}
	@ConfigItem(
			keyName = "lineThickness",
			name = "Line Thickness",
			description = "Sets the thickness of the regeneration meters.",
			position = 1,
			section = RegenMeterSettingsSection
	)
	@Units(Units.PIXELS)
	default LineThickness getLineThickness() {
		return LineThickness.MEDIUM; // Default thickness
	}
	@ConfigItem(
			keyName = "barWidth",
			name = "Bar Width",
			description = "Choose the width of the regeneration meters.",
			position = 4,
			section = RegenMeterSettingsSection
	)
	default BarWidth getBarWidth() {
		return BarWidth.WIDER; // Default width
	}
	@ConfigItem(
			keyName = "barXPosition",
			name = "Meter Position",
			description = "Choose the position of the regeneration meters.",
			position = 3,
			section = RegenMeterSettingsSection
	)
	default BarXPosition barXPosition() {
		return BarXPosition.LEFT; // Default position
	}
	@ConfigItem(
			keyName = "barYPosition",
			name = "Meter Attach",
			description = "Choose to attach or detach the regeneration meters.",
			position = 2,
			section = RegenMeterSettingsSection
	)
	default BarYPosition barYPosition() {
		return BarYPosition.DETACHED; // Default position
	}

	@ConfigItem(
			position = 3,
			keyName = "enableRestorationBars",
			name = "Restoration Amount",
			description = "Visually shows how much will be restored to your status bar.",
			section = MainSettingsSection
	)
	default boolean enableRestorationBars()
	{
		return true;
	}
	@ConfigItem(
			position = 4,
			keyName = "enableCounter",
			name = "Numeric Counters",
			description = "Shows current value of the status on the bar.",
			section = MainSettingsSection
	)
	default boolean enableCounter()
	{
		return false;
	}
	@ConfigItem(
			position = 5,
			keyName = "enableSkillIcon",
			name = "Icons",
			description = "Adds skill icons at the top of the bars.",
			section = MainSettingsSection
	)
	default boolean enableSkillIcon()
	{
		return true;
	}
	@ConfigItem(
			position = 2,
			keyName = "hideAfterCombatDelay",
			name = "Hide After",
			description = "(0 = always show) Amount of ticks before hiding status bars after combat.",
			section = MainSettingsSection
	)
	@Units(Units.TICKS)
	default int hideAfterCombatDelay()
	{
		return 16;
	}
	@Range(
			min = LiteStatBarsRenderer.MIN_WIDTH,
			max = LiteStatBarsRenderer.MAX_WIDTH
	)
	@ConfigItem(
			position = 1,
			keyName = "statbarWidth",
			name = "Width (resize/modern)",
			description = "The width of the status bars in the modern resizeable layout.",
			section = MainSettingsSection
	)
	default int statbarWidth()
	{
		return LiteStatBarsRenderer.DEFAULT_WIDTH;
	}
	@Range(
			min = LiteStatBarsRenderer.MIN_OPACITY,
			max = LiteStatBarsRenderer.MAX_OPACITY
	)
	@ConfigItem(
			position = 0,
			keyName = "barTransparency",
			name = "Transparency (%)",
			description = "Set the transparency of the status bars (0 - opaque, 100 - transparent).",
			section = MainSettingsSection
	)
	default int barTransparency()
	{
		return LiteStatBarsRenderer.DEFAULT_OPACITY;

	}
	@ConfigItem(
			keyName = "showInfoboxes",
			name = "Show Poison Timers",
			description = "Configures the poison/disease/venom timer infoboxes and tooltips to show.",
			section = MainSettingsSection,
			position = 6
	)
	default boolean showInfoboxes()
	{
		return false;
	}


	@Alpha
	@ConfigItem(
			keyName = "hitpointsColor",
			name = "HP Meter",
			description = "Color of the hitpoints regeneration meters.",
			position = 0,
			section = RegenMeterColorSettingsSection
	)
	default Color getHitpointsColor()
	{
		return Color.RED; // Default color
	}
	@Alpha
	@ConfigItem(
			keyName = "prayerDoseOrbStartColor",
			name = "Prayer Dose Indicator",
			description = "Color of the flashing meter underneath the orb when a potion should be drank",
			position = 1,
			section = RegenMeterColorSettingsSection
	)
	default Color prayerDoseOrbStartColor()
	{

		return Color.CYAN;
	}
	@Alpha
	@ConfigItem(
			keyName = "specialColor",
			name = "Spec. Attack Meter",
			description = "Color of the special attack regeneration meters.",
			position = 2,
			section = RegenMeterColorSettingsSection
	)
	default Color getSpecialColor()
	{
		return Color.ORANGE; // Default color
	}
	@Alpha
	@ConfigItem(
			keyName = "backdropColor",
			name = "Fill Area",
			description = "Color of the backdrop that represents the area to fill the regeneration meters.",
			position = 3,
			section = RegenMeterColorSettingsSection
	)
	default Color getBackdropColor()
	{
		return new Color(0x505050); // Default backdrop color
	}

	@ConfigItem(
			position = 1,
			keyName = "prayerColor",
			name = "Prayer",
			description = "Color for the Prayer bar.",
			section = PrayerColorSettingsSection
	)
	default Color prayerColor() {
		return new Color(50, 200, 200);
	}

	@ConfigItem(
			position = 2,
			keyName = "activePrayerColor",
			name = "Prayer (active)",
			description = "Color for the Active Prayer bar.",
			section = PrayerColorSettingsSection
	)
	default Color activePrayerColor() {
		return new Color(57, 255, 186);
	}

	@ConfigItem(
			position = 3,
			keyName = "prayerHealColor",
			name = "Recharge Volume",
			description = "Color for the Prayer Recharge volume.",
			section = PrayerColorSettingsSection
	)
	default Color prayerHealColor() {
		return new Color(200, 200, 200);
	}

	@ConfigItem(
			position = 1,
			keyName = "healthColor",
			name = "Hitpoints",
			description = "Color for the Health bar.",
			section = HPColorSettingsSection
	)
	default Color healthColor() {
		return new Color(225, 35, 0);
	}

	@ConfigItem(
			position = 2,
			keyName = "healColor",
			name = "Recharge Volume",
			description = "Color for the HP Recharge volume.",
			section = HPColorSettingsSection
	)
	default Color healColor() {
		return new Color(255, 112, 6);
	}

	@ConfigItem(
			position = 3,
			keyName = "poisonedColor",
			name = "Poisoned",
			description = "Color for the Poisoned bar.",
			section = HPColorSettingsSection
	)
	default Color poisonedColor() {
		return new Color(0, 145, 0);
	}

	@ConfigItem(
			position = 4,
			keyName = "venomedColor",
			name = "Venomed",
			description = "Color for the Venomed bar.",
			section = HPColorSettingsSection
	)
	default Color venomedColor() {
		return new Color(0, 65, 0);
	}

	@ConfigItem(
			position = 1,
			keyName = "energyColor",
			name = "Run Energy",
			description = "Color for the Run Energy bar.",
			section = EnergyColorSettingsSection
	)
	default Color energyColor() {
		return new Color(199, 174, 0);
	}
	@ConfigItem(
			position = 2,
			keyName = "energyHealColor",
			name = "Recharge Volume",
			description = "Color for the Run Energy Recharge volume.",
			section = EnergyColorSettingsSection
	)
	default Color energyHealColor() {
		return new Color(199, 118, 0);
	}
	@ConfigItem(
			position = 3,
			keyName = "runStaminaColor",
			name = "Stamina Pot (active)",
			description = "Color for the Stamina potion bar.",
			section = EnergyColorSettingsSection
	)
	default Color runStaminaColor() {
		return new Color(160, 124, 72);
	}

	@ConfigItem(
			position = 1,
			keyName = "specialAttackColor",
			name = "Spec. Attack",
			description = "Color for the Special Attack bar.",
			section = SpecialColorSettingsSection
	)
	default Color specialAttackColor() {
		return new Color(3, 153, 0);
	}

	@ConfigItem(
			position = 2,
			keyName = "diseaseColor",
			name = "Disease",
			description = "Color for the Disease bar.",
			section = SpecialColorSettingsSection
	)
	default Color diseaseColor() {
		return new Color(255, 193, 75);
	}

	@ConfigItem(
			position = 3,
			keyName = "parasiteColor",
			name = "Parasite",
			description = "Color for the Parasite bar.",
			section = SpecialColorSettingsSection
	)
	default Color parasiteColor() {
		return new Color(196, 62, 109);
	}

	@ConfigItem(
			position = 23,
			keyName = "attackColor",
			name = "Attack",
			description = "Color for the Attack bar.",
			section = CombatColorSettingsSection
	)
	default Color attackColor() {
		return new Color(255, 0, 0);
	}

	@ConfigItem(
			position = 24,
			keyName = "defenceColor",
			name = "Defence",
			description = "Color for the Defence bar.",
			section = CombatColorSettingsSection
	)
	default Color defenceColor() {
		return new Color(0, 0, 255);
	}

	@ConfigItem(
			position = 25,
			keyName = "strengthColor",
			name = "Strength",
			description = "Color for the Strength bar.",
			section = CombatColorSettingsSection
	)
	default Color strengthColor() {
		return new Color(0, 255, 0);
	}

	@ConfigItem(
			position = 26,
			keyName = "rangedColor",
			name = "Ranged",
			description = "Color for the Ranged bar.",
			section = CombatColorSettingsSection
	)
	default Color rangedColor() {
		return new Color(255, 165, 0);
	}

	@ConfigItem(
			position = 27,
			keyName = "magicColor",
			name = "Magic",
			description = "Color for the Magic bar.",
			section = CombatColorSettingsSection
	)
	default Color magicColor() {
		return new Color(128, 0, 128);
	}

	@ConfigItem(
			position = 28,
			keyName = "cookingColor",
			name = "Cooking",
			description = "Color for the Cooking bar.",
			section = SkillColorSettingsSection
	)
	default Color cookingColor() {
		return new Color(255, 228, 196);
	}

	@ConfigItem(
			position = 29,
			keyName = "woodcuttingColor",
			name = "Woodcutting",
			description = "Color for the Woodcutting bar.",
			section = SkillColorSettingsSection
	)
	default Color woodcuttingColor() {
		return new Color(34, 139, 34);
	}

	@ConfigItem(
			position = 30,
			keyName = "fletchingColor",
			name = "Fletching",
			description = "Color for the Fletching bar.",
			section = SkillColorSettingsSection
	)
	default Color fletchingColor() {
		return new Color(210, 105, 30);
	}

	@ConfigItem(
			position = 31,
			keyName = "fishingColor",
			name = "Fishing",
			description = "Color for the Fishing bar.",
			section = SkillColorSettingsSection
	)
	default Color fishingColor() {
		return new Color(135, 206, 235);
	}

	@ConfigItem(
			position = 32,
			keyName = "firemakingColor",
			name = "Firemaking",
			description = "Color for the Firemaking bar.",
			section = SkillColorSettingsSection
	)
	default Color firemakingColor() {
		return new Color(255, 99, 71);
	}

	@ConfigItem(
			position = 33,
			keyName = "craftingColor",
			name = "Crafting",
			description = "Color for the Crafting bar.",
			section = SkillColorSettingsSection
	)
	default Color craftingColor() {
		return new Color(173, 216, 230);
	}

	@ConfigItem(
			position = 34,
			keyName = "smithingColor",
			name = "Smithing",
			description = "Color for the Smithing bar.",
			section = SkillColorSettingsSection
	)
	default Color smithingColor() {
		return new Color(128, 128, 128);
	}

	@ConfigItem(
			position = 35,
			keyName = "miningColor",
			name = "Mining",
			description = "Color for the Mining bar.",
			section = SkillColorSettingsSection
	)
	default Color miningColor() {
		return new Color(0, 255, 255);
	}

	@ConfigItem(
			position = 36,
			keyName = "herbloreColor",
			name = "Herblore",
			description = "Color for the Herblore bar.",
			section = SkillColorSettingsSection
	)
	default Color herbloreColor() {
		return new Color(255, 20, 147);
	}

	@ConfigItem(
			position = 37,
			keyName = "agilityColor",
			name = "Agility",
			description = "Color for the Agility bar.",
			section = SkillColorSettingsSection
	)
	default Color agilityColor() {
		return new Color(255, 140, 0);
	}

	@ConfigItem(
			position = 38,
			keyName = "thievingColor",
			name = "Thieving",
			description = "Color for the Thieving bar.",
			section = SkillColorSettingsSection
	)
	default Color thievingColor() {
		return new Color(153, 50, 204);
	}

	@ConfigItem(
			position = 39,
			keyName = "slayerColor",
			name = "Slayer",
			description = "Color for the Slayer bar.",
			section = SkillColorSettingsSection
	)
	default Color slayerColor() {
		return new Color(0, 191, 255);
	}

	@ConfigItem(
			position = 40,
			keyName = "farmingColor",
			name = "Farming",
			description = "Color for the Farming bar.",
			section = SkillColorSettingsSection
	)
	default Color farmingColor() {
		return new Color(154, 205, 50);
	}

	@ConfigItem(
			position = 41,
			keyName = "runecraftColor",
			name = "Runecrafting",
			description = "Color for the Runecrafting bar.",
			section = SkillColorSettingsSection
	)
	default Color runecraftColor() {
		return new Color(186, 85, 211);
	}

	@ConfigItem(
			position = 42,
			keyName = "hunterColor",
			name = "Hunter",
			description = "Color for the Hunter bar.",
			section = SkillColorSettingsSection
	)
	default Color hunterColor() {
		return new Color(255, 228, 181);
	}

	@ConfigItem(
			position = 43,
			keyName = "constructionColor",
			name = "Construction",
			description = "Color for the Construction bar.",
			section = SkillColorSettingsSection
	)
	default Color constructionColor() {
		return new Color(139, 69, 19);
	}

	public enum BarXPosition {
		LEFT,
		MIDDLE,
		RIGHT
	}

	public enum BarYPosition {
		ATTACHED,
		DETACHED
	}

	public enum BarWidth {
		NORMAL,
		WIDER;
	}

}