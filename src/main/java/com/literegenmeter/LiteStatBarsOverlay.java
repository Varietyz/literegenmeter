/*
 * Copyright (c) 2024, Smoke (Smoked today) <https://github.com/Varietyz>
 * Copyright (c) 2019, Jos <Malevolentdev@gmail.com>
 * Copyright (c) 2019, Rheon <https://github.com/Rheon-D>
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import com.literegenmeter.config.LiteStatBarsMode;
import com.literegenmeter.statbars.Viewport;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

class LiteStatBarsOverlay extends Overlay
{
	private static final int HEIGHT = 132;
	private static final int RESIZED_BOTTOM_HEIGHT = 136;
	private static final int IMAGE_SIZE = 16;
	private static final Dimension ICON_DIMENSIONS = new Dimension(26, 25);
	private static final int RESIZED_BOTTOM_OFFSET_Y = 12;
	private static final int RESIZED_BOTTOM_OFFSET_X = 10;
	private static final int MAX_SPECIAL_ATTACK_VALUE = 100;
	private static final int MAX_RUN_ENERGY_VALUE = 100;

	private final Client client;
	private final LiteRegenMeterPlugin plugin;
	private final LiteRegenMeterConfig config;
	private final ItemStatChangesService itemStatService;
	private final SpriteManager spriteManager;

	private final Image prayerIcon;
	private final Image heartDisease;
	private final Image heartPoison;
	private final Image heartVenom;
	private Image heartIcon;
	private Image specialIcon;
	private Image energyIcon;
	private final Image attackIcon;
	private final Image defenceIcon;
	private final Image strengthIcon;
	private final Image rangedIcon;
	private final Image magicIcon;
	private final Image cookingIcon;
	private final Image woodcuttingIcon;
	private final Image fletchingIcon;
	private final Image fishingIcon;
	private final Image firemakingIcon;
	private final Image craftingIcon;
	private final Image smithingIcon;
	private final Image miningIcon;
	private final Image herbloreIcon;
	private final Image agilityIcon;
	private final Image thievingIcon;
	private final Image slayerIcon;
	private final Image farmingIcon;
	private final Image runecraftingIcon;
	private final Image hunterIcon;
	private final Image constructionIcon;
	private final Map<LiteStatBarsMode, LiteStatBarsRenderer> barRenderers = new EnumMap<>(LiteStatBarsMode.class);

	@Inject
	private LiteStatBarsOverlay(Client client, LiteRegenMeterPlugin plugin, LiteRegenMeterConfig config, SkillIconManager skillIconManager, ItemStatChangesService itemstatservice, SpriteManager spriteManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.itemStatService = itemstatservice;
		this.spriteManager = spriteManager;

		attackIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.ATTACK, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		defenceIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.DEFENCE, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		strengthIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.STRENGTH, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		rangedIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.RANGED, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		magicIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.MAGIC, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		cookingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.COOKING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		woodcuttingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.WOODCUTTING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		fletchingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.FLETCHING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		fishingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.FISHING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		firemakingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.FIREMAKING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		craftingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.CRAFTING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		smithingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.SMITHING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		miningIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.MINING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		herbloreIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.HERBLORE, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		agilityIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.AGILITY, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		thievingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.THIEVING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		slayerIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.SLAYER, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		farmingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.FARMING, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		runecraftingIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.RUNECRAFT, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		hunterIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.HUNTER, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		constructionIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.CONSTRUCTION, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		prayerIcon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(skillIconManager.getSkillImage(Skill.PRAYER, true), IMAGE_SIZE, IMAGE_SIZE), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);

		heartDisease = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		heartPoison = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
		heartVenom = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART), ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);

		initRenderers();
	}

	private Color brightenColor(Color color, float percentage) {
		int r = (int) Math.min(255, color.getRed() * (1 + percentage));
		int g = (int) Math.min(255, color.getGreen() * (1 + percentage));
		int b = (int) Math.min(255, color.getBlue() * (1 + percentage));
		return new Color(r, g, b);
	}

	private void initRenderers() {
		barRenderers.put(LiteStatBarsMode.DISABLED, null);

		barRenderers.put(LiteStatBarsMode.HITPOINTS, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HITPOINTS),
				() -> client.getBoostedSkillLevel(Skill.HITPOINTS),
				() -> getRestoreValue(Skill.HITPOINTS.getName()),
				() -> {
					final int poisonState = client.getVarpValue(VarPlayer.POISON);

					if (poisonState >= 1000000) {
						return config.venomedColor();
					}

					if (poisonState > 0) {
						return config.poisonedColor();
					}

					if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
						return config.diseaseColor();
					}

					if (client.getVarbitValue(Varbits.PARASITE) >= 1) {
						return config.parasiteColor();
					}

					return config.healthColor();
				},
				config::healColor,
				() -> {
					final int poisonState = client.getVarpValue(VarPlayer.POISON);

					if (poisonState > 0 && poisonState < 50) {
						return heartPoison;
					}

					if (poisonState >= 1000000) {
						return heartVenom;
					}

					if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0) {
						return heartDisease;
					}

					return heartIcon;
				}
		));

		barRenderers.put(LiteStatBarsMode.PRAYER, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.PRAYER),
				() -> client.getBoostedSkillLevel(Skill.PRAYER),
				() -> getRestoreValue(Skill.PRAYER.getName()),
				() -> {
					Color prayerColor = config.prayerColor();

					for (Prayer pray : Prayer.values()) {
						if (client.isPrayerActive(pray)) {
							prayerColor = config.activePrayerColor();
							break;
						}
					}

					return prayerColor;
				},
				config::prayerHealColor,
				() -> prayerIcon
		));

		barRenderers.put(LiteStatBarsMode.RUN_ENERGY, new LiteStatBarsRenderer(
				() -> MAX_RUN_ENERGY_VALUE,
				() -> client.getEnergy() / 100,
				() -> getRestoreValue("Run Energy"),
				() -> {
					if (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) != 0) {
						return config.runStaminaColor();
					} else {
						return config.energyColor();
					}
				},
				config::energyHealColor,
				() -> energyIcon
		));

		barRenderers.put(LiteStatBarsMode.SPECIAL_ATTACK, new LiteStatBarsRenderer(
				() -> MAX_SPECIAL_ATTACK_VALUE,
				() -> client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10,
				() -> 0,
				config::specialAttackColor,
				() -> brightenColor(config.specialAttackColor(), 0.5f),
				() -> specialIcon
		));
		barRenderers.put(LiteStatBarsMode.ATTACK, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.ATTACK),
				() -> client.getBoostedSkillLevel(Skill.ATTACK),
				() -> getRestoreValue(Skill.ATTACK.getName()),
				config::attackColor,
				() -> brightenColor(config.attackColor(), 0.5f),
				() -> attackIcon
		));

		barRenderers.put(LiteStatBarsMode.DEFENCE, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.DEFENCE),
				() -> client.getBoostedSkillLevel(Skill.DEFENCE),
				() -> getRestoreValue(Skill.DEFENCE.getName()),
				config::defenceColor,
				() -> brightenColor(config.defenceColor(), 0.5f),
				() -> defenceIcon
		));

		barRenderers.put(LiteStatBarsMode.STRENGTH, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.STRENGTH),
				() -> client.getBoostedSkillLevel(Skill.STRENGTH),
				() -> getRestoreValue(Skill.STRENGTH.getName()),
				config::strengthColor,
				() -> brightenColor(config.strengthColor(), 0.5f),
				() -> strengthIcon
		));

		barRenderers.put(LiteStatBarsMode.RANGED, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.RANGED),
				() -> client.getBoostedSkillLevel(Skill.RANGED),
				() -> getRestoreValue(Skill.RANGED.getName()),
				config::rangedColor,
				() -> brightenColor(config.rangedColor(), 0.5f),
				() -> rangedIcon
		));

		barRenderers.put(LiteStatBarsMode.MAGIC, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.MAGIC),
				() -> client.getBoostedSkillLevel(Skill.MAGIC),
				() -> getRestoreValue(Skill.MAGIC.getName()),
				config::magicColor,
				() -> brightenColor(config.magicColor(), 0.5f),
				() -> magicIcon
		));

		barRenderers.put(LiteStatBarsMode.COOKING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.COOKING),
				() -> client.getBoostedSkillLevel(Skill.COOKING),
				() -> getRestoreValue(Skill.COOKING.getName()),
				config::cookingColor,
				() -> brightenColor(config.cookingColor(), 0.5f),
				() -> cookingIcon
		));

		barRenderers.put(LiteStatBarsMode.WOODCUTTING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.WOODCUTTING),
				() -> client.getBoostedSkillLevel(Skill.WOODCUTTING),
				() -> getRestoreValue(Skill.WOODCUTTING.getName()),
				config::woodcuttingColor,
				() -> brightenColor(config.woodcuttingColor(), 0.5f),
				() -> woodcuttingIcon
		));

		barRenderers.put(LiteStatBarsMode.FLETCHING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.FLETCHING),
				() -> client.getBoostedSkillLevel(Skill.FLETCHING),
				() -> getRestoreValue(Skill.FLETCHING.getName()),
				config::fletchingColor,
				() -> brightenColor(config.fletchingColor(), 0.5f),
				() -> fletchingIcon
		));

		barRenderers.put(LiteStatBarsMode.FISHING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.FISHING),
				() -> client.getBoostedSkillLevel(Skill.FISHING),
				() -> getRestoreValue(Skill.FISHING.getName()),
				config::fishingColor,
				() -> brightenColor(config.fishingColor(), 0.5f),
				() -> fishingIcon
		));

		barRenderers.put(LiteStatBarsMode.FIREMAKING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.FIREMAKING),
				() -> client.getBoostedSkillLevel(Skill.FIREMAKING),
				() -> getRestoreValue(Skill.FIREMAKING.getName()),
				config::firemakingColor,
				() -> brightenColor(config.firemakingColor(), 0.5f),
				() -> firemakingIcon
		));

		barRenderers.put(LiteStatBarsMode.CRAFTING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.CRAFTING),
				() -> client.getBoostedSkillLevel(Skill.CRAFTING),
				() -> getRestoreValue(Skill.CRAFTING.getName()),
				config::craftingColor,
				() -> brightenColor(config.craftingColor(), 0.5f),
				() -> craftingIcon
		));

		barRenderers.put(LiteStatBarsMode.SMITHING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.SMITHING),
				() -> client.getBoostedSkillLevel(Skill.SMITHING),
				() -> getRestoreValue(Skill.SMITHING.getName()),
				config::smithingColor,
				() -> brightenColor(config.smithingColor(), 0.5f),
				() -> smithingIcon
		));

		barRenderers.put(LiteStatBarsMode.MINING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.MINING),
				() -> client.getBoostedSkillLevel(Skill.MINING),
				() -> getRestoreValue(Skill.MINING.getName()),
				config::miningColor,
				() -> brightenColor(config.miningColor(), 0.5f),
				() -> miningIcon
		));

		barRenderers.put(LiteStatBarsMode.HERBLORE, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HERBLORE),
				() -> client.getBoostedSkillLevel(Skill.HERBLORE),
				() -> getRestoreValue(Skill.HERBLORE.getName()),
				config::herbloreColor,
				() -> brightenColor(config.herbloreColor(), 0.5f),
				() -> herbloreIcon
		));

		barRenderers.put(LiteStatBarsMode.AGILITY, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.AGILITY),
				() -> client.getBoostedSkillLevel(Skill.AGILITY),
				() -> getRestoreValue(Skill.AGILITY.getName()),
				config::agilityColor,
				() -> brightenColor(config.agilityColor(), 0.5f),
				() -> agilityIcon
		));

		barRenderers.put(LiteStatBarsMode.THIEVING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.THIEVING),
				() -> client.getBoostedSkillLevel(Skill.THIEVING),
				() -> getRestoreValue(Skill.THIEVING.getName()),
				config::thievingColor,
				() -> brightenColor(config.thievingColor(), 0.5f),
				() -> thievingIcon
		));

		barRenderers.put(LiteStatBarsMode.SLAYER, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.SLAYER),
				() -> client.getBoostedSkillLevel(Skill.SLAYER),
				() -> getRestoreValue(Skill.SLAYER.getName()),
				config::slayerColor,
				() -> brightenColor(config.slayerColor(), 0.5f),
				() -> slayerIcon
		));

		barRenderers.put(LiteStatBarsMode.FARMING, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.FARMING),
				() -> client.getBoostedSkillLevel(Skill.FARMING),
				() -> getRestoreValue(Skill.FARMING.getName()),
				config::farmingColor,
				() -> brightenColor(config.farmingColor(), 0.5f),
				() -> farmingIcon
		));

		barRenderers.put(LiteStatBarsMode.RUNECRAFT, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.RUNECRAFT),
				() -> client.getBoostedSkillLevel(Skill.RUNECRAFT),
				() -> getRestoreValue(Skill.RUNECRAFT.getName()),
				config::runecraftColor,
				() -> brightenColor(config.runecraftColor(), 0.5f),
				() -> runecraftingIcon
		));

		barRenderers.put(LiteStatBarsMode.HUNTER, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.HUNTER),
				() -> client.getBoostedSkillLevel(Skill.HUNTER),
				() -> getRestoreValue(Skill.HUNTER.getName()),
				config::hunterColor,
				() -> brightenColor(config.hunterColor(), 0.5f),
				() -> hunterIcon
		));

		barRenderers.put(LiteStatBarsMode.CONSTRUCTION, new LiteStatBarsRenderer(
				() -> inLms() ? Experience.MAX_REAL_LEVEL : client.getRealSkillLevel(Skill.CONSTRUCTION),
				() -> client.getBoostedSkillLevel(Skill.CONSTRUCTION),
				() -> getRestoreValue(Skill.CONSTRUCTION.getName()),
				config::constructionColor,
				() -> brightenColor(config.constructionColor(), 0.5f),
				() -> constructionIcon
		));
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!plugin.isBarsDisplayed())
		{
			return null;
		}

		Viewport curViewport = null;
		Widget curWidget = null;

		for (Viewport viewport : Viewport.values())
		{
			final Widget viewportWidget = client.getWidget(viewport.getViewport());
			if (viewportWidget != null && !viewportWidget.isHidden())
			{
				curViewport = viewport;
				curWidget = viewportWidget;
				break;
			}
		}

		if (curViewport == null)
		{
			return null;
		}

		final Point offsetLeft = curViewport.getOffsetLeft();
		final Point offsetRight = curViewport.getOffsetRight();
		final Point location = curWidget.getCanvasLocation();

		LiteRegenMeterConfig.PackMode mode = config.packMode();

		int width = LiteStatBarsRenderer.DEFAULT_WIDTH;
		int height = HEIGHT;
		int offsetLeftBarX, offsetLeftBarY, offsetRightBarX, offsetRightBarY;

		if (curViewport == Viewport.RESIZED_BOTTOM)
		{
			width = config.statbarWidth();
			height = RESIZED_BOTTOM_HEIGHT;
			final int barWidthOffset = width - LiteStatBarsRenderer.DEFAULT_WIDTH;

			offsetLeftBarX = (location.getX() + RESIZED_BOTTOM_OFFSET_X - offsetLeft.getX() - 2 * barWidthOffset) + 19;
			offsetLeftBarY = (location.getY() - RESIZED_BOTTOM_OFFSET_Y - offsetLeft.getY());

			offsetRightBarX = (location.getX() + RESIZED_BOTTOM_OFFSET_X - offsetRight.getX() - barWidthOffset) + 9;
			offsetRightBarY = (location.getY() - RESIZED_BOTTOM_OFFSET_Y - offsetRight.getY());
			if (mode == LiteRegenMeterConfig.PackMode.VANILLA)
			{
				offsetLeftBarX -= 2;
				offsetRightBarX -= 2;
			}
		}
		else
		{
			offsetLeftBarX = (location.getX() - offsetLeft.getX());
			offsetRightBarX = (location.getX() - offsetRight.getX() + 4) + curWidget.getWidth();

			offsetLeftBarY = (location.getY() - offsetLeft.getY() - 5);
			offsetRightBarY = (location.getY() - offsetRight.getY() - 5);

			if (mode == LiteRegenMeterConfig.PackMode.VANILLA)
			{
				height -= 6;

				offsetLeftBarY = (location.getY() - offsetLeft.getY());
				offsetRightBarY = (location.getY() - offsetRight.getY());
				offsetRightBarX -= 4;

				width += 3;
			}
		}

		buildIcons();

		LiteStatBarsRenderer left = barRenderers.get(config.leftBarMode());
		LiteStatBarsRenderer right = barRenderers.get(config.rightBarMode());

		int leftBar1Length = height;
		int leftBar2Length = height;
		int rightBar1Length = height;
		int rightBar2Length = height;

		boolean isLeftBar1Off = left == null || config.leftBarMode() == LiteStatBarsMode.DISABLED;
		boolean isLeftBar2Off = barRenderers.get(config.LeftBarMode2()) == null || config.LeftBarMode2() == LiteStatBarsMode.DISABLED;

		boolean isRightBar1Off = right == null || config.rightBarMode() == LiteStatBarsMode.DISABLED;
		boolean isRightBar2Off = barRenderers.get(config.RightBarMode2()) == null || config.RightBarMode2() == LiteStatBarsMode.DISABLED;

		if (isLeftBar1Off && !isLeftBar2Off)
		{
			offsetLeftBarY -= height;
			leftBar2Length = height * 2;
		}
		else if (isLeftBar2Off && !isLeftBar1Off)
		{
			leftBar1Length = height * 2;
		}

		if (isRightBar1Off && !isRightBar2Off)
		{
			offsetRightBarY -= height;
			rightBar2Length = height * 2;
		}
		else if (isRightBar2Off && !isRightBar1Off)
		{
			rightBar1Length = height * 2;
		}

		if (left != null)
		{
			left.renderBar(config, g, offsetLeftBarX, offsetLeftBarY, width, leftBar1Length);
		}

		if (right != null)
		{
			right.renderBar(config, g, offsetRightBarX, offsetRightBarY, width, rightBar1Length);
		}

		int secondBarYOffset = height;

		LiteStatBarsRenderer secondLeft = barRenderers.get(config.LeftBarMode2());
		LiteStatBarsRenderer secondRight = barRenderers.get(config.RightBarMode2());

		if (secondLeft != null)
		{
			secondLeft.renderBar(config, g, offsetLeftBarX, offsetLeftBarY + secondBarYOffset, width, leftBar2Length);
		}

		if (secondRight != null)
		{
			secondRight.renderBar(config, g, offsetRightBarX, offsetRightBarY + secondBarYOffset, width, rightBar2Length);
		}

		return null;
	}

	private int getRestoreValue(String skill)
	{
		final MenuEntry[] menu = client.getMenuEntries();
		final int menuSize = menu.length;
		if (menuSize == 0)
		{
			return 0;
		}

		final MenuEntry entry = menu[menuSize - 1];
		final Widget widget = entry.getWidget();
		int restoreValue = 0;

		if (widget != null && widget.getId() == ComponentID.INVENTORY_CONTAINER)
		{
			final Effect change = itemStatService.getItemStatChanges(widget.getItemId());

			if (change != null)
			{
				for (final StatChange c : change.calculate(client).getStatChanges())
				{
					final int value = c.getTheoretical();

					if (value != 0 && c.getStat().getName().equals(skill))
					{
						restoreValue = value;
					}
				}
			}
		}

		return restoreValue;
	}

	private void buildIcons()
	{
		if (heartIcon == null)
		{
			heartIcon = loadAndResize(SpriteID.MINIMAP_ORB_HITPOINTS_ICON);
		}
		if (energyIcon == null)
		{
			energyIcon = loadAndResize(SpriteID.MINIMAP_ORB_WALK_ICON);
		}
		if (specialIcon == null)
		{
			specialIcon = loadAndResize(SpriteID.MINIMAP_ORB_SPECIAL_ICON);
		}
	}

	private BufferedImage loadAndResize(int spriteId)
	{
		BufferedImage image = spriteManager.getSprite(spriteId, 0);
		if (image == null)
		{
			return null;
		}

		return ImageUtil.resizeCanvas(image, ICON_DIMENSIONS.width, ICON_DIMENSIONS.height);
	}

	private boolean inLms()
	{
		return client.getWidget(ComponentID.LMS_INGAME_INFO) != null;
	}
}
