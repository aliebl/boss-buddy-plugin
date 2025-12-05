package com.bossbuddy;

import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.runelite.http.api.loottracker.LootRecordType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"type", "name"})
public class ConfigLoot
{
	LootRecordType type;
	String name;
	int kills;
	Instant first = Instant.now();
	Instant last;
	String lastDrop;
	int lastDropKC;
	int gePrice;
	int[] drops;

	ConfigLoot(String name)
	{
		this.type = LootRecordType.NPC;
		this.name = name;
		this.drops = new int[0];
	}

	void add(int id, int killCount, int gePrice, int date)
	{
		int[] newDrops = new int[drops.length + 4];

		for (int i = 0; i < drops.length + 4; i += 4)
		{
			if (i == 0)
			{
				newDrops[0] = id;
				newDrops[1] = killCount;
				newDrops[2] = gePrice;
				newDrops[3] = date;
			}
			else
			{
				newDrops[i] = drops[i - 4];
				newDrops[i + 1] = drops[i - 3];
				newDrops[i + 2] = drops[i - 2];
				newDrops[i + 3] = drops[i - 1];
			}
		}
		drops = newDrops;
	}

	public int numDrops()
	{
		return drops.length / 4;
	}
}