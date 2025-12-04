package com.bossbuddy.views;

import com.bossbuddy.BossBuddyConfig;
import com.bossbuddy.BossBuddyPlugin;
import com.bossbuddy.ConfigLoot;
import com.bossbuddy.loot.BossDropItem;
import com.bossbuddy.loot.BossDropRecord;
import com.bossbuddy.util.Constants;
import com.bossbuddy.util.Util;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.SwingUtil;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BossBuddyPanel extends PluginPanel {

    @Inject
    private Gson gson;
    private ConfigLoot configLoot = null;
    private BossBuddyConfig config;
    private BossDropRecord bossDropRecord;
    private TableResultsPanel tablePanel;
    private IconTextField monsterSearchField = new IconTextField();
    private final JPanel mainPanel = new JPanel();
    private JButton removeLoot = new JButton();
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();
    private int targetCombatLevel = 0;
    private int targetMonsterId = -1;
    private String panelMonsterName = "";
    public String profileKey = null;
    private ConfigManager configManager = null;
    private ItemManager itemManager = null;
    private BossBuddyPlugin plugin = null;

    @Inject
    private ClientThread clientThread;

    public BossBuddyPanel(BossBuddyPlugin plugin, BossBuddyConfig config, ConfigManager configManager, ItemManager itemManager, Gson gson, ClientThread clientThread, String profileKey) {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        this.itemManager = itemManager;
        this.gson = gson;
        this.clientThread = clientThread;
        this.profileKey = profileKey;

        // Layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Main Panel

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Search Field

        buildSearchField();

        SwingUtil.removeButtonDecorations(removeLoot);
        removeLoot.setText("Remove All Drops");
        removeLoot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        removeLoot.setUI(new BasicButtonUI());
        removeLoot.setToolTipText("Remove All Drops");
        removeLoot.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 30));
        removeLoot.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        Util.showHandCursorOnHover(removeLoot);

        removeLoot.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        removeAllLoot(monsterSearchField.getText());
                    }
                });

        removeLoot.setLayout(new BorderLayout());

        // Error Panel - Empty State
        errorPanel.setContent(Constants.PLUGIN_NAME, "Enter a monster name.");

        add(monsterSearchField);

        add(mainPanel);
        add(errorPanel);
       // add(removeLoot);
    }

    void removeRecord(int itemIndex){

        log.info(String.valueOf(itemIndex * 4));
        log.info(String.valueOf(configLoot.getDrops()[itemIndex * 4]));
        clientThread.invokeLater(()->  plugin.removeLoot(panelMonsterName,itemIndex * 4));

         //remove item from drops and reset loot config
        //plugin.removeRecord(itemIndex);
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


    void rebuildMainPanel() {
        remove(errorPanel);
        SwingUtil.fastRemoveAll(mainPanel);

        tablePanel = new TableResultsPanel(this,config, bossDropRecord);

        mainPanel.add(tablePanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    void resetMainPanel() {
        SwingUtil.fastRemoveAll(mainPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
        add(errorPanel);
    }

    void buildSearchField() {
        monsterSearchField.setIcon(IconTextField.Icon.SEARCH);
        monsterSearchField.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        monsterSearchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        monsterSearchField.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        monsterSearchField.setMinimumSize(new Dimension(0, 30));

        monsterSearchField.addActionListener(
                evt -> {
                    searchForMonsterName(monsterSearchField.getText());
                });
        monsterSearchField.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        searchForMonsterName(monsterSearchField.getText());
                    }
                });
        monsterSearchField.addClearListener(
                () -> {
                    reset();
                });
    }

    void buildButton(JButton btn, ImageIcon icon, ImageIcon selectedIcon, String on, String off, ActionListener listener) {
        SwingUtil.removeButtonDecorations(btn);
        btn.setIcon(icon);
        btn.setSelectedIcon(selectedIcon);
        btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        btn.setUI(new BasicButtonUI());

        SwingUtil.addModalTooltip(btn, on, off);
        Util.showHandCursorOnHover(btn);
        btn.addActionListener(listener);
    }


    void searchForMonsterName(String monsterName) {
        if (monsterName.isEmpty()) return;
        
        monsterSearchField.setEditable(false);
        monsterSearchField.setIcon(IconTextField.Icon.LOADING_DARKER);
        configLoot = getLootConfig(monsterName);
        monsterSearchField.setIcon(configLoot == null? IconTextField.Icon.ERROR : IconTextField.Icon.SEARCH);
        monsterSearchField.setEditable(true);

        if(configLoot == null)
            return;

        panelMonsterName = monsterName;
        clientThread.invokeLater(()-> plugin.buildPanelItems(configLoot));
    }


    public void refreshMainPanel() {
        if (bossDropRecord != null) {
            SwingUtilities.invokeLater(() -> {
                rebuildMainPanel();
            });
        }
    }

    public void refreshMainPanelWithRecords(String monsterName, BossDropItem[] bdi, int killCount) {
        configLoot = getLootConfig(monsterName);
        bossDropRecord = new BossDropRecord(monsterName, bdi, killCount);

        SwingUtilities.invokeLater(() -> {
            rebuildMainPanel();
        });
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

    void resetSearchField() {
        monsterSearchField.setIcon(IconTextField.Icon.SEARCH);
        monsterSearchField.setText("");
        monsterSearchField.setEditable(true);
    }

    public void reset() {
        SwingUtilities.invokeLater(() -> {
            resetSearchField();
            resetMainPanel();
        });
    }

    public void lookupMonsterDrops(String monsterName, int combatLevel, int monsterId) {
        targetCombatLevel = combatLevel;
        targetMonsterId = monsterId;

        SwingUtilities.invokeLater(() -> {
            monsterSearchField.setText(monsterName);
            searchForMonsterName(monsterName);
        });
    }

    public void removeAllLoot(String monsterName) {
        ConfigLoot lootConfig = getLootConfig(monsterName);
        int[] newArray = new int[0];
        lootConfig.setDrops(newArray);
        setLootConfig(lootConfig.getName(), lootConfig);
    }
}