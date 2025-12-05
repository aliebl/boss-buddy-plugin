package com.bossbuddy;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

public class BossBuddyNPC
{
	@Getter
	private final int npcIndex;

	@Getter
	private final String npcName;

	@Getter
	private final int id;
	@Getter
	@Setter
	private NPC npc;

	@Getter
	@Setter
	private WorldPoint currentLocation;

	@Getter
	@Setter
	private double currentHp;

	@Getter
	@Setter
	private double healthRatio;

	@Getter
	@Setter
	private double healthScale;

	@Getter
	@Setter
	private boolean isDead;

	@Getter
	@Setter
	private int offset;

	@Getter
	@Setter
	private int isTypeNumeric;

	@Getter
	private int npcSize;

	/**
	 * The time the npc died at, in game ticks, relative to the tick counter
	 */
	@Getter
	@Setter
	private int diedOnTick;

	/**
	 * The time it takes for the npc to respawn, in game ticks
	 */
	@Getter
	@Setter
	private int respawnTime;

	@Getter
	@Setter
	private List<WorldPoint> possibleRespawnLocations;

	BossBuddyNPC(NPC npc)
	{
		this.npc = npc;
		this.id = npc.getId();
		this.npcName = npc.getName();
		this.npcIndex = npc.getIndex();
		this.currentLocation = npc.getWorldLocation();
		this.currentHp = 100;
		this.healthRatio = 100;
		this.healthScale = npc.getHealthScale();
		this.isDead = false;
		this.offset = 0;
		this.isTypeNumeric = 0;

		this.possibleRespawnLocations = new ArrayList<>(2);
		this.respawnTime = -1;
		this.diedOnTick = -1;

		final NPCComposition composition = npc.getTransformedComposition();

		if (composition != null)
		{
			this.npcSize = composition.getSize();
		}
	}
}
