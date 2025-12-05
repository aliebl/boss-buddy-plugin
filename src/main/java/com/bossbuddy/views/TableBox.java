package com.bossbuddy.views;


import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.loot.BossDropRecord;
import com.bossbuddy.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        //setLayout(new BorderLayout(0, 1));
        //setBorder(new EmptyBorder(5, 0, 0, 0));

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

        JLabel headerLabel = new JLabel(headerStr);
        headerLabel.setFont(FontManager.getRunescapeBoldFont());
        headerLabel.setForeground(ColorScheme.BRAND_ORANGE);
        headerLabel.setMinimumSize(new Dimension(1, headerLabel.getPreferredSize().height));
        //headerLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel headerKillLabel = new JLabel("x " + bossDropRecord.getKills());
        headerKillLabel.setFont(FontManager.getRunescapeSmallFont());
        headerKillLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        //headerKillLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel headerPriceLabel = new JLabel(bossDropRecord.GEPriceTotalFormatted() );
        headerPriceLabel.setFont(FontManager.getRunescapeSmallFont());
        headerPriceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        //headerTotalLabel.setHorizontalAlignment(JLabel.CENTER);

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

    void buildHeaderContainer() {
        headerContainer.setLayout(new BorderLayout());
        headerContainer.setBackground(HEADER_BG_COLOR);
        headerContainer.setPreferredSize(new Dimension(0, 30));


        //Util.showHandCursorOnHover(headerContainer);
        headerContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                //toggleCollapse();
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                //headerContainer.setBackground(HOVER_COLOR);
                //leftHeader.setBackground(HOVER_COLOR);
                //collapseBtn.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                //headerContainer.setBackground(HEADER_BG_COLOR);
                //leftHeader.setBackground(HEADER_BG_COLOR);
                //collapseBtn.setBackground(HEADER_BG_COLOR);
            }
        });
        if (headerStr.endsWith("…")) {
            // If header is truncated, show the full text on hover
            headerContainer.setToolTipText(fullHeaderStr + " - " + bossDropRecord.GEPriceTotalFormatted());
        }

        headerContainer.add(leftHeader, BorderLayout.NORTH);
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