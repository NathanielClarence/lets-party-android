package com.example.letsparty;

import java.util.UUID;

public class PlayerUtil {
    private static String playerId;

    public static String getPlayerId(){
        if (playerId == null)
            playerId = UUID.randomUUID().toString();
        return playerId;
    }
}