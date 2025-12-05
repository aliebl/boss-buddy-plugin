package com.bossbuddy.respawn;

import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.BossBuddyNPC;
import com.bossbuddy.BossBuddyPlugin;
import com.google.common.base.Strings;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class RespawnOverlay extends Overlay
{

	private static final int MAX_DRAW_DISTANCE = 32;

	private final Client client;
	private final Map<Integer, BossBuddyNPC> bossBuddyNPCs;
	private final BossBuddyConfig config;
	private final BossBuddyPlugin plugin;

	@Inject
	private RespawnOverlay(Client client, BossBuddyConfig config, BossBuddyPlugin plugin)
	{
		this.bossBuddyNPCs = plugin.bossBuddyNPCs;
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!this.config.npcRespawnLocation())
		{
			return null;
		}

		for (Integer npcIndex : this.bossBuddyNPCs.keySet())
		{
			BossBuddyNPC bossBuddyNPC = this.bossBuddyNPCs.get(npcIndex);
			if (!bossBuddyNPC.isDead())
			{
				continue;
			}

			final List<WorldPoint> spawnPoints = bossBuddyNPC.getPossibleRespawnLocations();

			for (WorldPoint spawnPoint : spawnPoints)
			{
				String text;
				Stroke stroke = new BasicStroke((float) 2);//config.borderWidth());
				Color tileColor = Color.CYAN;//point.getColor();
				final Instant now = Instant.now();
				final double baseTick = (bossBuddyNPC.getRespawnTime() - (client.getTickCount() - bossBuddyNPC.getDiedOnTick())) * (Constants.GAME_TICK_LENGTH / 1000.0);
				final double sinceLast = (now.toEpochMilli() - plugin.lastTickUpdate.toEpochMilli()) / 1000.0;
				final double timeLeft = Math.max(0, baseTick - sinceLast);
				text = String.valueOf(timeLeft);
				if (text.contains("."))
				{
					text = text.substring(0, text.indexOf(".") + 2);
				}
				WorldView wv = client.findWorldViewFromWorldPoint(spawnPoint);
				drawTile(graphics, wv, spawnPoint, tileColor, text, stroke);
			}
		}
		return null;
	}

	private void drawTile(Graphics2D graphics, WorldView wv, WorldPoint point, Color color, @Nullable String label, Stroke borderStroke)
	{
		if (wv.isTopLevel())
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

			if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
			{
				return;
			}
		}

		LocalPoint lp = LocalPoint.fromWorld(wv, point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color, new Color(0, 0, 0, 50), borderStroke);
		}

		if (!Strings.isNullOrEmpty(label))
		{
			Point canvasTextLocation = Perspective.getCanvasTextLocation(client, graphics, lp, label, 0);
			if (canvasTextLocation != null)
			{

				OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
			}
		}
	}
}
