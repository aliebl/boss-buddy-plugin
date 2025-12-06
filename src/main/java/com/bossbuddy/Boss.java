package com.bossbuddy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.runelite.api.NPC;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.RSTimeUnit;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;

import static net.runelite.api.gameval.NpcID.*;

@Getter
public enum Boss
{
	GENERAL_GRAARDOR(NpcID.GODWARS_BANDOS_AVATAR, 90, ChronoUnit.SECONDS, ItemID.BANDOSPET),
	KRIL_TSUTSAROTH(NpcID.GODWARS_ZAMORAK_AVATAR, 90, ChronoUnit.SECONDS, ItemID.ZAMORAKPET),
	KREEARRA(NpcID.GODWARS_ARMADYL_AVATAR, 90, ChronoUnit.SECONDS, ItemID.ARMADYLPET),
	COMMANDER_ZILYANA(NpcID.GODWARS_SARADOMIN_AVATAR, 90, ChronoUnit.SECONDS, ItemID.SARADOMINPET),
	CALLISTO(NpcID.CALLISTO, 11, RSTimeUnit.GAME_TICKS, ItemID.CALLISTO_PET),
	ARTIO(NpcID.CALLISTO_SINGLES, 11, RSTimeUnit.GAME_TICKS, ItemID.CALLISTO_PET),
	CHAOS_ELEMENTAL(NpcID.CHAOSELEMENTAL, 14, RSTimeUnit.GAME_TICKS, ItemID.CHAOSELEPET),
	CHAOS_FANATIC(NpcID.CHAOS_FANATIC, 14, RSTimeUnit.GAME_TICKS, ItemID.STAFF_OF_ZAROS),
	CRAZY_ARCHAEOLOGIST(NpcID.CRAZY_ARCHAEOLOGIST, 9, ChronoUnit.SECONDS, ItemID.FEDORA),
	KING_BLACK_DRAGON(NpcID.KING_DRAGON, 9, ChronoUnit.SECONDS, ItemID.KBDPET),
	SCORPIA(NpcID.SCORPIA, 14, RSTimeUnit.GAME_TICKS, ItemID.SCORPIA_PET),
	SCURRIUS(NpcID.RAT_BOSS_NORMAL, 29, RSTimeUnit.GAME_TICKS, ItemID.SCURRIUSPET),
	SCURRIUS_PRIVATE(NpcID.RAT_BOSS_INSTANCE, 29, RSTimeUnit.GAME_TICKS, ItemID.SCURRIUSPET),
	VENENATIS(NpcID.VENENATIS, 10, RSTimeUnit.GAME_TICKS, ItemID.VENENATIS_PET),
	SPINDEL(NpcID.VENENATIS_SINGLES, 9, RSTimeUnit.GAME_TICKS, ItemID.VENENATIS_PET),
	VETION(NpcID.VETION_2, 15, RSTimeUnit.GAME_TICKS, ItemID.VETION_PET),
	CALVARION(NpcID.VETION_2_SINGLE, 14, RSTimeUnit.GAME_TICKS, ItemID.VETION_PET),
	DAGANNOTH_PRIME(NpcID.DAGCAVE_MAGIC_BOSS, 90, ChronoUnit.SECONDS, ItemID.PRIMEPET),
	DAGANNOTH_REX(NpcID.DAGCAVE_MELEE_BOSS, 90, ChronoUnit.SECONDS, ItemID.REXPET),
	DAGANNOTH_SUPREME(NpcID.DAGCAVE_RANGED_BOSS, 90, ChronoUnit.SECONDS, ItemID.SUPREMEPET),
	CORPOREAL_BEAST(NpcID.CORP_BEAST, 30, ChronoUnit.SECONDS, ItemID.COREPET),
	GIANT_MOLE(NpcID.MOLE_GIANT, 9000, ChronoUnit.MILLIS, ItemID.MOLEPET),
	DERANGED_ARCHAEOLOGIST(NpcID.FOSSIL_CRAZY_ARCHAEOLOGIST, 29400, ChronoUnit.MILLIS, ItemID.FOSSIL_LARGE_UNID),
	CERBERUS(NpcID.CERBERUS_ATTACKING, 8400, ChronoUnit.MILLIS, ItemID.HELL_PET),
	THERMONUCLEAR_SMOKE_DEVIL(NpcID.SMOKE_DEVIL_BOSS, 8400, ChronoUnit.MILLIS, ItemID.SMOKEPET),
	KRAKEN(NpcID.SLAYER_KRAKEN_BOSS, 8400, ChronoUnit.MILLIS, ItemID.KRAKENPET),
	KALPHITE_QUEEN(NpcID.KALPHITE_FLYINGQUEEN, 30, ChronoUnit.SECONDS, ItemID.KQPET_WALKING),
	DUSK(NpcID.GARGBOSS_DUSK_DEATH, 5, ChronoUnit.MINUTES, ItemID.DAWNPET, false, true),
	ALCHEMICAL_HYDRA(NpcID.HYDRABOSS_FINALDEATH, 25200, ChronoUnit.MILLIS, ItemID.HYDRAPET),
	SARACHNIS(NpcID.SARACHNIS, 16, RSTimeUnit.GAME_TICKS, ItemID.SARACHNISPET),
	ZALCANO(NpcID.ZALCANO_WEAK, 21600, ChronoUnit.MILLIS, ItemID.ZALCANOPET),
	PHANTOM_MUSPAH(NpcID.MUSPAH_FINAL, 50, RSTimeUnit.GAME_TICKS, ItemID.MUSPAHPET),
	THE_LEVIATHAN(NpcID.LEVIATHAN, 30, RSTimeUnit.GAME_TICKS, ItemID.LEVIATHANPET),
	ARAXXOR(NpcID.ARAXXOR_DEAD, 15, RSTimeUnit.GAME_TICKS, ItemID.ARAXXORPET, true, true),
	AMOXLIATL(NpcID.AMOXLIATL, 28, RSTimeUnit.GAME_TICKS, ItemID.AMOXLIATLPET),
	HUEYCOATL(NpcID.HUEY_HEAD_DEFEATED, 50, RSTimeUnit.GAME_TICKS, ItemID.HUEYPET),
	;

