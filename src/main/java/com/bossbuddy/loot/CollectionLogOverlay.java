package com.bossbuddy.loot;

import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.BossBuddyPlugin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class CollectionLogOverlay extends WidgetItemOverlay
{
	@Inject
	private BossBuddyPlugin bossBuddyPlugin;

	@Inject
	private BossBuddyConfig bossBuddyConfig;

	private static final int OVERLAY_TEXT_ALPHA = 200;

	public CollectionLogOverlay() {
		super();

		drawAfterInterface(621);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
		if (bossBuddyConfig.hideCollectionLogKC()) {
			return;
		}

		bossBuddyPlugin.findCollectionLogKC(itemId, bossDropItem -> {
			if(bossDropItem == null){
				return;
			}
			Rectangle r = widgetItem.getCanvasBounds();
			Color luckColor = Color.green;

			String overallLuckText = String.valueOf(bossDropItem.getKillCount()) +"kc";

			graphics.setColor(Color.BLACK);
			graphics.drawString(overallLuckText, r.x + 0.5f, r.y + r.height + 0.5f);

			Color textColor = new Color(luckColor.getRed(), luckColor.getGreen(), luckColor.getBlue(), OVERLAY_TEXT_ALPHA)
				.brighter().brighter();
			graphics.setColor(textColor);

			graphics.drawString(overallLuckText, r.x, r.y + r.height);
		});
	}
}

