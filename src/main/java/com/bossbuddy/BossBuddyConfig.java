package com.bossbuddy;

import com.bossbuddy.views.PriceType;
import com.bossbuddy.views.ViewOption;
import net.runelite.client.config.*;
import com.bossbuddy.util.Constants;

import java.awt.*;

import static com.bossbuddy.util.Constants.*;

@ConfigGroup("Boss Buddy")
public interface BossBuddyConfig extends Config
{
	String GROUP = "bossbuddy";



	@ConfigItem(
			keyName = "coinSplit",
			name = "Coin Split",
			description = "Enable Coin menu option for splitting",
			position = 0
	)
	default boolean coinSplit()
	{
		return true;
	}

	@ConfigItem(
			keyName = "wornItems",
			name = "Worn items",
			description = "Enable display of worn items while banking",
			position = 0
	)
	default boolean wornItems()
	{
		return true;
	}


	@ConfigSection(
			name = "Loot Settings",
			description = "Settings relating to Loot",
			position = 1
	)
	String loot_settings = "loot_settings";

	@ConfigSection(
			name = "Notification Settings",
			description = "Settings relating to Notifications",
			position = 2
	)
	String notification_settings = "notification_settings";

	@ConfigSection(
			name = "HP Settings",
			description = "Settings relating to Monster HP",
			position = 3
	)
	String hp_settings = "hp_settings";

	@ConfigItem(
			keyName = "dropRarityLimit",
			name = "Drop Rarity",
			description = "Show rarity message for items greater than this",
			position = 0,
			section = loot_settings
	)
	default String dropRarityLimit()
	{
		return "1/128";
	}

	@ConfigItem(
			keyName = "trackBossDropValue",
			name = "Item Value",
			description = "Track kill count for items with GE value greater than this",
			position = 1,
			section = loot_settings
	)
	default int trackBossDropValue()
	{
		return 100000;
	}

	@ConfigItem(
			position = 2,
			keyName = "ignoreItems",
			name = "Ignore Items",
			description = "Items to ignore. Comma Separated.",
			section = loot_settings

	)
	default String ignoreItems() {
		return "";
	}

	@ConfigItem(
			position = 3,
			keyName = "displayDate",
			name = "Display Date",
			description = "Display date along side rarity message",
			section = loot_settings

	)
	default boolean displayDate() {
		return false;
	}



	@ConfigItem(
			keyName = "bossRespawnTimer",
			name = "Boss Respawn Timer",
			description = "Enable respawn notifications for bosses",
			position = 0,
			section = notification_settings
	)
	default boolean bossRespawnTimer()
	{
		return true;
	}

	@ConfigItem(
			keyName = "bossRespawnNotification",
			name = "Boss Respawn Notification",
			description = "Enable Boss respawn notification sound",
			position = 1,
			section = notification_settings
	)
	default boolean bossRespawnNotification()
	{
		return true;
	}

	@ConfigItem(
			keyName = "npcRespawnTimer",
			name = "NPC Respawn Timer",
			description = "Enable respawn notifications for normal NPCs",
			position = 2,
			section = notification_settings
	)
	default boolean npcRespawnTimer()
	{
		return false;
	}

	@ConfigItem(
			keyName = "npcRespawnNotification",
			name = "NPC Respawn Notification",
			description = "Enable NPC respawn notification sound",
			position = 3,
			section = notification_settings
	)
	default boolean npcRespawnNotification()
	{
		return true;
	}

