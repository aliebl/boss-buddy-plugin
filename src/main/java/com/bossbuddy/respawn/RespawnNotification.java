package com.bossbuddy.respawn;

import com.bossbuddy.BossBuddyConfig;
import com.google.inject.Inject;
import net.runelite.api.SoundEffectID;
import net.runelite.client.Notifier;
import net.runelite.client.audio.AudioPlayer;

import java.util.concurrent.Callable;

public class RespawnNotification implements Callable<String> {

    private final Notifier notifier;
    private final String npcName;

    public RespawnNotification(String npcName, Notifier notifier){
        this.npcName = npcName;
        this.notifier = notifier;
    }

    @Override
    public String call() throws Exception {
        notifier.notify(npcName + " is about to spawn.");
        return "Result from " + npcName + " timer";
    }
}
