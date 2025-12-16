package com.bossbuddy.gearhelper;

import com.bossbuddy.BossBuddyConfig;
import com.google.common.collect.ImmutableSet;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import javax.inject.Inject;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Set;

@Slf4j
public class EquipmentOverlay extends OverlayPanel
{
	private static final int PLACEHOLDER_WIDTH = 36;
	private static final int PLACEHOLDER_WIDTH_150_PERCENT = 57;
	private static final int PLACEHOLDER_HEIGHT = 32;
	private static final ImageComponent PLACEHOLDER_IMAGE = new ImageComponent(new BufferedImage(PLACEHOLDER_WIDTH, PLACEHOLDER_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));
	private final ItemManager itemManager;
	private final Client client;
	private final BossBuddyConfig bossBuddyConfig;

	private static final Set<Integer> DIZANAS_QUIVER_IDS = ImmutableSet.<Integer>builder()
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(28951)))
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(28955)))
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(28902)))
		.build();

	@Inject
	private EquipmentOverlay(Client client, ItemManager itemManager, BossBuddyConfig bossBuddyConfig)
	{
		setPosition(OverlayPosition.BOTTOM_RIGHT);

		panelComponent.setWrap(true);
		panelComponent.setGap(new Point(6, 4));
		panelComponent.setPreferredSize(new Dimension(3 * (Constants.ITEM_SPRITE_WIDTH), 5 * (Constants.ITEM_SPRITE_HEIGHT)));
		panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);

		this.itemManager = itemManager;
		this.client = client;
		this.bossBuddyConfig = bossBuddyConfig;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!this.bossBuddyConfig.wornItems())
		{
			return null;
		}

		Widget bankContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);

		if (bankContainer == null || bankContainer.isSelfHidden())
		{
			return null;
		}

		Map<Integer, Item> equippedItems = new HashMap<>();
		int slot = 0;
		ItemContainer itemContainer = client.getItemContainer(94);
		if (itemContainer != null)
		{
			Item[] containerItems = itemContainer.getItems();
			for (Item it : containerItems)
			{
				Item slotItem = new Item(it.getId(), it.getQuantity());
				equippedItems.put(slot, slotItem);
				slot++;
			}

			final Item cape = itemContainer.getItem(1);
			var isWearingQuiver = cape != null && DIZANAS_QUIVER_IDS.contains(cape.getId());

			if (isWearingQuiver)
			{
				final int quiverAmmoId = client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO);
				final int quiverAmmoCount = client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT);
				final Item quiverAmmo = new Item(quiverAmmoId, quiverAmmoCount);
				equippedItems.put(11, quiverAmmo);
			}
		}

		panelComponent.getChildren().clear();
		buildInventoryOverlay(equippedItems);
		return panelComponent.render(graphics);
	}

	private void buildInventoryOverlay(Map<Integer, Item> equippedItems)
	{
		Map<Integer, Integer> equipmentMap = Map.ofEntries(
			entry(14, 0), //NULL
			entry(0, 1), //Head
			entry(6, 2), //NULL
			entry(1, 3), //Cape
			entry(2, 4), //Amulet
			entry(13, 5), //Ammo
			entry(3, 6), //Weapon
			entry(4, 7), //Body
			entry(5, 8), //Shield
			entry(8, 9), //NULL
			entry(7, 10), //Legs
			entry(11, 11), //Quiver Ammo
			entry(9, 12), //Gloves
			entry(10, 13), //Boots
			entry(12, 14) //Ring
		);

		for (int i = 0; i < equipmentMap.size(); i++)
		{
			panelComponent.getChildren().add(new ImageComponent(new BufferedImage(36, PLACEHOLDER_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR)));
		}

		for (Map.Entry<Integer, Item> itemMap : equippedItems.entrySet())
		{
			Integer itemSlot = itemMap.getKey();
			Item item = itemMap.getValue();
			if (!equipmentMap.containsKey(itemSlot))
			{
				panelComponent.getChildren().add(new ImageComponent(new BufferedImage(PLACEHOLDER_WIDTH_150_PERCENT, PLACEHOLDER_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR)));
			}
			else
			{
				if (item != null && item.getId() != -1 && item.getQuantity() > 0)
				{
					final BufferedImage image = getImage(item);
					if (image != null)
					{
						panelComponent.getChildren().set(equipmentMap.get(itemSlot), new ImageComponent(image));
					}
				}
			}
		}
	}


	private BufferedImage getImage(Item item)
	{
		return itemManager.getImage(item.getId(), item.getQuantity(), item.getQuantity() > 1);
	}
}
