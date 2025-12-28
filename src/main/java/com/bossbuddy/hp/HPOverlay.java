package com.bossbuddy.hp;

import javax.inject.Inject;
import com.bossbuddy.Boss;
import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.BossBuddyNPC;
import com.bossbuddy.BossBuddyPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.game.NPCManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import static net.runelite.api.gameval.NpcID.*;
import static net.runelite.api.gameval.VarbitID.HPBAR_HUD_BASEHP;
import static net.runelite.api.gameval.VarbitID.HPBAR_HUD_HP;

@Slf4j
public class HPOverlay extends Overlay
{
	@Inject
	private Client client;

	private final BossBuddyPlugin bossBuddyPlugin;
	private final BossBuddyConfig bossBuddyConfig;
	private final NPCManager npcManager;

	protected String lastFont = "";
	protected int lastFontSize = 0;
	protected boolean useRunescapeFont = true;
	protected Font font = null;

	NumberFormat format = new DecimalFormat("#");
	NumberFormat oneDecimalFormat = new DecimalFormat("#.0");
	NumberFormat twoDecimalFormat = new DecimalFormat("#.00");

	@Inject
	HPOverlay(BossBuddyPlugin plugin, BossBuddyConfig config, NPCManager npcManager)
	{
		setPriority(0.75f);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.bossBuddyPlugin = plugin;
		this.bossBuddyConfig = config;
		this.npcManager = npcManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		updateFont();
		graphics.setFont(font);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		if (bossBuddyConfig.showOverlay())
		{
			ArrayList<NPC> stackedNpcs = new ArrayList<>();
			bossBuddyPlugin.getBossBuddyNPCs().forEach((id, npc) -> renderTimer(npc, graphics, stackedNpcs));
		}
		return null;
	}

	private void renderTimer(final BossBuddyNPC bossBuddyNPC, final Graphics2D graphics, ArrayList<NPC> stackedNpcs)
	{
		if (bossBuddyNPC == null || bossBuddyNPC.isDead())
		{
			return;
		}

		NPC npc = bossBuddyNPC.getNpc();
		if (npc == null || npc.getName() == null)
		{
			return;
		}

		double wNpcHealthRatio = bossBuddyNPC.getHealthRatio();

		if (bossBuddyConfig.npcHideFull() && wNpcHealthRatio == 100)
		{
			return;
		}

		Integer maxHealth = npcManager.getHealth(npc.getId());

		if (Boss.isNpcBoss(npc))
		{
			Boss boss = Boss.find(npc.getId());
			if (boss.isIgnoreMaxHp())
			{
				maxHealth = client.getVarbitValue(HPBAR_HUD_BASEHP);

				if (npc.getId() == OLM_HEAD && isCoxOlmHandsAlive())
				{
					return;
				}
			}
			else if (boss.getMaxHp() != -1)
			{
				maxHealth = boss.getMaxHp();
			}
		}

		if (bossBuddyConfig.hpValue() || bossBuddyNPC.getIsTypeNumeric() == 1)
		{
			if (maxHealth != null && maxHealth != 0)
			{
				double numericHealth = (int) Math.floor((wNpcHealthRatio / 100) * maxHealth);
				bossBuddyNPC.setCurrentHp(numericHealth);
			}
		}

		Color timerColor = Color.GREEN;

		if (maxHealth != null && maxHealth > 1)
		{
			int curNumericHealth = (int) Math.floor((wNpcHealthRatio / 100) * maxHealth);
			timerColor = getGradientHpColor(curNumericHealth, maxHealth);
		}
		else
		{
			int curNumericHealth = (int) Math.floor(wNpcHealthRatio);
			timerColor = getGradientHpColor(curNumericHealth, 100);
		}

		String currentHPString = getCurrentHpString(bossBuddyNPC, true);
		if (maxHealth != null)
		{
			currentHPString += "/" + maxHealth;
		}
		String currentHPPercentString = getCurrentHpString(bossBuddyNPC, false);

		int offset = 0;
		NPC firstStack = null;
		for (NPC sNpc : stackedNpcs)
		{
			if (sNpc.getWorldLocation().getX() == npc.getWorldLocation().getX() && sNpc.getWorldLocation().getY() == npc.getWorldLocation().getY())
			{
				if (firstStack == null)
				{
					firstStack = npc;
				}
				offset += graphics.getFontMetrics().getHeight();
			}
		}

		int zOffset = 0;
		if (bossBuddyConfig.aboveHP())
		{
			zOffset += npc.getLogicalHeight();
		}
		Point textLocation = null;
		stackedNpcs.add(npc);
		if (firstStack != null && offset > 0)
		{
			textLocation = firstStack.getCanvasTextLocation(graphics, currentHPString, zOffset);
		}
		else
		{
			textLocation = npc.getCanvasTextLocation(graphics, currentHPString, zOffset);
		}
		if (textLocation != null)
		{
			Point stackOffset = new Point(textLocation.getX() + bossBuddyConfig.overlayRight() - bossBuddyConfig.overlayLeft(), (textLocation.getY() - bossBuddyConfig.overlayUp() + bossBuddyConfig.overlayDown()) - offset);

			if (bossBuddyConfig.hpValue() && !bossBuddyConfig.hpPercentage())
			{
				handleText(graphics, stackOffset, currentHPString, timerColor);
			}
			else if (!bossBuddyConfig.hpValue() && bossBuddyConfig.hpPercentage())
			{
				handleText(graphics, stackOffset, currentHPPercentString + "%", timerColor);
			}
			else if (bossBuddyConfig.hpValue() && bossBuddyConfig.hpPercentage())
			{
				if (bossBuddyConfig.stackOverlay())
				{
					handleText(graphics, stackOffset, "[ " + currentHPString + " ]", timerColor);
					stackOffset = new Point(textLocation.getX() + bossBuddyConfig.overlayRight() - bossBuddyConfig.overlayLeft(), (textLocation.getY() - bossBuddyConfig.overlayUp() + bossBuddyConfig.overlayDown() + graphics.getFontMetrics().getHeight()) - offset);
					handleText(graphics, stackOffset, currentHPPercentString + "%", timerColor);
				}
				else
				{
					handleText(graphics, stackOffset, "[ " + currentHPString + " ] " + currentHPPercentString + "%", timerColor);
				}
			}
		}
	}