	@ConfigItem(
			keyName = "npcRespawnLocation",
			name = "NPC Respawn Location",
			description = "Enable NPC respawn location tile",
			position = 4,
			section = notification_settings
	)
	default boolean npcRespawnLocation()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "showOverlay",
			name = "Show NPC HP overlay",
			description = "Turn on/off HP overlay",
			section = hp_settings
	)
	default boolean showOverlay() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "aboveHP",
			name = "Display above HP",
			description = "Default position above enemy HP bar",
			section = hp_settings
	)
	default boolean aboveHP() {
		return false;
	}

	@Range(
			min = 0,
			max = 200
	)
	@ConfigItem(
			position = 1,
			keyName = "overlayUp",
			name = "Move Up",
			description = "Change vertical placement of HP Overlay",
			section = hp_settings
	)
	default int overlayUp() {
		return 0;
	}
	@Range(
			min = 0,
			max = 200
	)
	@ConfigItem(
			position = 2,
			keyName = "overlayDown",
			name = "Move Down",
			description = "Change vertical placement of HP Overlay",
			section = hp_settings
	)
	default int overlayDown() {
		return 0;
	}

	@Range(
			min = 0,
			max = 200
	)
	@ConfigItem(
			position = 3,
			keyName = "overlayRight",
			name = "Move Right",
			description = "Change horizontal placement of HP Overlay",
			section = hp_settings
	)
	default int overlayRight() {
		return 0;
	}

	@Range(
			min = 0,
			max = 200
	)
	@ConfigItem(
			position = 4,
			keyName = "overlayLeft",
			name = "Move Left",
			description = "Change horizontal placement of HP Overlay",
			section = hp_settings
	)
	default int overlayLeft() {
		return 0;
	}

	@ConfigItem(
			position = 5,
			keyName = "fontSize",
			name = "Overlay Size",
			description = "Size of the font to use for HP Overlay ",
			section = hp_settings
	)
	default int fontSize() {
		return 15;
	}

	@Range(
			min = 0,
			max = 2
	)
	@ConfigItem(
			position = 6,
			keyName = "decimalHp",
			name = "Decimal Points ",
			description = "Show 0-2 decimals of precision.",
			section = hp_settings
	)
	default int decimalHp() {
		return 0;
	}

	@ConfigItem(
			keyName = "hpPercentage",
			name = "HP Percentage",
			description = "Enable HP Percentage",
			position = 7,
			section = hp_settings
	)
	default boolean hpPercentage()
	{
		return false;
	}



	@ConfigItem(
			keyName = "hpValue",
			name = "HP Value",
			description = "Enable HP Value",
			position = 8,
			section = hp_settings
	)
	default boolean hpValue()
	{
		return false;
	}

	@ConfigItem(
			keyName = "stackOverlay",
			name = "Stack Value/Percent",
			description = "Stack the Value and Percentage overlays while both are displayed",
			position = 9,
			section = hp_settings
	)
	default boolean stackOverlay()
	{
		return false;
	}


	@ConfigItem(
			position = 10,
			keyName = "npcHideFull",
			name = "Hide Overlay when full HP",
			description = "Hide HP Overlay when full HP",
			section = hp_settings
	)
	default boolean npcHideFull() {
		return false;
	}

	@ConfigItem(
			position = 11,
			keyName = "npcShowAllHP",
			name = "Show All",
			description = "Show for all NPCs",
			section = hp_settings
	)
	default boolean npcShowAllHP() {
		return false;
	}

	@ConfigItem(
			position = 12,
			keyName = "npcNameShowHP",
			name = "NPC Names",
			description = "Names of NPC to show HP Info. Semi Colon Separated with Optional HP Thresholds. Example: Yama,66,33;Araxxor,25",
			section = hp_settings
	)
	default String npcNameShowHP() {
		return "";
	}

	@ConfigItem(
			position = 13,
			keyName = "npcIDShowHP",
			name = "NPC Ids",
			description = "Id of NPCs to show HP Info. Semi Colon Separated with Optional HP Thresholds. Example: Yama,66,33;Araxxor,25",
			section = hp_settings
	)
	default String npcIDShowHP() {
		return "";
	}


	@ConfigItem(
			position = 14,
			keyName = "npcNoShowHP",
			name = "Show All Blacklist",
			description = "Name of NPCs to hide Info. Comma Separated.",
			section = hp_settings
	)
	default String npcShowAllBlacklist() {
		return "";
	}

	default Color commonColor() {
		return DEFAULT_COMMON_COLOR;
	}

	default Color priceColor() {
		return DEFAULT_PRICE_COLOR;
	}



}
