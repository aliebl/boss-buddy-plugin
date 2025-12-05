package com.bossbuddy;

import com.bossbuddy.hp.HPOverlay;
import com.bossbuddy.respawn.RespawnOverlay;
import com.bossbuddy.gearhelper.EquipmentOverlay;
import com.bossbuddy.loot.BossDropFraction;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.osrswiki.DropTableSection;
import com.bossbuddy.osrswiki.WikiItem;
import com.bossbuddy.osrswiki.WikiScraper;
import com.bossbuddy.respawn.RespawnNotification;
import com.bossbuddy.respawn.RespawnTimer;
import com.bossbuddy.util.Icons;
import com.bossbuddy.util.Constants;
import com.bossbuddy.views.BossBuddyPanel;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.Notifier;
import net.runelite.client.chat.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.api.WorldView;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import static net.runelite.api.gameval.NpcID.*;
import static net.runelite.api.gameval.VarbitID.*;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import okhttp3.OkHttpClient;

@SuppressWarnings("SameReturnValue")
@Slf4j
@PluginDescriptor(
	name = "Boss Buddy"
)
public class BossBuddyPlugin extends Plugin
{
	ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	private static final int MAX_ACTOR_VIEW_RANGE = 15;
	private static final int MAX_RESPAWN_TIME_TICKS = 500;

	@Setter
	private boolean isUserLoggedIn = false;

	private boolean skipNextSpawnCheck = false;
	private static final int COINS = ItemID.COINS;
	private static final Duration WAIT = Duration.ofSeconds(5);

	@Getter(AccessLevel.PACKAGE)
	private Actor lastOpponent;

	@Getter(AccessLevel.PACKAGE)
	private NPC lastNPC;

	@Getter(AccessLevel.PACKAGE)
	@VisibleForTesting
	private Instant lastTime;

	@Inject
	private Client client;


	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HPOverlay hpOverlay;

	@Inject
	private RespawnOverlay respawnOverlay;

	@Inject
	private EquipmentOverlay equipmentOverlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Notifier notifier;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	public OkHttpClient okHttpClient;

	@Getter(AccessLevel.PACKAGE)
	public Instant lastTickUpdate;

	final DateTimeFormatter formatter = DateTimeFormatter
		.ofPattern("yyyyMMdd")
		.withZone(ZoneOffset.UTC);

	private static final Multimap<String, String> NPC_DISAMBIGUATION_MAP = ImmutableMultimap.of(
		"Dusk", "Grotesque Guardians"
	);

	private String profileKey;
	private final Map<Integer, DropTableSection[]> loadedDropTableSections = new HashMap<>();

	@Getter(AccessLevel.PUBLIC)
	public final Map<Integer, BossBuddyNPC> bossBuddyNPCs = new HashMap<>();

	private List<String> showNPCs = new ArrayList<>();
	private List<String> showNPCsWithTypes = new ArrayList<>();
	private List<String> showNPCIDs = new ArrayList<>();
	private List<String> showNPCBlacklist = new ArrayList<>();

	private boolean showAllHP = true;

	private final List<NPC> spawnedNpcsThisTick = new ArrayList<>();
	private final List<NPC> despawnedNpcsThisTick = new ArrayList<>();

	private WorldPoint lastPlayerLocation;
	private final HashMap<Integer, WorldPoint> npcLocations = new HashMap<>();

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private BossBuddyConfig config;

	private BossBuddyPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp()
	{

		profileKey = null;

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			isUserLoggedIn = true;
		}

		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		String profileKey = configManager.getRSProfileKey();
		if (profileKey != null)
		{
			switchProfile(profileKey);
		}
		panel = new BossBuddyPanel(this, config, configManager, gson, clientThread, profileKey);

		navButton =
			NavigationButton.builder()
				.tooltip(Constants.PLUGIN_NAME)
				.icon(Icons.navImg)
				.priority(Constants.DEFAULT_PRIORITY)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(hpOverlay);
		overlayManager.add(respawnOverlay);
		overlayManager.add(equipmentOverlay);
		showNPCs = getSelectedNpcNames(false);
		showNPCsWithTypes = getSelectedNpcNames(true);
		showNPCIDs = getSelectedNpcIds();

		this.showAllHP = config.npcShowAllHP();
		showNPCBlacklist = getShowAllBlacklistNames();

