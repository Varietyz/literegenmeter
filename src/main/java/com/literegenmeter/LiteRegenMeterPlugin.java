/*
 * Copyright (c) 2024, Smoke (Smoked today) <https://github.com/Varietyz>
 * Copyright (c) 2019, Sean Dewar <https://github.com/seandewar>
 * Copyright (c) 2018, Hydrox6 <ikada@protonmail.ch>
 * Copyright (c) 2018, Abex
 * Copyright (c) 2018, Zimaya <https://github.com/Zimaya>
 * Copyright (c) 2018, Jos <Malevolentdev@gmail.com>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import com.literegenmeter.orbmeters.*;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemStats;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@PluginDescriptor(
	name = "LITE Regen Meter",
	description = "Track and show the hitpoints and special attack regeneration timers + more, adjusted to work with the RuneLITE theme by Smoke (Smoked today).",
	tags = {"combat", "health", "hitpoints", "special", "attack", "overlay", "notifications", "runelite", "theme", "smoke", "varietyz", "orb", "bar", "poison", "venom", "disease", "heart"}
)
@PluginDependency(ItemStatPlugin.class)
public class LiteRegenMeterPlugin extends Plugin
{
	static final int POISON_TICK_MILLIS = 18200;
	static final int VENOM_THRESHOLD = 1000000;
	static final int VENOM_MAXIUMUM_DAMAGE = 20;

	static final BufferedImage HEART_DISEASE;
	static final BufferedImage HEART_POISON;
	static final BufferedImage HEART_VENOM;

	static
	{
		HEART_DISEASE = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.DISEASE_HEART), 26, 26);
		HEART_POISON = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.POISON_HEART), 26, 26);
		HEART_VENOM = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, AlternateSprites.VENOM_HEART), 26, 26);
	}

	private Instant startOfLastTick = Instant.now();
	private static final int SPEC_REGEN_TICKS = 50;
	private static final int NORMAL_HP_REGEN_TICKS = 100;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Notifier notifier;

	@Inject
	private LitePrayerDoseOverlay doseOverlay;

	@Inject
	private LiteRegenMeterOverlay overlay;

	@Inject
	private LiteStatBarsOverlay statBarOverlay;

	@Inject
	private PoisonOverlay poisonOverlay;

	@Inject
	private LiteRegenMeterConfig config;

	@Inject
	private ClientThread clientThread;

	@Getter(AccessLevel.PACKAGE)
	private boolean prayersActive = false;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private int prayerBonus;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ItemManager itemManager;

	@Getter
	private double hitpointsPercentage;

	@Getter
	private double specialPercentage;

	@Getter(AccessLevel.PACKAGE)
	private boolean barsDisplayed;

	@Getter
	private int lastDamage;
	private boolean envenomed;
	private PoisonInfobox infobox;
	private Instant poisonNaturalCure;
	private Instant nextPoisonTick;
	private BufferedImage heart;

	private int ticksSinceSpecRegen;
	private int ticksSinceHPRegen;
	private int lastCombatActionTickCount;

	private boolean wearingLightbearer;

	@Provides
	LiteRegenMeterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LiteRegenMeterConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(this::checkStatusBars);

		overlayManager.add(overlay);
		overlayManager.add(statBarOverlay);
		overlayManager.add(doseOverlay);
		overlayManager.add(poisonOverlay);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::checkHealthIcon);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(doseOverlay);
		overlayManager.remove(statBarOverlay);
		overlayManager.remove(poisonOverlay);

		barsDisplayed = false;

		if (infobox != null)
		{
			infoBoxManager.removeInfoBox(infobox);
			infobox = null;
		}

		envenomed = false;
		lastDamage = 0;
		poisonNaturalCure = null;
		nextPoisonTick = null;

		clientThread.invoke(this::resetHealthIcon);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged ev)
	{
		if (ev.getGameState() == GameState.HOPPING || ev.getGameState() == GameState.LOGIN_SCREEN)
		{
			ticksSinceHPRegen = -2; // For some reason this makes this accurate
			ticksSinceSpecRegen = 0;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final int id = event.getContainerId();
		if (id == InventoryID.INVENTORY.getId())
		{
			updatePotionBonus(event.getItemContainer(),
					client.getItemContainer(InventoryID.EQUIPMENT));
		}
		else if (id == InventoryID.EQUIPMENT.getId())
		{
			prayerBonus = totalPrayerBonus(event.getItemContainer().getItems());
		}

		if (event.getContainerId() != InventoryID.EQUIPMENT.getId())
		{
			return;
		}

		ItemContainer equipment = event.getItemContainer();
		final boolean hasLightbearer = equipment.contains(ItemID.LIGHTBEARER);
		if (hasLightbearer == wearingLightbearer)
		{
			return;
		}

		// Lightbearer switch preserves time until next spec regen if <25 ticks remain
		// If unequipping Lightbearer, this will always evaluate to 0
		ticksSinceSpecRegen = Math.max(0, ticksSinceSpecRegen - 25);
		wearingLightbearer = hasLightbearer;
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged ev)
	{
		if (ev.getVarbitId() == Varbits.PRAYER_RAPID_HEAL)
		{
			ticksSinceHPRegen = 0;
		}
		if (ev.getVarpId() == VarPlayer.POISON)
		{
			final int poisonValue = ev.getValue();
			nextPoisonTick = Instant.now().plus(Duration.of(POISON_TICK_MILLIS, ChronoUnit.MILLIS));

			final int damage = nextDamage(poisonValue);
			this.lastDamage = damage;

			envenomed = poisonValue >= VENOM_THRESHOLD;

			if (poisonValue < VENOM_THRESHOLD)
			{
				poisonNaturalCure = Instant.now().plus(Duration.of(POISON_TICK_MILLIS * poisonValue, ChronoUnit.MILLIS));
			}
			else
			{
				poisonNaturalCure = null;
			}

			if (config.showInfoboxes())
			{
				if (infobox != null)
				{
					infoBoxManager.removeInfoBox(infobox);
					infobox = null;
				}

				if (damage > 0)
				{
					final BufferedImage image = getSplat(envenomed ? SpriteID.HITSPLAT_DARK_GREEN_VENOM : SpriteID.HITSPLAT_GREEN_POISON, damage);

					if (image != null)
					{
						infobox = new PoisonInfobox(image, this);
						infoBoxManager.addInfoBox(infobox);
					}
				}
			}

			checkHealthIcon();
		}
		else if (ev.getVarpId() == VarPlayer.DISEASE_VALUE)
		{
			checkHealthIcon();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		final int ticksPerSpecRegen = wearingLightbearer ? SPEC_REGEN_TICKS / 2 : SPEC_REGEN_TICKS;
		checkStatusBars();

		if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000)
		{
			// The recharge doesn't tick when at 100%
			ticksSinceSpecRegen = 0;
		}
		else
		{
			ticksSinceSpecRegen = (ticksSinceSpecRegen + 1) % ticksPerSpecRegen;
		}
		specialPercentage = ticksSinceSpecRegen / (double) ticksPerSpecRegen;


		int ticksPerHPRegen = NORMAL_HP_REGEN_TICKS;
		if (client.isPrayerActive(Prayer.RAPID_HEAL))
		{
			ticksPerHPRegen /= 2;
		}

		ticksSinceHPRegen = (ticksSinceHPRegen + 1) % ticksPerHPRegen;
		hitpointsPercentage = ticksSinceHPRegen / (double) ticksPerHPRegen;

		int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int maxHP = client.getRealSkillLevel(Skill.HITPOINTS);
		if (currentHP == maxHP && !config.showWhenNoChange())
		{
			hitpointsPercentage = 0;
		}
		else if (currentHP > maxHP)
		{
			// Show it going down
			hitpointsPercentage = 1 - hitpointsPercentage;
		}

		if (config.getNotifyBeforeHpRegenSeconds() > 0 && currentHP < maxHP && shouldNotifyHpRegenThisTick(ticksPerHPRegen))
		{
			notifier.notify("Your next hitpoint will regenerate soon!");
		}

		if (config.showPrayerDoseIndicator())
		{
			doseOverlay.onTick();
		}

		if (!config.changeHealthIcon())
		{
			overlayManager.remove(poisonOverlay);
			clientThread.invoke(this::resetHealthIcon);
		}

		if (!config.showInfoboxes())
		{
			infoBoxManager.removeInfoBox(infobox);
			infobox = null;
		}

		for (PrayerType prayerType : PrayerType.values())
		{
			Prayer prayer = prayerType.getPrayer();
			int ord = prayerType.ordinal();

			if (!client.isPrayerActive(prayer)) {
				continue;
			}

		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (LiteRegenMeterConfig.GROUP.equals(event.getGroup()) && event.getKey().equals("hideAfterCombatDelay"))
		{
			clientThread.invokeLater(this::checkStatusBars);
		}

	}

	private void checkStatusBars()
	{
		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		final Actor interacting = localPlayer.getInteracting();

		if (config.hideAfterCombatDelay() == 0)
		{
			barsDisplayed = true;
		}
		else if ((interacting instanceof NPC && ArrayUtils.contains(((NPC) interacting).getComposition().getActions(), "Attack"))
				|| (interacting instanceof Player && client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1))
		{
			lastCombatActionTickCount = client.getTickCount();
			barsDisplayed = true;
		}
		else if (client.getTickCount() - lastCombatActionTickCount >= config.hideAfterCombatDelay())
		{
			barsDisplayed = false;
		}
	}

	private boolean shouldNotifyHpRegenThisTick(int ticksPerHPRegen)
	{
		// if the configured duration lies between two ticks, choose the earlier tick
		final int ticksBeforeHPRegen = ticksPerHPRegen - ticksSinceHPRegen;
		final int notifyTick = (int) Math.ceil(config.getNotifyBeforeHpRegenSeconds() * 1000d / Constants.GAME_TICK_LENGTH);
		return ticksBeforeHPRegen == notifyTick;
	}
	private int totalPrayerBonus(Item[] items)
	{
		int total = 0;
		for (Item item : items)
		{
			ItemStats is = itemManager.getItemStats(item.getId(), false);
			if (is != null && is.getEquipment() != null)
			{
				total += is.getEquipment().getPrayer();
			}
		}
		return total;
	}

	private void updatePotionBonus(ItemContainer inventory, @Nullable ItemContainer equip)
	{
		boolean hasPrayerPotion = false;
		boolean hasSuperRestore = false;
		boolean hasSanfew = false;
		boolean hasWrench = false;

		for (Item item : inventory.getItems())
		{
			final PrayerRestoreType type = PrayerRestoreType.getType(item.getId());

			if (type != null)
			{
				switch (type)
				{
					case PRAYERPOT:
						hasPrayerPotion = true;
						break;
					case RESTOREPOT:
						hasSuperRestore = true;
						break;
					case SANFEWPOT:
						hasSanfew = true;
						break;
					case HOLYWRENCH:
						hasWrench = true;
						break;
				}
			}
		}

		// Some items providing the holy wrench bonus can also be worn
		if (!hasWrench && equip != null)
		{
			for (Item item : equip.getItems())
			{
				final PrayerRestoreType type = PrayerRestoreType.getType(item.getId());
				if (type == PrayerRestoreType.HOLYWRENCH)
				{
					hasWrench = true;
					break;
				}
			}
		}

		// Prayer potion: floor(7 + 25% of base level) - 27% with holy wrench
		// Super restore: floor(8 + 25% of base level) - 27% with holy wrench
		// Sanfew serum: floor(4 + 30% of base level) - 32% with holy wrench
		final int prayerLevel = client.getRealSkillLevel(Skill.PRAYER);
		int restored = 0;
		if (hasSanfew)
		{
			restored = Math.max(restored, 4 + (int) Math.floor(prayerLevel *  (hasWrench ? .32 : .30)));
		}
		if (hasSuperRestore)
		{
			restored = Math.max(restored, 8 + (int) Math.floor(prayerLevel *  (hasWrench ? .27 : .25)));
		}
		if (hasPrayerPotion)
		{
			restored = Math.max(restored, 7 + (int) Math.floor(prayerLevel *  (hasWrench ? .27 : .25)));
		}

		doseOverlay.setRestoreAmount(restored);
	}

	private static int nextDamage(int poisonValue)
	{
		int damage;

		if (poisonValue >= VENOM_THRESHOLD)
		{
			//Venom Damage starts at 6, and increments in twos;
			//The VarPlayer increments in values of 1, however.
			poisonValue -= VENOM_THRESHOLD - 3;
			damage = poisonValue * 2;
			//Venom Damage caps at 20, but the VarPlayer keeps increasing
			if (damage > VENOM_MAXIUMUM_DAMAGE)
			{
				damage = VENOM_MAXIUMUM_DAMAGE;
			}
		}
		else
		{
			damage = (int) Math.ceil(poisonValue / 5.0f);
		}

		return damage;
	}

	private static int getDrainEffect(Client client)
	{
		int drainEffect = 0;

		for (PrayerType prayerType : PrayerType.values())
		{
			if (client.isPrayerActive(prayerType.getPrayer()))
			{
				drainEffect += prayerType.getDrainEffect();
			}
		}

		return drainEffect;
	}

	String getEstimatedTimeRemaining(boolean formatForOrb)
	{
		final int drainEffect = getDrainEffect(client);

		if (drainEffect == 0)
		{
			return "N/A";
		}

		// Calculate how many seconds each prayer points last so the prayer bonus can be applied
		// https://oldschool.runescape.wiki/w/Prayer#Prayer_drain_mechanics
		final int drainResistance = 2 * prayerBonus + 60;
		final double secondsPerPoint = 0.6 * ((double) drainResistance / drainEffect);

		// Calculate the number of seconds left
		final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		final double secondsLeft = (currentPrayer * secondsPerPoint);

		LocalTime timeLeft = LocalTime.ofSecondOfDay((long) secondsLeft);

		if (formatForOrb && (timeLeft.getHour() > 0 || timeLeft.getMinute() > 9))
		{
			long minutes = Duration.ofSeconds((long) secondsLeft).toMinutes();
			return String.format("%dm", minutes);
		}
		else if (timeLeft.getHour() > 0)
		{
			return timeLeft.format(DateTimeFormatter.ofPattern("H:mm:ss"));
		}
		else
		{
			return timeLeft.format(DateTimeFormatter.ofPattern("m:ss"));
		}
	}
	private BufferedImage getSplat(int id, int damage)
	{
		//Get a copy of the hitsplat to get a clean one each time
		final BufferedImage rawSplat = spriteManager.getSprite(id, 0);
		if (rawSplat == null)
		{
			return null;
		}

		final BufferedImage splat = new BufferedImage(
				rawSplat.getColorModel(),
				rawSplat.copyData(null),
				rawSplat.getColorModel().isAlphaPremultiplied(),
				null);

		final Graphics g = splat.getGraphics();
		g.setFont(FontManager.getRunescapeSmallFont());

		// Align the text in the centre of the hitsplat
		final FontMetrics metrics = g.getFontMetrics();
		final String text = String.valueOf(damage);
		final int x = (splat.getWidth() - metrics.stringWidth(text)) / 2;
		final int y = (splat.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

		g.setColor(Color.BLACK);
		g.drawString(String.valueOf(damage), x + 1, y + 1);
		g.setColor(Color.WHITE);
		g.drawString(String.valueOf(damage), x, y);
		return splat;
	}

	private static String getFormattedTime(Instant endTime)
	{
		final Duration timeLeft = Duration.between(Instant.now(), endTime);
		int seconds = (int) (timeLeft.toMillis() / 1000L);
		int minutes = seconds / 60;
		int secs = seconds % 60;

		return String.format("%d:%02d", minutes, secs);
	}

	String createTooltip()
	{
		String line1 = MessageFormat.format("Next {0} damage: {1}</br>Time until damage: {2}",
				envenomed ? "venom" : "poison", ColorUtil.wrapWithColorTag(String.valueOf(lastDamage), Color.RED), getFormattedTime(nextPoisonTick));
		String line2 = envenomed ? "" : MessageFormat.format("</br>Time until cure: {0}", getFormattedTime(poisonNaturalCure));

		return line1 + line2;
	}

	private void checkHealthIcon()
	{
		if (!config.changeHealthIcon())
		{
			return;
		}


		final BufferedImage newHeart;
		final int poison = client.getVarpValue(VarPlayer.POISON);

		if (poison >= VENOM_THRESHOLD)
		{
			newHeart = HEART_VENOM;
		}
		else if (poison > 0)
		{
			newHeart = HEART_POISON;
		}
		else if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0)
		{
			newHeart = HEART_DISEASE;
		}
		else
		{
			resetHealthIcon();
			return;
		}

		// Only update sprites when the heart icon actually changes
		if (newHeart != heart)
		{
			heart = newHeart;
			client.getWidgetSpriteCache().reset();

			// Get the sprite pixels and create a new image with an offset
			BufferedImage offsetHeart = new BufferedImage(newHeart.getWidth(), newHeart.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = offsetHeart.getGraphics();

			// Draw the heart image at (10, 0) to move it 10 pixels to the left
			g.drawImage(newHeart, -4, 0, null);
			g.dispose();

			client.getSpriteOverrides().put(SpriteID.MINIMAP_ORB_HITPOINTS_ICON, ImageUtil.getImageSpritePixels(offsetHeart, client));
		}
	}


	private void resetHealthIcon() {
		if (heart == null) {
			return;
		}

		client.getWidgetSpriteCache().reset();
		client.getSpriteOverrides().remove(SpriteID.MINIMAP_ORB_HITPOINTS_ICON);

		// Retrieve the default heart image from the sprite manager
		BufferedImage defaultHeart = spriteManager.getSprite(SpriteID.MINIMAP_ORB_HITPOINTS_ICON, 0);

		if (defaultHeart != null) {
			// Define the target dimensions for the image
			int targetWidth = 26;
			int targetHeight = 26;

			// Define the image's actual dimensions
			int imageWidth = 15;
			int imageHeight = 14;

			// Create a new buffered image with the target dimensions
			BufferedImage resizedHeart = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics g = resizedHeart.getGraphics();

			// Draw the heart image centered in the target dimensions with -4 offset on x-axis
			g.drawImage(defaultHeart, (targetWidth - imageWidth) / 2 - 3,
					(targetHeight - imageHeight) / 2, imageWidth, imageHeight, null);
			g.dispose();

			// Store the resized sprite into the sprite overrides
			client.getSpriteOverrides().put(SpriteID.MINIMAP_ORB_HITPOINTS_ICON, ImageUtil.getImageSpritePixels(resizedHeart, client));
		}

		heart = null; // Reset the heart variable
	}
}
