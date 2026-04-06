package com.bossbuddy.views;


import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.loot.BossDropRecord;
import com.bossbuddy.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import javax.swing.*;
import java.util.List;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class TableBox extends JPanel
{
	private final BossBuddyConfig config;
	private final TableResultsPanel tableResultsPanel;

	private final BossDropRecord bossDropRecord;
	public BossDropItem[] items;
	private final String fullHeaderStr;
	private String headerStr;

	private final JPanel listViewContainer = new JPanel();
	private final JPanel headerContainer = new JPanel();
	private final JPanel leftHeader = new JPanel();
	private final JPanel removeHeader = new JPanel();
	private final Color HEADER_BG_COLOR = ColorScheme.DARKER_GRAY_COLOR.darker();
	private final List<BossDropItemPanel> itemPanels = new ArrayList<>();

	public TableBox(TableResultsPanel tableResultsPanel, BossBuddyConfig config, BossDropRecord bossDropRecord, String headerStr)
	{
		this.tableResultsPanel = tableResultsPanel;
		this.config = config;
		this.bossDropRecord = bossDropRecord;
		this.items = bossDropRecord.getItems();
		this.fullHeaderStr = headerStr;
		this.headerStr = headerStr;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		buildHeader();
		buildItemsContainer();
	}

	void removeRecord(int itemIndex)
	{
		tableResultsPanel.removeRecord(itemIndex);

		listViewContainer.remove(itemIndex);
	}

	void buildHeader()
	{
		buildLeftHeader();
		buildRemoveHeader();
		buildHeaderContainer();
	}


	void buildLeftHeader()
	{
		int maxHeaderLength = 18;
		if (headerStr.length() > maxHeaderLength)
		{
			headerStr = headerStr.substring(0, maxHeaderLength) + "…"; // Manually truncate the header
		}

		JLabel headerLabel = new JLabel(headerStr);
		headerLabel.setFont(FontManager.getRunescapeBoldFont());
		headerLabel.setForeground(ColorScheme.BRAND_ORANGE);
		headerLabel.setMinimumSize(new Dimension(1, headerLabel.getPreferredSize().height));

		JLabel headerKillLabel = new JLabel("x " + bossDropRecord.getKills());
		headerKillLabel.setFont(FontManager.getRunescapeSmallFont());
		headerKillLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		JLabel headerPriceLabel = new JLabel(bossDropRecord.GEPriceTotalFormatted());
		headerPriceLabel.setFont(FontManager.getRunescapeSmallFont());
		headerPriceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.X_AXIS));
		leftHeader.setBackground(HEADER_BG_COLOR);
		leftHeader.setBorder(new EmptyBorder(7, 7, 7, 7));

		leftHeader.add(Box.createRigidArea(new Dimension(10, 0)));
		leftHeader.add(headerLabel);

		leftHeader.add(Box.createRigidArea(new Dimension(5, 0)));
		leftHeader.add(headerKillLabel);
		leftHeader.add(Box.createHorizontalGlue());
		leftHeader.add(Box.createRigidArea(new Dimension(5, 0)));

		leftHeader.add(headerPriceLabel);

	}

	void buildRemoveHeader()
	{
		JButton deleteBtn = new JButton();
		SwingUtil.removeButtonDecorations(deleteBtn);
		deleteBtn.setText("Clear Tracker");
		deleteBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		deleteBtn.setUI(new BasicButtonUI());
		deleteBtn.setToolTipText("Remove all loot records");
		deleteBtn.setPreferredSize(new Dimension(60, 20));
		Util.showHandCursorOnHover(deleteBtn);
		deleteBtn.addActionListener((evt) -> {
			tableResultsPanel.clearRecords();
		});
		deleteBtn.setLayout(new BorderLayout());

		removeHeader.setLayout(new BoxLayout(removeHeader, BoxLayout.X_AXIS));
		removeHeader.setBackground(HEADER_BG_COLOR);
		removeHeader.setBorder(new EmptyBorder(7, 7, 7, 7));

		removeHeader.add(Box.createRigidArea(new Dimension(10, 0)));
		removeHeader.add(deleteBtn);
	}

	void buildHeaderContainer()
	{
		headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
		//headerContainer.setLayout(new BorderLayout());
		headerContainer.setBackground(HEADER_BG_COLOR);
		headerContainer.setPreferredSize(new Dimension(0, 60));

		if (headerStr.endsWith("…"))
		{
			// If header is truncated, show the full text on hover
			headerContainer.setToolTipText(fullHeaderStr + " - " + bossDropRecord.GEPriceTotalFormatted());
		}

		headerContainer.add(leftHeader);
		headerContainer.add(removeHeader);
		add(headerContainer);

	}

	void buildItemsContainer()
	{

		int i = 0;
		for (BossDropItem item : items)
		{
			if (item != null)
			{
				try
				{
					BossDropItemPanel itemPanel = new BossDropItemPanel(this, item, config, i, i > 0);
					itemPanels.add(itemPanel);
					listViewContainer.add(itemPanel);
				}
				catch (Exception ex)
				{
					log.info(ex.getMessage());
				}
			}
			i++;
		}

		listViewContainer.setLayout(new BoxLayout(listViewContainer, BoxLayout.Y_AXIS));
		add(listViewContainer);
	}
}