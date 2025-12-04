package com.bossbuddy.loot;

import com.bossbuddy.util.Util;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.loottracker.LootRecordType;

@Slf4j
@Value
public class BossDropRecord {
    @NonNull
    private final String bossName;
    private final BossDropItem[] items;
    private final int kills;

    public int GEPriceTotal(){
        int totalPrice = 0;
        for(BossDropItem item : items){
            totalPrice += item.getGePrice();
        }
        return totalPrice;
    }

    public String GEPriceTotalFormatted() {

        return   GEPriceTotal() > 0 ? Util.rsFormat(GEPriceTotal()) : "";
    }
}
