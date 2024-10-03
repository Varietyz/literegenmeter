/*
 * Copyright (c) 2019, Jos <Malevolentdev@gmail.com>
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
package com.litestatbars;

import net.runelite.client.config.*;
import com.litestatbars.config.LiteStatBarsMode;

import java.awt.*;

@ConfigGroup(LiteStatBarsConfig.GROUP)
public interface LiteStatBarsConfig extends Config

{
	@ConfigSection(
			name = "Stat Bars",
			description = "Setup the stat bars.",
			position = 0
	)
	String MainBarSettingsSection = "MainBarSettings";

	@ConfigSection(
			name = "Settings",
			description = "Configuration for the stat bars.",
			position = 1
	)
	String MainSettingsSection = "MainSettings";

	@ConfigSection(
			name = "HP Colors",
			description = "Configuration for health color options.",
			position = 2
	)
	String HPColorSettingsSection = "HPColorSettings";

	@ConfigSection(
			name = "Prayer Colors",
			description = "Configuration for prayer color options.",
			position = 3
	)
	String PrayerColorSettingsSection = "PrayerColorSettings";

	@ConfigSection(
			name = "Energy Colors",
			description = "Configuration for prayer color options.",
			position = 4
	)
	String EnergyColorSettingsSection = "EnergyColorSettings";

	@ConfigSection(
			name = "Special Colors",
			description = "Configuration for special color options.",
			position = 5
	)
	String SpecialColorSettingsSection = "SpecialColorSettings";

	@ConfigSection(
			name = "Combat Skill Colors",
			description = "Configuration for combat skill color options.",
			position = 6
	)
	String CombatColorSettingsSection = "CombatColorSettings";

	@ConfigSection(
			name = "Skill Colors",
			description = "Configuration for skill color options.",
			position = 7
	)
	String SkillColorSettingsSection = "SkillColorSettings";

	String GROUP = "litestatbars";

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
			position = 1,
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
			position = 2,
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
			position = 3,
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
			position = 4,
			keyName = "hideAfterCombatDelay",
			name = "Hide After",
			description = "Amount of ticks before hiding status bars after combat. 0 = always show status bars.",
			section = MainSettingsSection
	)
	@Units(Units.TICKS)
	default int hideAfterCombatDelay()
	{
		return 0;
	}

	@Range(
			min = LiteStatBarsRenderer.MIN_WIDTH,
			max = LiteStatBarsRenderer.MAX_WIDTH
	)
	@ConfigItem(
			position = 5,
			keyName = "barWidth",
			name = "Width (resize/modern)",
			description = "The width of the status bars in the modern resizeable layout.",
			section = MainSettingsSection
	)
	default int barWidth()
	{
		return LiteStatBarsRenderer.DEFAULT_WIDTH;
	}

	@Range(
			min = LiteStatBarsRenderer.MIN_OPACITY,
			max = LiteStatBarsRenderer.MAX_OPACITY
	)
	@ConfigItem(
			position = 6,
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
}
