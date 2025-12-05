package com.bossbuddy.loot;

import com.bossbuddy.util.Util;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.runelite.client.util.AsyncBufferedImage;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class BossDropItem {
    private final int id;
    private final String name;
    private final int quantity;
    private final int killCount;
    private final int date;
    private final int gePrice;
    private final boolean tradeable;
    private AsyncBufferedImage image;

    public String getGEPriceFormatted() {
        String priceLabelStr = gePrice > 0 ? Util.rsFormat(gePrice) : "";
        if (name.equals("Nothing")) {
            priceLabelStr = "";
        }
        return priceLabelStr;
    }
}


