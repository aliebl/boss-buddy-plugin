package com.bossbuddy.respawn;

import com.bossbuddy.Boss;
import com.bossbuddy.BossBuddyNPC;
import com.bossbuddy.BossBuddyPlugin;
import lombok.Getter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.AsyncBufferedImage;

import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;

@Getter
public class RespawnTimer extends Timer
{
	private final Boss boss;
	private final BossBuddyNPC bossBuddyNPC;

	public RespawnTimer(Boss boss, BufferedImage bossImage, Plugin plugin)
	{
		super(boss.getSpawnTime().toMillis(), ChronoUnit.MILLIS, bossImage, plugin);
		this.boss = boss;
		this.bossBuddyNPC = null;
	}

	public RespawnTimer(BossBuddyNPC bossBuddyNPC, AsyncBufferedImage image, BossBuddyPlugin bossBuddyPlugin)
	{
		super(bossBuddyNPC.getRespawnTime() * 600L, ChronoUnit.MILLIS, image, bossBuddyPlugin);
		this.boss = null;
		this.bossBuddyNPC = bossBuddyNPC;
	}

	public RespawnTimer(BossBuddyNPC bossBuddyNPC, Boss boss, BufferedImage bossImage, Plugin plugin)
	{
		super(boss.getSpawnTime().toMillis(), ChronoUnit.MILLIS, bossImage, plugin);
		this.boss = boss;
		this.bossBuddyNPC = bossBuddyNPC;
	}
}