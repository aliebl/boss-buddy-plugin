package com.bossbuddy.views;


import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.loot.BossDropRecord;
import com.bossbuddy.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TableBox extends JPanel {
    private BossBuddyConfig config;
    private TableResultsPanel tableResultsPanel;

    private BossDropRecord bossDropRecord;
    public BossDropItem[] items;
    private ViewOption viewOption;
    private String fullHeaderStr;
    private String headerStr;
    private JButton percentBtn;

    private final JButton collapseBtn = new JButton();
    private final JPanel listViewContainer = new JPanel();
    private JPanel gridViewPanel = new JPanel();
    private final JPanel headerContainer = new JPanel();
    private final JPanel leftHeader = new JPanel();

    private final Color HEADER_BG_COLOR = ColorScheme.DARKER_GRAY_COLOR.darker();
    private final Color HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR.darker();

    private final List<BossDropItemPanel> itemPanels = new ArrayList<>();
    private static int maxHeaderLength = 18;

    public TableBox(TableResultsPanel tableResultsPanel, BossBuddyConfig config, BossDropRecord bossDropRecord, String headerStr) {
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

    void removeRecord(int itemIndex){
        tableResultsPanel.removeRecord(itemIndex);
    }

    void buildHeader() {
        buildLeftHeader();
        buildHeaderContainer();
    }

    void buildLeftHeader() {
        if (headerStr.length() > maxHeaderLength) {
            headerStr = headerStr.substring(0, maxHeaderLength) + "…"; // Manually truncate the header
        }

        JLabel headerLabel = new JLabel(headerStr + " - " + bossDropRecord.GEPriceTotalFormatted());
        headerLabel.setFont(FontManager.getRunescapeBoldFont());
        headerLabel.setForeground(ColorScheme.BRAND_ORANGE);
        headerLabel.setHorizontalAlignment(JLabel.CENTER);

        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.X_AXIS));
        leftHeader.setBackground(HEADER_BG_COLOR);
        leftHeader.add(Box.createRigidArea(new Dimension(10, 0)));
        leftHeader.add(headerLabel);

    }

    void buildHeaderContainer() {
        headerContainer.setLayout(new BorderLayout());
        headerContainer.setBackground(HEADER_BG_COLOR);
        headerContainer.setPreferredSize(new Dimension(0, 40));

        Util.showHandCursorOnHover(headerContainer);
        headerContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                toggleCollapse();
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                headerContainer.setBackground(HOVER_COLOR);
                leftHeader.setBackground(HOVER_COLOR);
                collapseBtn.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                headerContainer.setBackground(HEADER_BG_COLOR);
                leftHeader.setBackground(HEADER_BG_COLOR);
                collapseBtn.setBackground(HEADER_BG_COLOR);
            }
        });
        if (headerStr.endsWith("…")) {
            // If header is truncated, show the full text on hover
            headerContainer.setToolTipText(fullHeaderStr + " - " + bossDropRecord.GEPriceTotalFormatted());
        }

        headerContainer.add(leftHeader, BorderLayout.WEST);
        add(headerContainer);

    }

    void buildItemsContainer() {

        int i = 0;
        for (BossDropItem item : items) {
            if(item != null) {
                try {
                    BossDropItemPanel itemPanel = new BossDropItemPanel(this, item, config, i, i > 0);
                    itemPanels.add(itemPanel);
                    listViewContainer.add(itemPanel);
                }catch(Exception ex){
                    log.info(ex.getMessage());
                }
            }
            i++;
        }

        listViewContainer.setLayout(new BoxLayout(listViewContainer, BoxLayout.Y_AXIS));
        add(listViewContainer);
    }


    void collapse() {
        if (!isCollapsed()) {
            collapseBtn.setSelected(true);
            listViewContainer.setVisible(false);
            gridViewPanel.setVisible(false);
        }
    }

    void expand() {
        if (isCollapsed()) {
            collapseBtn.setSelected(false);
            listViewContainer.setVisible(true);
            gridViewPanel.setVisible(true);
        }
    }

    void toggleCollapse() {
        if (isCollapsed()) {
            expand();
        } else {
            collapse();
        }
    }

    boolean isCollapsed() {
        return collapseBtn.isSelected();
    }
}