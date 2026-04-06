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
	ABYSSAL_SIRE(ABYSSALSIRE_SIRE_STASIS_AWAKE, 15, RSTimeUnit.GAME_TICKS, ItemID.ABYSSALSIRE_PET, false, true, false,425),
	ALCHEMICAL_HYDRA(NpcID.HYDRABOSS_FINALDEATH, 25200, ChronoUnit.MILLIS, ItemID.HYDRAPET,1100),
	AMOXLIATL(NpcID.AMOXLIATL, 28, RSTimeUnit.GAME_TICKS, ItemID.AMOXLIATLPET,520),
	ARAXXOR(NpcID.ARAXXOR_DEAD, 15, RSTimeUnit.GAME_TICKS, ItemID.ARAXXORPET, true, false, false, 1020),
	ARAXXOR_ALIVE(NpcID.ARAXXOR, 15, RSTimeUnit.GAME_TICKS, ItemID.ARAXXORPET, true, true, false, 1020),
	ARTIO(NpcID.CALLISTO_SINGLES, 11, RSTimeUnit.GAME_TICKS, ItemID.CALLISTO_PET,450),
	BABA(TOA_BABA,15,  RSTimeUnit.GAME_TICKS, ItemID.TOA_BOOK_BABA,3140),
	CALLISTO(NpcID.CALLISTO, 11, RSTimeUnit.GAME_TICKS, ItemID.CALLISTO_PET,1000),
	CALVARION(NpcID.VETION_2_SINGLE, 14, RSTimeUnit.GAME_TICKS, ItemID.VETION_PET,150),
	CERBERUS(NpcID.CERBERUS_ATTACKING, 8400, ChronoUnit.MILLIS, ItemID.HELL_PET,600),
	CERBERUS_SITTING(NpcID.CERBERUS_SITTING, 8400, ChronoUnit.MILLIS, ItemID.HELL_PET),
	CHAOS_ELEMENTAL(NpcID.CHAOSELEMENTAL, 14, RSTimeUnit.GAME_TICKS, ItemID.CHAOSELEPET,250),
	CHAOS_FANATIC(NpcID.CHAOS_FANATIC, 14, RSTimeUnit.GAME_TICKS, ItemID.STAFF_OF_ZAROS,225),
	COMMANDER_ZILYANA(NpcID.GODWARS_SARADOMIN_AVATAR, 90, ChronoUnit.SECONDS, ItemID.SARADOMINPET,255),
	CORPOREAL_BEAST(NpcID.CORP_BEAST, 30, ChronoUnit.SECONDS, ItemID.COREPET,2000),
	CRAZY_ARCHAEOLOGIST(NpcID.CRAZY_ARCHAEOLOGIST, 9, ChronoUnit.SECONDS, ItemID.FEDORA,225),
	DAGANNOTH_PRIME(NpcID.DAGCAVE_MAGIC_BOSS, 90, ChronoUnit.SECONDS, ItemID.PRIMEPET,255),
	DAGANNOTH_REX(NpcID.DAGCAVE_MELEE_BOSS, 90, ChronoUnit.SECONDS, ItemID.REXPET,255),
	DAGANNOTH_SUPREME(NpcID.DAGCAVE_RANGED_BOSS, 90, ChronoUnit.SECONDS, ItemID.SUPREMEPET,255),
	DERANGED_ARCHAEOLOGIST(NpcID.FOSSIL_CRAZY_ARCHAEOLOGIST, 29400, ChronoUnit.MILLIS, ItemID.FOSSIL_LARGE_UNID,200),
	DOOM(DOM_BOSS, 90, ChronoUnit.MILLIS,ItemID.DOMPET, true,true, true, -1),
	DOOM_SHIELD(DOM_BOSS_SHIELDED, 90, ChronoUnit.MILLIS,ItemID.DOMPET, true,true, true, -1),
	DOOM_BURROW(DOM_BOSS_BURROWED, 90, ChronoUnit.MILLIS,ItemID.DOMPET, true,true, true, -1),
	DUKE_AWAKE(DUKE_SUCELLUS_AWAKE,15,RSTimeUnit.GAME_TICKS, ItemID.DUKESUCELLUSPET,440),
	DUSK(NpcID.GARGBOSS_DUSK_DEATH, 5, ChronoUnit.MINUTES, ItemID.DAWNPET, false, true, true,-1),
	GENERAL_GRAARDOR(NpcID.GODWARS_BANDOS_AVATAR, 90, ChronoUnit.SECONDS, ItemID.BANDOSPET,255),
	GIANT_MOLE(NpcID.MOLE_GIANT, 9000, ChronoUnit.MILLIS, ItemID.MOLEPET, 200),
	HUEYCOATL(NpcID.HUEY_HEAD_DEFEATED, 50, RSTimeUnit.GAME_TICKS, ItemID.HUEYPET,2500),
	KALPHITE_QUEEN(NpcID.KALPHITE_QUEEN, 30, ChronoUnit.SECONDS, ItemID.KQPET_WALKING,255),
	KALPHITE_QUEENFLYING(NpcID.KALPHITE_FLYINGQUEEN, 30, ChronoUnit.SECONDS, ItemID.KQPET_WALKING,255),
	KING_BLACK_DRAGON(NpcID.KING_DRAGON, 9, ChronoUnit.SECONDS, ItemID.KBDPET, 255),
	KRAKEN(NpcID.SLAYER_KRAKEN_BOSS, 8400, ChronoUnit.MILLIS, ItemID.KRAKENPET),
	KREEARRA(NpcID.GODWARS_ARMADYL_AVATAR, 90, ChronoUnit.SECONDS, ItemID.ARMADYLPET,255),
	KRIL_TSUTSAROTH(NpcID.GODWARS_ZAMORAK_AVATAR, 90, ChronoUnit.SECONDS, ItemID.ZAMORAKPET,255),
	PHANTOM_MUSPAH_MELEE(MUSPAH_MELEE, 50, RSTimeUnit.GAME_TICKS, ItemID.MUSPAHPET,850),
	PHANTOM_MUSPAH(MUSPAH, 50, RSTimeUnit.GAME_TICKS, ItemID.MUSPAHPET,850),
	PHANTOM_MUSPAH_FINAL(NpcID.MUSPAH_FINAL, 50, RSTimeUnit.GAME_TICKS, ItemID.MUSPAHPET,850),
	ICE_KING(NpcID.RT_ICE_KING,30, RSTimeUnit.GAME_TICKS,ItemID.RTELDRICPET,600),
	FIRE_QUEEN(RT_FIRE_QUEEN,30, RSTimeUnit.GAME_TICKS,ItemID.RTBRANDAPET,600),
	THERMONUCLEAR_SMOKE_DEVIL(NpcID.SMOKE_DEVIL_BOSS, 8400, ChronoUnit.MILLIS, ItemID.SMOKEPET,240),
	SARACHNIS(NpcID.SARACHNIS, 16, RSTimeUnit.GAME_TICKS, ItemID.SARACHNISPET,400),
	SCORPIA(NpcID.SCORPIA, 14, RSTimeUnit.GAME_TICKS, ItemID.SCORPIA_PET,200),
	SCURRIUS(NpcID.RAT_BOSS_NORMAL, 29, RSTimeUnit.GAME_TICKS, ItemID.SCURRIUSPET,1500),
	SCURRIUS_PRIVATE(NpcID.RAT_BOSS_INSTANCE, 29, RSTimeUnit.GAME_TICKS, ItemID.SCURRIUSPET,500),
	SPINDEL(NpcID.VENENATIS_SINGLES, 9, RSTimeUnit.GAME_TICKS, ItemID.VENENATIS_PET,515),
	THE_LEVIATHAN(NpcID.LEVIATHAN, 30, RSTimeUnit.GAME_TICKS, ItemID.LEVIATHANPET,900),
	THE_WHISPERER(WHISPERER, 30, RSTimeUnit.GAME_TICKS, ItemID.WHISPERERPET,900),
	VENENATIS(NpcID.VENENATIS, 10, RSTimeUnit.GAME_TICKS, ItemID.VENENATIS_PET,850),
	VETION(NpcID.VETION_2, 15, RSTimeUnit.GAME_TICKS, ItemID.VETION_PET,255),
	VORKATH(NpcID.VORKATH, 15, RSTimeUnit.GAME_TICKS, ItemID.VORKATHPET,true,true,false,750),
	VARDORVIS(NpcID.VARDORVIS,15, RSTimeUnit.GAME_TICKS, ItemID.VARDORVISPET,700),
	YAMA(NpcID.YAMA,15,RSTimeUnit.GAME_TICKS, ItemID.YAMAPET, true,true,false,2500),
	ZALCANO(NpcID.ZALCANO_WEAK, 21600, ChronoUnit.MILLIS, ItemID.ZALCANOPET),
	OLM_HAND_RIGHT(NpcID.OLM_HAND_RIGHT,15, RSTimeUnit.GAME_TICKS, ItemID.OLMPET, true, true, false, 600),
	OLM_HAND_LEFT(NpcID.OLM_HAND_LEFT,15, RSTimeUnit.GAME_TICKS, ItemID.OLMPET, true, true, false, 600);


	private static final Map<Integer, Boss> bosses;
	private final int id;
	private final Duration spawnTime;
	private final int itemSpriteId;
	private final boolean ignoreDead;
	private final boolean ignoreAlarm;
	private final boolean ignoreMaxHp;
	private final int maxHp;

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
		this(id, period, unit, itemSpriteId, false, false, true, -1);
	}

	Boss(int id, long period, TemporalUnit unit, int itemSpriteId, int maxHp)
	{
		this(id, period, unit, itemSpriteId, false, false, false, maxHp);
	}

	Boss(int id, long period, TemporalUnit unit, int itemSpriteId, boolean ignoreDead, boolean ignoreAlarm, boolean ignoreMaxHp, int maxHp)
	{
		this.id = id;
		this.spawnTime = Duration.of(period, unit);
		this.itemSpriteId = itemSpriteId;
		this.ignoreDead = ignoreDead;
		this.ignoreAlarm = ignoreAlarm;
		this.ignoreMaxHp = ignoreMaxHp;
		this.maxHp = maxHp;
	}

	// Tombs of Amascut - String identification seems like the way to identify these bosses (with blacklist control)
	private static final ImmutableSet<String> TOA_BOSS_NAMES = ImmutableSet.of("Akkha", "Kephri", "Zebak", "Tumeken's Warden", "Elidinis' Warden");

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
		WHISPERER,
		DUKE_SUCELLUS_AWAKE, DUKE_SUCELLUS_ASLEEP
	);

	// Generic bosses - bosses that does not have a specific section
	private static final ImmutableSet<Integer> GEN_BOSS_IDS = ImmutableSet.of(
	);


	private static final ImmutableSet<Integer> ALL_BOSS_IDS =
		ImmutableSet.<Integer>builder()
			.addAll(GEN_BOSS_IDS)
			.addAll(COX_BOSS_IDS)
			.addAll(DT2_BOSS_IDS)
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