	private static final Map<Integer, Boss> bosses;
	private final int id;
	private final Duration spawnTime;
	private final int itemSpriteId;
	private final boolean ignoreDead;
	private final boolean ignoreAlarm;

	static
	{
		ImmutableMap.Builder<Integer, Boss> builder = new ImmutableMap.Builder<>();

		for (Boss boss : values())
		{
			builder.put(boss.getId(), boss);
		}

		bosses = builder.build();
	}

	Boss(int id, long period, TemporalUnit unit, int itemSpriteId)
	{
		this(id, period, unit, itemSpriteId, false, false);
	}

	Boss(int id, long period, TemporalUnit unit, int itemSpriteId, boolean ignoreDead, boolean ignoreAlarm)
	{
		this.id = id;
		this.spawnTime = Duration.of(period, unit);
		this.itemSpriteId = itemSpriteId;
		this.ignoreDead = ignoreDead;
		this.ignoreAlarm = ignoreAlarm;
	}

	// Tombs of Amascut - String identification seems like the way to identify these bosses (with blacklist control)
	private static final ImmutableSet<String> TOA_BOSS_NAMES = ImmutableSet.of("Akkha", "Kephri", "Zebak", "Ba-Ba", "Tumeken's Warden", "Elidinis' Warden");

	// Chambers of Xeric - Some ids for cox that support varbits
	private static final ImmutableSet<Integer> COX_BOSS_IDS = ImmutableSet.of(
		// The Great Olm
		OLM_HEAD,
		// Tekton
		RAIDS_TEKTON_WAITING, RAIDS_TEKTON_WALKING_STANDARD, RAIDS_TEKTON_FIGHTING_STANDARD, RAIDS_TEKTON_WALKING_ENRAGED, RAIDS_TEKTON_FIGHTING_ENRAGED, RAIDS_TEKTON_HAMMERING,
		// Vespula
		RAIDS_VESPULA_FLYING, RAIDS_VESPULA_ENRAGED, RAIDS_VESPULA_WALKING, RAIDS_VESPULA_PORTAL,
		// Muttadile
		RAIDS_DOGODILE_SUBMERGED, RAIDS_DOGODILE_JUNIOR, RAIDS_DOGODILE,
		// Vasa
		RAIDS_VASANISTIRIO_WALKING, RAIDS_VASANISTIRIO_HEALING
	);

	// Desert Treasure 2
	private static final ImmutableSet<Integer> DT2_BOSS_IDS = ImmutableSet.of(
		LEVIATHAN,
		VARDORVIS, VARDORVIS_BASE_POSTQUEST,
		WHISPERER,
		DUKE_SUCELLUS_AWAKE, DUKE_SUCELLUS_ASLEEP
	);

	// Generic bosses - bosses that does not have a specific section
	private static final ImmutableSet<Integer> GEN_BOSS_IDS = ImmutableSet.of(
		YAMA
	);

	// Doom of Mokhaiotl
	public static final ImmutableSet<Integer> DOOM_BOSS_IDS = ImmutableSet.of(
		DOM_BOSS,
		DOM_BOSS_SHIELDED,
		DOM_BOSS_BURROWED
	);

	private static final ImmutableSet<Integer> ALL_BOSS_IDS =
		ImmutableSet.<Integer>builder()
			.addAll(GEN_BOSS_IDS)
			.addAll(COX_BOSS_IDS)
			.addAll(DT2_BOSS_IDS)
			.addAll(DOOM_BOSS_IDS)
			.build();

	public static boolean isNpcBossFromTOA(NPC npc)
	{
		int id = npc.getId();
		String name = npc.getName();
		boolean isWardenP1 = (id == TOA_WARDEN_TUMEKEN_PHASE1 || id == TOA_WARDEN_ELIDINIS_PHASE1);

		return name != null && TOA_BOSS_NAMES.contains(name) && !isWardenP1;
	}

	public static boolean isNpcBoss(NPC npc)
	{
		if (ALL_BOSS_IDS.contains(npc.getId()) || bosses.get(npc.getId()) != null)
		{
			return true;
		}
		// Only check TOA names if ID lookup fails
		return isNpcBossFromTOA(npc);
	}

	public static Boss find(int id)
	{
		return bosses.get(id);
	}
}