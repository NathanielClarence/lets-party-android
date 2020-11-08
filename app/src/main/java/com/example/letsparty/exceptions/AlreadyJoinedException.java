package com.example.letsparty.exceptions;

import androidx.annotation.Nullable;

public class AlreadyJoinedException extends RuntimeException{
    private final String roomCode;
    private final String playerName;

    public AlreadyJoinedException(String roomCode, String playerName){
        this.roomCode = roomCode;
        this.playerName = playerName;
    }

    @Nullable
    @Override
    public String getMessage() {
        return "Player with name " + playerName + " already exists in room " + roomCode;
    }
}
