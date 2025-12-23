package com.bossbuddy.views;

import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.loot.BossDropRecord;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import net.runelite.client.ui.ColorScheme;
import javax.swing.border.MatteBorder;
import java.util.List;

public class TableResultsPanel extends JPanel
{
	private final BossBuddyConfig config;
	private final BossDropItem[] bossDropItems;
	private final BossDropRecord bossDropRecord;
	private final BossBuddyPanel bossBuddyPanel;

	private final List<TableBox> boxes = new ArrayList<>();
	private int selectedTabIndex;

	private final JPanel dropTableContent = new JPanel();

	public TableResultsPanel(BossBuddyPanel bossBuddyPanel, BossBuddyConfig config, BossDropRecord bossDropRecord)
	{
		this.config = config;
		this.bossBuddyPanel = bossBuddyPanel;
		this.bossDropRecord = bossDropRecord;
		this.bossDropItems = bossDropRecord.getItems();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		dropTableContent.setLayout(new BoxLayout(dropTableContent, BoxLayout.Y_AXIS));

		JScrollPane scrollPane = new JScrollPane(this);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(25);

		buildDropTableContent();
		add(dropTableContent);
	}

	void removeRecord(int itemIndex)
	{
		bossBuddyPanel.removeRecord(itemIndex);
	}

	void buildDropTableContent()
	{
		if (bossDropItems.length > 1)
		{
			dropTableContent.add(Box.createRigidArea(new Dimension(0, 5)));

			JPanel labelContainer = new JPanel(new BorderLayout());
			dropTableContent.add(labelContainer);

			dropTableContent.add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel separator = new JPanel();
			separator.setPreferredSize(new Dimension(0, 6));
			separator.setBorder(new MatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR));
			dropTableContent.add(separator);
		}

		String tableHeader = bossDropRecord.getBossName();

		TableBox tableBox = new TableBox(this, config, bossDropRecord, tableHeader);
		boxes.add(tableBox);

		dropTableContent.add(tableBox);
		dropTableContent.add(Box.createRigidArea(new Dimension(0, 5)));
	}
}