	private boolean isCoxOlmHandsAlive()
	{
		for (BossBuddyNPC wnpc : bossBuddyPlugin.getBossBuddyNPCs().values())
		{
			NPC npc = wnpc.getNpc();
			if (npc == null)
			{
				continue;
			}

			int id = npc.getId();
			if ((id == OLM_HAND_RIGHT || id == OLM_HAND_LEFT) && !npc.isDead())
			{
				return true;
			}
		}

		return false;
	}

	private String getCurrentHpString(BossBuddyNPC bossBuddyNPC, boolean numeric)
	{
		NPC npc = bossBuddyNPC.getNpc();

		if (numeric)
		{
			String healthRatio = format.format(bossBuddyNPC.getHealthRatio());

			boolean usePercentage = bossBuddyNPC.getCurrentHp() == 100 && bossBuddyNPC.getHealthRatio() < 100.0;

			if (Boss.isNpcBoss(npc))
			{
				int curHp = client.getVarbitValue(HPBAR_HUD_HP);
				Boss boss = Boss.find(npc.getId());
				if(!boss.isIgnoreMaxHp())
				{
					curHp = (int) bossBuddyNPC.getCurrentHp();
				}
				return usePercentage ? healthRatio : String.valueOf(curHp);
			}

			return usePercentage ? healthRatio : String.valueOf((int) (bossBuddyNPC.getCurrentHp()));
		}

		switch (bossBuddyConfig.decimalHp())
		{
			case 1:
				return String.valueOf(oneDecimalFormat.format(bossBuddyNPC.getHealthRatio()));
			case 2:
				return String.valueOf(twoDecimalFormat.format(bossBuddyNPC.getHealthRatio()));
			default:
				return String.valueOf((bossBuddyNPC.getHealthRatio() >= 1) ? format.format(bossBuddyNPC.getHealthRatio()) : twoDecimalFormat.format(bossBuddyNPC.getHealthRatio()));
		}

	}

	private void handleText(Graphics2D graphics, Point textLoc, String text, Color color)
	{
		int offsetShadow = 1;

		graphics.setColor(new Color(0, 0, 0, color.getAlpha()));
		graphics.drawString(text, textLoc.getX() + offsetShadow, textLoc.getY() + offsetShadow);
		graphics.setColor(color);
		graphics.drawString(text, textLoc.getX(), textLoc.getY());
	}

	private void updateFont()
	{
		if (lastFontSize != bossBuddyConfig.fontSize())
		{
			lastFont = "roboto";
			lastFontSize = bossBuddyConfig.fontSize();
			int style = Font.PLAIN;
			font = new Font(lastFont, style, bossBuddyConfig.fontSize());
			useRunescapeFont = false;
		}
	}

	private Color getGradientHpColor(int currentHealth, int maxHealth)
	{
		currentHealth = Math.min(maxHealth, Math.max(0, currentHealth));

		double healthPercentage = (double) currentHealth / maxHealth;

		Color colorA = Color.GREEN;
		Color colorB = Color.RED;

		int red = (int) (colorB.getRed() + (colorA.getRed() - colorB.getRed()) * healthPercentage);
		int green = (int) (colorB.getGreen() + (colorA.getGreen() - colorB.getGreen()) * healthPercentage);
		int blue = (int) (colorB.getBlue() + (colorA.getBlue() - colorB.getBlue()) * healthPercentage);

		return new Color(red, green, blue);
	}
}
