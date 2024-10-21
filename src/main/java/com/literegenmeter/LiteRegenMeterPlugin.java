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
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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
import net.runelite.http.api.item.ItemStats;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "LITE Regen Meter",
	description = "Track Regen timers and display status orbs for any skill, adjusted to work with the RuneLITE theme by Smoke (Smoked today).",
	tags = {"combat", "health", "hitpoints", "special", "attack", "overlay", "notifications", "runelite", "theme", "smoke", "varietyz", "orb", "bar", "poison", "venom", "disease", "heart", "status", "calculate", "dose", "prayer", "skilling"},
	conflicts = {"Regeneration Meter", "Poison", "Status Bars"}
)
@PluginDependency(ItemStatPlugin.class)
public class LiteRegenMeterPlugin extends Plugin
{
	static final int POISON_TICK_MILLIS = 18200;
	static final int VENOM_THRESHOLD = 1000000;
	static final int VENOM_MAXIUMUM_DAMAGE = 20;
	private static final int SPEC_REGEN_TICKS = 50;
	private static final int NORMAL_HP_REGEN_TICKS = 100;

	private int HEART_ICON_ID = 10485776;
	private int HEART_SPRITE_ID = 1067;

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
	private ChatMessageManager chatMessageManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ConfigManager configManager;

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

		spriteManager.addSpriteOverrides(LiteRegenSprites.values());

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

		spriteManager.removeSpriteOverrides(LiteRegenSprites.values());

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

		clientThread.invoke(()-> resetHealthIcon(true));
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged ev)
	{
		if (ev.getGameState() == GameState.HOPPING || ev.getGameState() == GameState.LOGIN_SCREEN)
		{
			ticksSinceHPRegen = -2;
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
		if (!event.getGroup().equals(LiteRegenMeterConfig.GROUP))
		{
			return;
		}

		if(event.getKey().equals("hideAfterCombatDelay"))
		{
			clientThread.invokeLater(this::checkStatusBars);
		}

		if(event.getKey().equals("enableStatBars"))
		{
			clientThread.invokeLater(this::checkStatusBars);
		}

		if(event.getKey().equals("poisonIcon"))
		{
			clientThread.invokeLater(this::checkHealthIcon);
		}

		if(event.getKey().equals("packMode"))
		{
			clientThread.invokeLater(this::checkHealthIcon);
		}

	}

	private void checkStatusBars()
	{
		if (!config.enableStatBars())
		{
			barsDisplayed = false;
			return;
		}
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
			poisonValue -= VENOM_THRESHOLD - 3;
			damage = poisonValue * 2;
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

		final int drainResistance = 2 * prayerBonus + 60;
		final double secondsPerPoint = 0.6 * ((double) drainResistance / drainEffect);

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

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int ORBS_UPDATE_HEALTH = 446;
		if (event.getScriptId() == ORBS_UPDATE_HEALTH)
		{
			//keep the icon updated when hp orb script is fired
			checkHealthIcon();
		}
	}

	private void checkHealthIcon()
	{
		if(!config.poisonIcon())
		{
			resetHealthIcon(false);
			return;
		}

		Widget hpIcon = client.getWidget(HEART_ICON_ID);

		if(hpIcon != null)
		{
			int STATUS_ICON_ID;
			final int poison = client.getVarpValue(VarPlayer.POISON);

			if (poison >= VENOM_THRESHOLD)
			{
				STATUS_ICON_ID = LiteRegenSprites.HEART_VENOM.getSpriteId();
			}
			else if (poison > 0)
			{
				STATUS_ICON_ID = LiteRegenSprites.HEART_POISON.getSpriteId();
			}
			else if (client.getVarpValue(VarPlayer.DISEASE_VALUE) > 0)
			{
				STATUS_ICON_ID = LiteRegenSprites.HEART_DISEASE.getSpriteId();
			}
			else
			{
				resetHealthIcon(false);
				return;
			}

			hpIcon.setSpriteId(STATUS_ICON_ID);
			hpIcon.setOriginalX(27 - getHealthIconXOffset());
			hpIcon.revalidate();
		}
	}

	private void resetHealthIcon(boolean shutdown)
	{
		Widget hpIcon = client.getWidget(HEART_ICON_ID);
		if(hpIcon != null)
		{
			if(hpIcon.getSpriteId() != HEART_SPRITE_ID)
				hpIcon.setSpriteId(HEART_SPRITE_ID);

			//on shutdown set it back to default (0) regardless of theme
			hpIcon.setOriginalX(27 - (shutdown ? 0 : getHealthIconXOffset()));
			hpIcon.revalidate();
		}
	}

	private int getHealthIconXOffset()
	{
		return (configManager.getConfiguration(LiteRegenMeterConfig.GROUP, "packMode").equals("VANILLA") ? 0 : 4);
	}
	
}