		clientThread.invokeLater(this::rebuildAllNpcs);
	}


	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			bossBuddyNPCs.clear();
			spawnedNpcsThisTick.clear();
			despawnedNpcsThisTick.clear();
		});
		overlayManager.remove(hpOverlay);
		overlayManager.remove(respawnOverlay);
		npcLocations.clear();
		bossBuddyNPCs.clear();
		clientToolbar.removeNavigation(navButton);
		scheduledExecutorService.shutdown();
	}

	private void switchProfile(String profileKey)
	{
		executor.execute(() ->
		{
			this.profileKey = profileKey;

			log.debug("Switched to profile {}", profileKey);

			clientThread.invokeLater(() ->
			{
				if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
				{
					return false;
				}

				SwingUtilities.invokeLater(() ->
				{
					panel.profileKey = this.profileKey;
				});

				return true;
			});
		});
	}

	public void buildPanelItems(ConfigLoot lootConfig)
	{
		BossDropItem[] bdi = buildBossDropItemsFromConfig(lootConfig);
		panel.refreshMainPanelWithRecords(lootConfig.getName(), bdi, lootConfig.getKills());
	}

	private BossDropItem[] buildBossDropItemsFromConfig(ConfigLoot configLoot)
	{
		int dropsCount = configLoot.numDrops();
		int[] drops = configLoot.getDrops();

		BossDropItem[] items = new BossDropItem[dropsCount];
		for (int i = 0; i < dropsCount * 4; i += 4)
		{
			int id = drops[i];
			int killCount = drops[i + 1];
			int gePrice = drops[i + 2];
			int date = drops[i + 3];
			items[i / 4] = buildBossDropItem(id, 0, killCount, gePrice, date);
		}
		return items;
	}

	private BossDropItem buildBossDropItem(int itemId, int quantity, int killCount, int gePrice, int date)
	{
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		if (gePrice == -1)
		{
			gePrice = itemManager.getItemPrice(itemId);
		}

		boolean tradeable = itemComposition.isTradeable();
		int isNoted = itemComposition.getNote();

		if (isNoted == 799)
		{
			int noteId = itemComposition.getLinkedNoteId();
			if (noteId != -1)
			{
				ItemComposition notedItem = itemManager.getItemComposition(noteId);
				tradeable = notedItem.isTradeable();
			}
		}
		if (Objects.equals(itemComposition.getName(), "Coins"))
		{
			tradeable = true;
		}

		return new BossDropItem(
			itemId,
			itemComposition.getMembersName(),
			quantity,
			killCount,
			date,
			gePrice,
			tradeable,
			itemManager.getImage(itemId));
	}

	public void setLootConfig(String name, ConfigLoot loot)
	{
		String profile = profileKey;
		if (Strings.isNullOrEmpty(profile))
		{
			log.debug("Trying to set loot with no profile!");
			return;
		}

		String json = gson.toJson(loot);
		configManager.setConfiguration(BossBuddyConfig.GROUP, profile, "BOSS_BUDDY_NPC_" + name.toUpperCase(), json);
	}

	ConfigLoot getLootConfig(String name)
	{
		String profile = profileKey;
		if (Strings.isNullOrEmpty(profile))
		{
			log.debug("Trying to get loot with no profile!");
			return null;
		}

		String json = configManager.getConfiguration(BossBuddyConfig.GROUP, profile, "BOSS_BUDDY_NPC_" + name.toUpperCase());
		if (json == null)
		{
			return null;
		}

		return gson.fromJson(json, ConfigLoot.class);
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged e)
	{
		final String profileKey = configManager.getRSProfileKey();
		if (profileKey == null)
		{
			return;
		}

		if (profileKey.equals(this.profileKey))
		{
			return;
		}

		switchProfile(profileKey);
	}

	@Subscribe
	public void onMenuOpened(final MenuOpened event)
	{
		if (!config.coinSplit())
		{
			return;
		}

		final MenuEntry[] entries = event.getMenuEntries();
		for (int idx = entries.length - 1; idx >= 0; --idx)
		{
			final MenuEntry entry = entries[idx];
			final Widget w = entry.getWidget();

			if (w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY
				&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10)
			{

				final int itemId = w.getItemId();
				final int itemCount = w.getItemQuantity();
				if (itemId == COINS)
				{

					MenuEntry splitCoins = entry
						.setOption("Split")
						.setTarget(entry.getTarget())
						.setType(MenuAction.RUNELITE);

					Menu subLeft = splitCoins.createSubMenu();

					subLeft.createMenuEntry(-1)
						.setOption("Split 2")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> splitCoins(itemCount, 2));

					subLeft.createMenuEntry(-1)
						.setOption("Split 3")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> splitCoins(itemCount, 3));

					subLeft.createMenuEntry(-1)
						.setOption("Split 4")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> splitCoins(itemCount, 4));

					subLeft.createMenuEntry(-1)
						.setOption("Split 5")
						.setType(MenuAction.RUNELITE)
						.onClick(e -> splitCoins(itemCount, 5));

				}
			}
		}
	}

	private void splitCoins(int quantity, int splitAmount)
	{
		DecimalFormat formatter = new DecimalFormat("#,###");
		log.info(String.valueOf(quantity / splitAmount));

		String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(String.format("Split: %s / %d = %s", formatter.format(quantity), splitAmount, formatter.format(quantity / splitAmount)))
			.build();

		chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());

	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN ||
			gameStateChanged.getGameState() == GameState.HOPPING)
		{
			bossBuddyNPCs.forEach((id, npc) -> npc.setDiedOnTick(-1));
			lastPlayerLocation = null;
			skipNextSpawnCheck = true;
		}
	}


	private static Collection<ItemStack> stack(Collection<ItemStack> items)
	{
		final List<ItemStack> list = new ArrayList<>();

		for (final ItemStack item : items)
		{
			int quantity = 0;
			for (final ItemStack i : list)
			{
				if (i.getId() == item.getId())
				{
					quantity = i.getQuantity();
					list.remove(i);
					break;
				}
			}
			if (quantity > 0)
			{
				list.add(new ItemStack(item.getId(), item.getQuantity() + quantity));
			}
			else
			{
				list.add(item);
			}
		}

		return list;
	}


	@Provides
	BossBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BossBuddyConfig.class);
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		Actor opponent = event.getTarget();

		if (opponent == null)
		{
			lastTime = Instant.now();
			return;
		}

		lastOpponent = opponent;
		List<NPC> matchingNPC = worldView.npcs().stream().filter(npc -> Objects.equals(npc.getName(), lastOpponent.getName()) && npc.getCombatLevel() == lastOpponent.getCombatLevel()).collect(Collectors.toList());
		for (NPC curNPC : matchingNPC)
		{
			lastNPC = curNPC;
			if (!loadedDropTableSections.isEmpty() && loadedDropTableSections.containsKey(curNPC.getId()))
			{
				log.debug("Drop table already loaded");
			}
			else
			{
				WikiScraper.getDropsByMonster(okHttpClient, curNPC.getName(), curNPC.getId()).whenCompleteAsync((dropTableSections, ex) -> {
					DecimalFormat df = new DecimalFormat("#.####");
					df.setRoundingMode(RoundingMode.CEILING);

					loadedDropTableSections.put(curNPC.getId(), dropTableSections);
				});
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		validateSpawnedNpcs();
		lastTickUpdate = Instant.now();
		lastPlayerLocation = client.getLocalPlayer().getWorldLocation();

		if (lastOpponent != null
			&& lastTime != null
			&& client.getLocalPlayer().getInteracting() == null)
		{
			if (Duration.between(lastTime, Instant.now()).compareTo(WAIT) > 0)
			{
				lastOpponent = null;
				lastNPC = null;
			}
		}

		if (!config.showOverlay())
		{
			return;
		}

		HashMap<WorldPoint, Integer> locationCount = new HashMap<>();
		for (WorldPoint location : npcLocations.values())
		{
			locationCount.put(location, locationCount.getOrDefault(location, 0) + 1);
		}

		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (!isNpcInList(npc))
			{
				continue;
			}

			if (npc.getCombatLevel() <= 0)
			{
				continue;
			}

			BossBuddyNPC bossBuddyNPC = bossBuddyNPCs.get(npc.getIndex());

			if (bossBuddyNPC != null)
			{
				updateBossBuddyNPCProperties(npc, bossBuddyNPC, locationCount);
			}
		}

		List<Integer> npcToRemove = new ArrayList<>();
		for (BossBuddyNPC bossBuddyNPC : this.bossBuddyNPCs.values())
		{
			final Instant now = Instant.now();
			final double baseTick = (bossBuddyNPC.getRespawnTime() - (client.getTickCount() - bossBuddyNPC.getDiedOnTick())) * (net.runelite.api.Constants.GAME_TICK_LENGTH / 1000.0);
			final double sinceLast = (now.toEpochMilli() - this.lastTickUpdate.toEpochMilli()) / 1000.0;
			final double timeLeft = baseTick - sinceLast;

			if (timeLeft <= -60 && !isInViewRange(client.getLocalPlayer().getWorldLocation(), bossBuddyNPC.getNpc().getWorldLocation()))
			{
				npcToRemove.add(bossBuddyNPC.getNpcIndex());
			}
		}

		for (Integer npcIndex : npcToRemove)
		{
			this.bossBuddyNPCs.remove(npcIndex);
		}
	}

	private void updateBossBuddyNPCProperties(NPC npc, BossBuddyNPC bossBuddyNPC, Map<WorldPoint, Integer> locationCount)
	{
		double monsterHp = ((double) npc.getHealthRatio() / (double) npc.getHealthScale() * 100);

		boolean isBoss = Boss.isNpcBoss(npc);
		if (isBoss)
		{
			final int curHp = client.getVarbitValue(HPBAR_HUD_HP);
			final int maxHp = client.getVarbitValue(HPBAR_HUD_BASEHP);
			if (maxHp > 0 && curHp >= 0)
			{
				double hpVarbitRatio = 100.0 * curHp / maxHp;
				if (hpVarbitRatio > 0)
				{
					monsterHp = hpVarbitRatio;
				}
			}
			else
			{
				return;
			}
		}
		boolean isDoomBoss = Boss.DOOM_BOSS_IDS.contains(npc.getId());

		if (!npc.isDead() && ((npc.getHealthRatio() / npc.getHealthScale() != 1) || isDoomBoss))
		{
			bossBuddyNPC.setHealthRatio(monsterHp);
			bossBuddyNPC.setCurrentLocation(npc.getWorldLocation());
			bossBuddyNPC.setDead(false);

			WorldPoint currentLocation = bossBuddyNPC.getCurrentLocation();

			if (locationCount.containsKey(currentLocation))
			{
				bossBuddyNPC.setOffset(locationCount.get(currentLocation) - 1);
				locationCount.put(currentLocation, locationCount.get(currentLocation) - 1);
			}
		}
		else if (npc.isDead())
		{
			bossBuddyNPC.setHealthRatio(0);
			bossBuddyNPC.setDead(true);
		}

		npcLocations.put(bossBuddyNPC.getNpcIndex(), bossBuddyNPC.getCurrentLocation());
	}

	private void checkRates(BossDropItem item, NPCComposition npc)
	{
		int npcId = npc.getId();
		String npcName = npc.getName();

		if (NPC_DISAMBIGUATION_MAP.containsKey(npcName))
		{
			Collection<String> npcMap = NPC_DISAMBIGUATION_MAP.get(npcName);
			npcName = npcMap.stream().findFirst().orElse("None");
			npcId = -1;
		}

		if (!loadedDropTableSections.isEmpty() && loadedDropTableSections.containsKey(npcId))
		{
			log.debug("Drop Table already loaded");
		}
		else
		{
			int finalNpcId = npcId;
			WikiScraper.getDropsByMonster(okHttpClient, npcName, npcId).whenCompleteAsync((dropTableSections, ex) -> {
				DecimalFormat df = new DecimalFormat("#.####");
				df.setRoundingMode(RoundingMode.CEILING);

				loadedDropTableSections.put(finalNpcId, dropTableSections);
			});
		}

		DropTableSection[] dropTableSections = loadedDropTableSections.get(npcId);
		if (dropTableSections == null)
		{
			return;
		}

		boolean foundDrop = false;
		Map<String, Integer> commonDrops = new HashMap<>();
		for (DropTableSection section : dropTableSections)
		{
			Map<String, WikiItem[]> dropTables = section.getTable();

			for (Map.Entry<String, WikiItem[]> wikiEntry : dropTables.entrySet())
			{

				WikiItem[] wikiItems = wikiEntry.getValue();

				String dropItemName = item.getName();
				int dropItemQuant = item.getQuantity();

				if (commonDrops.containsKey(dropItemName) && commonDrops.get(dropItemName) == dropItemQuant)
				{
					log.info("Most likely rare drop that is under report rate");
					continue;
				}

				WikiItem wikiItem = Arrays.stream(wikiItems).filter(k -> Objects.equals(k.getName(), dropItemName) && k.quantityMatch(dropItemQuant)).findFirst().orElse(null);
				if (wikiItem != null)
				{

					String itemRarity = wikiItem.getRarityLabelText(false);
					if (Objects.equals(itemRarity, "Always") || Objects.equals(wikiItem.getName(), "Coins"))
					{
						continue;
					}

					if (itemRarity.contains(";"))
					{
						itemRarity = Arrays.stream(itemRarity.split(";")).findFirst().get();
					}

					BossDropFraction itemRarityFraction = BossDropFraction.parseFraction(itemRarity);
					BossDropFraction rarityLimitFraction = BossDropFraction.parseFraction(config.dropRarityLimit());
					int fractionCompare = itemRarityFraction.compareTo(rarityLimitFraction);

					if (fractionCompare > 0)
					{
						commonDrops.put(dropItemName, dropItemQuant);
						continue;
					}

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String formattedDate = sdf.format(new Date());

					String chatMessageString = String.format("Item: %s - Rarity: %s", item.getName(), itemRarity);
					if (config.displayDate())
					{
						chatMessageString = String.format("%s - Date: %s", chatMessageString, formattedDate);
					}

					String chatMessage = new ChatMessageBuilder()
						.append(ChatColorType.HIGHLIGHT)
						.append(chatMessageString)
						.build();

					chatMessageManager.queue(
						QueuedMessage.builder()
							.type(ChatMessageType.CONSOLE)
							.runeLiteFormattedMessage(chatMessage)
							.build());
					foundDrop = true;
					break;
				}
				if (foundDrop)
				{
					break;
				}
			}
		}
	}

	@Subscribe
	public void onServerNpcLoot(final ServerNpcLoot event)
	{
		final NPCComposition npc = event.getComposition();
		final Collection<ItemStack> items = event.getItems();
		String name = npc.getName();

		if (NPC_DISAMBIGUATION_MAP.containsKey(name))
		{
			Collection<String> npcMap = NPC_DISAMBIGUATION_MAP.get(name);
			name = npcMap.stream().findFirst().orElse("None");
		}

		int killCount = getKc(name);
		addLoot(npc, items, killCount);
	}

	private int getKc(String npcName)
	{
		Integer killCount = configManager.getRSProfileConfiguration("killcount", npcName.toLowerCase(), int.class);
		if (killCount == null)
		{
			String bossBuddyJson = configManager.getConfiguration(BossBuddyConfig.GROUP, profileKey, "BOSS_BUDDY_NPC_" + npcName.toUpperCase());
			String lootTrackerJson = configManager.getConfiguration(BossBuddyConfig.LOOT_TRACKER_GROUP, profileKey, "drops_NPC_" + npcName);

			if (bossBuddyJson == null && lootTrackerJson != null)
			{
				ConfigLoot savedConfig = gson.fromJson(lootTrackerJson, ConfigLoot.class);
				killCount = savedConfig.kills;
			}
			else if (bossBuddyJson != null && lootTrackerJson != null)
			{
				ConfigLoot bbConfig = gson.fromJson(bossBuddyJson, ConfigLoot.class);
				ConfigLoot ltConfig = gson.fromJson(lootTrackerJson, ConfigLoot.class);

				if (ltConfig.kills > bbConfig.kills)
				{
					bbConfig.kills = ltConfig.kills;
				}
				killCount = bbConfig.kills;

			}
			else if (bossBuddyJson != null)
			{
				ConfigLoot savedConfig = gson.fromJson(bossBuddyJson, ConfigLoot.class);
				killCount = savedConfig.kills;
			}
			else
			{
				killCount = 0;
			}
		}
		return killCount + 1;
	}

	void addLoot(NPCComposition npc, Collection<ItemStack> items, int killCount)
	{
		int intdate = Integer.parseInt(formatter.format(Instant.now()));
		final BossDropItem[] entries = buildEntries(stack(items), killCount, intdate);
		for (BossDropItem bdi : entries)
		{
			if (Objects.equals(bdi.getName(), "Dwarf remains") || Arrays.stream(config.ignoreItems().split(",")).anyMatch(k -> k.equals(bdi.getName())))
			{
				continue;
			}

			checkRates(bdi, npc);
			ConfigLoot lootConfig = getLootConfig(npc.getName());
			if (lootConfig == null)
			{
				lootConfig = new ConfigLoot(npc.getName());
			}

			lootConfig.kills = killCount;

			if (bdi.getGePrice() * bdi.getQuantity() >= config.trackBossDropValue() || !bdi.isTradeable())
			{
				lootConfig.lastDropKC = bdi.getKillCount();
				lootConfig.lastDrop = bdi.getName();
				lootConfig.add(bdi.getId(), bdi.getKillCount(), bdi.getGePrice() * bdi.getQuantity(), bdi.getDate());
				lootConfig.last = Instant.now();
			}

			setLootConfig(lootConfig.name, lootConfig);
			buildPanelItems(lootConfig);
		}
	}


	private BossDropItem[] buildEntries(final Collection<ItemStack> itemStacks, int killCount, int date)
	{
		return itemStacks.stream()
			.map(itemStack -> buildBossDropItem(itemStack.getId(), itemStack.getQuantity(), killCount, -1, date))
			.toArray(BossDropItem[]::new);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		final NPC npc = npcSpawned.getNpc();
		final String npcName = npc.getName();
		if (!isNpcInList(npc))
		{
			return;
		}

		if (npcName == null)
		{
			return;
		}
		if (npc.getCombatLevel() <= 0)
		{
			return;
		}

		BossBuddyNPC previousNPC = bossBuddyNPCs.get(npc.getIndex());
		BossBuddyNPC bossBuddyNPC = new BossBuddyNPC(npc);

		if (previousNPC != null)
		{
			bossBuddyNPC.setDiedOnTick(previousNPC.getDiedOnTick());

		}

		if (isNpcNumericDefined(npc))
		{
			bossBuddyNPC.setIsTypeNumeric(1);
		}

		bossBuddyNPC.setDead(false);
		spawnedNpcsThisTick.add(npc);
		bossBuddyNPCs.put(npc.getIndex(), bossBuddyNPC);
		npcLocations.put(npc.getIndex(), npc.getWorldLocation());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		if (npc == null || !isNpcInList(npc))
		{
			return;
		}
		Actor actor = npcDespawned.getActor();

		if (bossBuddyNPCs.containsKey(npc.getIndex()))
		{
			createTimer(npc);
		}

		npcLocations.remove(npc.getIndex());
	}

	@Subscribe
	public void onNpcChanged(NpcChanged e)
	{
		final NPC npc = e.getNpc();
		int id = npc.getId();
		int idx = npc.getIndex();

		if (id == DUKE_SUCELLUS_DEAD || id == DUKE_SUCELLUS_DEAD_QUEST)
		{
			bossBuddyNPCs.remove(idx);
			npcLocations.remove(idx);
		}

		if (id == 412)
		{
			return;
		}

		if (isNpcInList(npc))
		{
			BossBuddyNPC previousNPC = bossBuddyNPCs.get(npc.getIndex());
			BossBuddyNPC bossBuddyNPC = new BossBuddyNPC(npc);

			if (previousNPC != null)
			{
				bossBuddyNPC.setDiedOnTick(previousNPC.getDiedOnTick());
			}

			if (isNpcNumericDefined(npc))
			{
				bossBuddyNPC.setIsTypeNumeric(1);
			}

			bossBuddyNPCs.put(idx, bossBuddyNPC);
			npcLocations.put(idx, npc.getWorldLocation());
		}
		else
		{
			bossBuddyNPCs.remove(idx);
			npcLocations.remove(idx);
		}
	}

	private void createTimer(NPC npc)
	{
		RespawnTimer timer = null;
		BossBuddyNPC bossBuddyNPC = bossBuddyNPCs.get(npc.getIndex());
		bossBuddyNPC.setDead(true);
		despawnedNpcsThisTick.add(npc);
		clearTimer(bossBuddyNPC);

		if (Boss.isNpcBoss(npc) && config.bossRespawnTimer())
		{
			Boss boss = Boss.find(npc.getId());
			if (boss != null && !boss.isIgnoreAlarm())
			{
				//log.info("Creating boss spawn timer for {} ({})", bossBuddyNPC.getNpcName(), boss.getSpawnTime());
				timer = new RespawnTimer(bossBuddyNPC, boss, itemManager.getImage(boss.getItemSpriteId()), this);

			}
		}
		else
		{
			if (bossBuddyNPC.getRespawnTime() > -1 && config.npcRespawnTimer())
			{
				//log.info("Creating spawn timer for {} ({})", npc.getName(), bossBuddyNPC.getRespawnTime());
				timer = new RespawnTimer(bossBuddyNPC, itemManager.getImage(ItemID.SKULL), this);
				timer.setTooltip(npc.getName());
			}
		}

		if (timer != null)
		{
			timer.setTooltip(bossBuddyNPC.getNpcName());
			infoBoxManager.addInfoBox(timer);
			createRespawnNotification(timer, bossBuddyNPC.getNpcName(), Boss.isNpcBoss(npc));
		}
	}

	private void clearTimer(BossBuddyNPC bossBuddyNPC)
	{
		infoBoxManager.removeIf(t -> t instanceof RespawnTimer && ((RespawnTimer) t).getBossBuddyNPC() == bossBuddyNPC);
	}

	private void createRespawnNotification(RespawnTimer timer, String npcName, boolean isBoss)
	{
		RespawnNotification respawnNotification = new RespawnNotification(npcName, notifier);
		boolean createNotification = isBoss && config.bossRespawnNotification();

		if (!isBoss && config.npcRespawnNotification())
		{
			createNotification = true;
		}

		if (createNotification)
		{
			Timer t = new java.util.Timer();
			t.schedule(
				new java.util.TimerTask()
				{
					@Override
					public void run()
					{
						try
						{
							respawnNotification.call();
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
						t.cancel();
					}
				},
				timer.getDuration().minusSeconds(5).toMillis()
			);
		}
	}

	private void validateSpawnedNpcs()
	{
		if (skipNextSpawnCheck)
		{
			skipNextSpawnCheck = false;
		}
		else
		{
			for (NPC npc : despawnedNpcsThisTick)
			{
				if (isInViewRange(client.getLocalPlayer().getWorldLocation(), npc.getWorldLocation()))
				{
					BossBuddyNPC mn = bossBuddyNPCs.get(npc.getIndex());

					if (mn != null)
					{
						int tickDied = client.getTickCount() + 1;
						mn.setDiedOnTick(tickDied); // This runs before tickCounter updates, so we add 1
					}
				}
			}

			for (NPC npc : spawnedNpcsThisTick)
			{
				if (lastPlayerLocation != null && isInViewRange(lastPlayerLocation, npc.getWorldLocation()))
				{
					BossBuddyNPC mn = bossBuddyNPCs.get(npc.getIndex());

					if (mn.getDiedOnTick() != -1)
					{
						final int respawnTime = client.getTickCount() + 1 - mn.getDiedOnTick();

						if ((mn.getRespawnTime() == -1 || respawnTime < mn.getRespawnTime()) && respawnTime <= MAX_RESPAWN_TIME_TICKS)
						{
							mn.setRespawnTime(respawnTime);
						}

						mn.setDiedOnTick(-1);
					}

					final WorldPoint npcLocation = npc.getWorldLocation();
					final WorldPoint possibleOtherNpcLocation = getWorldLocationBehind(npc);

					mn.getPossibleRespawnLocations().removeIf(x ->
						!x.equals(npcLocation) && !x.equals(possibleOtherNpcLocation));

					if (mn.getPossibleRespawnLocations().isEmpty())
					{
						mn.getPossibleRespawnLocations().add(npcLocation);
					}
				}
			}
		}

		spawnedNpcsThisTick.clear();
		despawnedNpcsThisTick.clear();
	}

	private static boolean isInViewRange(WorldPoint wp1, WorldPoint wp2)
	{
		int distance = wp1.distanceTo(wp2);
		return distance < MAX_ACTOR_VIEW_RANGE;
	}

	private static WorldPoint getWorldLocationBehind(NPC npc)
	{
		final int orientation = npc.getOrientation() / 256;
		int dx = 0, dy = 0;

		switch (orientation)
		{
			case 0: // South
				dy = -1;
				break;
			case 1: // Southwest
				dx = -1;
				dy = -1;
				break;
			case 2: // West
				dx = -1;
				break;
			case 3: // Northwest
				dx = -1;
				dy = 1;
				break;
			case 4: // North
				dy = 1;
				break;
			case 5: // Northeast
				dx = 1;
				dy = 1;
				break;
			case 6: // East
				dx = 1;
				break;
			case 7: // Southeast
				dx = 1;
				dy = -1;
				break;
		}

		final WorldPoint currWP = npc.getWorldLocation();
		return new WorldPoint(currWP.getX() - dx, currWP.getY() - dy, currWP.getPlane());
	}

	public void removeLoot(String monsterName, int indexToRemove)
	{
		ConfigLoot lootConfig = getLootConfig(monsterName);
		int[] newArray = new int[lootConfig.getDrops().length - 4];
		int newArrayIndex = 0;
		for (int i = 0; i < lootConfig.getDrops().length; i++)
		{
			if (i != indexToRemove && i != indexToRemove + 1 && i != indexToRemove + 2 && i != indexToRemove + 3)
			{
				newArray[newArrayIndex++] = lootConfig.getDrops()[i];
			}
		}
		lootConfig.setDrops(newArray);
		setLootConfig(lootConfig.getName(), lootConfig);
		buildPanelItems(lootConfig);

	}

	public void removeLoot(String monsterName, int indexToRemove, boolean removeAll)
	{
		ConfigLoot lootConfig = getLootConfig(monsterName);
		int[] newArray = new int[lootConfig.getDrops().length - 3];
		int newArrayIndex = 0;
		for (int i = 0; i < lootConfig.getDrops().length; i++)
		{
			if (!removeAll)
			{
				if (i != indexToRemove && i != indexToRemove + 1 && i != indexToRemove + 2)
				{
					newArray[newArrayIndex++] = lootConfig.getDrops()[i];
				}
			}
		}
		lootConfig.setDrops(newArray);
		setLootConfig(lootConfig.getName(), lootConfig);
		buildPanelItems(lootConfig);

	}

	private boolean isNpcNameInShowAllBlacklist(String npcName)
	{
		return npcName != null && (showNPCBlacklist.contains(npcName.toLowerCase()) ||
			showNPCBlacklist.stream().anyMatch(pattern -> WildcardMatcher.matches(pattern, npcName)));
	}

	private boolean isNpcNameInList(String npcName)
	{
		return npcName != null && (showNPCs.contains(npcName.toLowerCase()) ||
			showNPCs.stream().anyMatch(pattern -> WildcardMatcher.matches(pattern, npcName)));
	}

	private boolean isNpcIdInList(int npcId)
	{
		return showNPCIDs.contains(String.valueOf(npcId));
	}

	public boolean isNpcIdBlacklisted(NPC npc)
	{
		String npcName = npc.getName();
		if (npcName != null)
		{
			int id = npc.getId();

			switch (npcName)
			{
				case "Duke Sucellus": // duke sucellus - allow only fight id to be tracked from duke
					return id != DUKE_SUCELLUS_AWAKE && id != DUKE_SUCELLUS_ASLEEP;
				case "Vardorvis":
					return id == VARDORVIS_BASE_POSTQUEST; // Vardorvis outside instance.
				case "Akkha":
					return id == AKKHA_SPAWN; // Pre-enter room idle Akkha id 11789
				case "Muttadile":
					// On name tagging this is duplicating dogodile junior's hp to submerged version
					// this is due to name matching, I doubt anybody want submerged version
					return id == RAIDS_DOGODILE_SUBMERGED;
				case "Yama":
					// Blacklist everything but Yama the boss
					return id != YAMA;
				default:
					return false;
			}
		}

		return false;
	}

	private boolean isNpcInList(NPC npc)
	{
		if (isNpcIdBlacklisted(npc))
		{
			return false;
		}

		boolean isInList = (isNpcNameInList(npc.getName()) || isNpcIdInList(npc.getId()));

		if (!isInList)
		{
			return this.showAllHP && !isNpcNameInShowAllBlacklist(npc.getName());
		}

		return true;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (Objects.equals(configChanged.getGroup(), "MonsterHP") && (Objects.equals(configChanged.getKey(), "npcShowAll") || Objects.equals(configChanged.getKey(), "npcShowAllBlacklist") || Objects.equals(configChanged.getKey(), "npcToShowHp") || Objects.equals(configChanged.getKey(), "npcIdToShowHp")))
		{
			showNPCs = getSelectedNpcNames(false);
			showNPCIDs = getSelectedNpcIds();

			this.showAllHP = config.npcShowAllHP();
			showNPCBlacklist = getShowAllBlacklistNames();

			clientThread.invokeLater(this::rebuildAllNpcs);
		}


	}

	List<String> getShowAllBlacklistNames()
	{
		String configNPCs = config.npcShowAllBlacklist().toLowerCase();
		return configNPCs.isEmpty() ? Collections.emptyList() : Text.fromCSV(configNPCs);
	}

	@VisibleForTesting
	List<String> getSelectedNpcNames(boolean includeDisplaytype)
	{
		String configNPCs = config.npcNameShowHP().toLowerCase();
		if (configNPCs.isEmpty())
		{
			return Collections.emptyList();
		}

		List<String> selectedNpcNamesRaw = Text.fromCSV(configNPCs);

		if (!includeDisplaytype)
		{
			List<String> strippedNpcNames = new ArrayList<>(selectedNpcNamesRaw);
			strippedNpcNames.replaceAll(npcName -> npcName != null && npcName.contains(":") ? npcName.split(":")[0] : npcName);

			return strippedNpcNames;
		}

		return selectedNpcNamesRaw;
	}

	@VisibleForTesting
	List<String> getSelectedNpcIds()
	{
		String configNPCIDs = config.npcIDShowHP().toLowerCase();
		if (configNPCIDs.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configNPCIDs);
	}

	@VisibleForTesting
	boolean isNpcNumericDefined(NPC npc)
	{
		String npcNameTargetLowerCase = Objects.requireNonNull(npc.getName()).toLowerCase();

		for (String npcNameRaw : showNPCsWithTypes)
		{
			String npcName = npcNameRaw.contains(":") ? npcNameRaw.split(":")[0] : npcNameRaw;
			boolean isMatch = WildcardMatcher.matches(npcName, npcNameTargetLowerCase);

			if (npcNameRaw.contains(":n") && isMatch)
			{
				return true;
			}
		}
		return false;
	}

	private void rebuildAllNpcs()
	{
		bossBuddyNPCs.clear();

		if (client.getGameState() != GameState.LOGGED_IN &&
			client.getGameState() != GameState.LOADING)
		{
			return;
		}

		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (isNpcInList(npc))
			{
				BossBuddyNPC bossBuddyNPC = new BossBuddyNPC(npc);

				if (isNpcNumericDefined(npc))
				{
					bossBuddyNPC.setIsTypeNumeric(1);
				}

				bossBuddyNPCs.put(npc.getIndex(), bossBuddyNPC);
				npcLocations.put(npc.getIndex(), npc.getWorldLocation());
			}
		}
	}
}
