package com.bossbuddy.views;


import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.osrswiki.WikiScraper;
import com.bossbuddy.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.SwingUtil;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class BossDropItemPanel extends JPanel {

    @Inject
    private ConfigManager configManager;

    private final TableBox tableBox;
    private final int itemIndex;
    private final BossDropItem item;
    private final AsyncBufferedImage image;
    private String itemName;
    private final Color bgColor = ColorScheme.DARKER_GRAY_COLOR;
    private final Color hoverColor = bgColor.brighter();
    private final JLabel rarityLabel = new JLabel();
    private final JLabel priceLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final JPanel imageContainer = new JPanel(new BorderLayout());
    private final JPanel leftSidePanel = new JPanel(new GridLayout(2, 1));

    public BossDropItemPanel(TableBox tableBox, BossDropItem item, BossBuddyConfig config, int index, boolean showSeparator) {
        this.item = item;
        this.tableBox = tableBox;
        this.image = item.getImage();
        this.itemName = item.getName();
        this.itemIndex = index;

        int maxNameLength = 18;
        if (itemName.length() > maxNameLength) {
            itemName = itemName.replaceAll("\\(.*\\)", "").trim();

            if (itemName.length() > maxNameLength) {
                itemName =  itemName.substring(0, maxNameLength) + "…";
            }
        }

        setBorder(new EmptyBorder(0, 0, 5, 0));
        setLayout(new BorderLayout());
        setBackground(bgColor);

        JPanel container = new JPanel(new BorderLayout());
        JPanel paddingContainer = new JPanel(new BorderLayout());
        int padding = 2;
        paddingContainer.setBorder(new EmptyBorder(padding, padding, padding, padding));

        if (showSeparator)
            container.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR));
        paddingContainer.setBackground(bgColor);

        JPanel leftPanel = buildLeftPanel();
        JPanel rightPanel = buildRightPanel();
        rightPanel.setBackground(bgColor);

        paddingContainer.add(leftPanel, BorderLayout.WEST);
        paddingContainer.add(rightPanel, BorderLayout.EAST);

        container.add(paddingContainer);

        rarityLabel.setFont(FontManager.getRunescapeSmallFont());
        rarityLabel.setForeground(config.commonColor());

        priceLabel.setFont(FontManager.getRunescapeSmallFont());
        priceLabel.setForeground(config.priceColor());

        Util.showHandCursorOnHover(container);
        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                String wikiUrl = WikiScraper.getWikiUrl(item.getName());
                try {
                    Desktop.getDesktop().browse(new URL(wikiUrl).toURI());
                } catch (Exception e) {
                }
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                setBackground(hoverColor);
                paddingContainer.setBackground(hoverColor);
                leftSidePanel.setBackground(hoverColor);
                rightPanel.setBackground(hoverColor);
                imageContainer.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                setBackground(bgColor);
                paddingContainer.setBackground(bgColor);
                leftSidePanel.setBackground(bgColor);
                rightPanel.setBackground(bgColor);
                imageContainer.setBackground(bgColor);
            }
        });

        if (itemName.endsWith("…")) {
            container.setToolTipText(item.getName());
        }

        add(container);
    }

    private JPanel buildImagePanel() {
        imageContainer.setSize(30, imageContainer.getHeight());
        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(new ImageIcon(IconTextField.class.getResource(IconTextField.Icon.LOADING_DARKER.getFile()))); // set loading icon

        imageLabel.setIcon(new ImageIcon(image));
        imageContainer.setBorder(new EmptyBorder(0, 5, 0, Math.max(30 - image.getWidth(), 5)));

        imageLabel.setSize(35, imageLabel.getWidth());

        imageContainer.add(imageLabel, BorderLayout.WEST);
        imageContainer.setSize(30, imageContainer.getHeight());
        imageContainer.setBackground(bgColor);
        return imageContainer;
    }


    private JPanel buildLeftPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        JPanel itemImage = buildImagePanel();

        leftSidePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        leftSidePanel.setBackground(bgColor);

        JLabel itemNameLabel = new JLabel(itemName);
        itemNameLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
        itemNameLabel.setFont(FontManager.getRunescapeBoldFont());
        itemNameLabel.setHorizontalAlignment(JLabel.LEFT);
        itemNameLabel.setVerticalAlignment(JLabel.CENTER);

        rarityLabel.setHorizontalAlignment(JLabel.LEFT);
        rarityLabel.setVerticalAlignment(JLabel.CENTER);
        setRarityLabelText();

        leftSidePanel.add(itemNameLabel);
        leftSidePanel.add(rarityLabel);

        container.add(itemImage);
        container.add(leftSidePanel);
        return container;
    }

    private JPanel buildRightPanel() {
        JPanel rightSidePanel = new JPanel(new GridLayout(2, 1));

        JButton deleteBtn = new JButton();
        SwingUtil.removeButtonDecorations(deleteBtn);
        deleteBtn.setText("x");
        //deleteBtn.setForeground(ColorScheme.DARKER_GRAY_COLOR);
        deleteBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        //deleteBtn.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
        deleteBtn.setUI(new BasicButtonUI());
        deleteBtn.setToolTipText("Remove Record");
        deleteBtn.setPreferredSize(new Dimension(30,10));
        Util.showHandCursorOnHover(deleteBtn);
        deleteBtn.addActionListener((evt) -> {
            tableBox.removeRecord(itemIndex);
        });
        deleteBtn.setLayout(new BorderLayout());

        dateLabel.setFont(FontManager.getRunescapeSmallFont());
        dateLabel.setBorder(new EmptyBorder(0, 0, 3, 2));
        dateLabel.setHorizontalAlignment(JLabel.RIGHT);
        dateLabel.setVerticalAlignment(JLabel.CENTER);

        setPriceLabelText();
        priceLabel.setVerticalAlignment(JLabel.CENTER);
        priceLabel.setHorizontalAlignment(JLabel.RIGHT);

        rightSidePanel.add(deleteBtn);
        rightSidePanel.add(priceLabel);

        return rightSidePanel;
    }

    void setRarityLabelText() {
        int kc =  item.getKillCount();
        int dateInt =  item.getDate();
        String dateString = String.valueOf(dateInt);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter stringFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate date = LocalDate.parse(dateString, dateFormatter);
        String formattedDate = stringFormatter.format(date);

        if(kc == 0)
            rarityLabel.setText(formattedDate);
        else
            rarityLabel.setText(item.getKillCount() + " | " + formattedDate);
    }

    void setPriceLabelText() {
        priceLabel.setText(item.getGEPriceFormatted());
    }

}