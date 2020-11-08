package com.example.letsparty.exceptions;

import androidx.annotation.Nullable;

public class RoomNotFoundException extends RuntimeException {
    private final String roomCode;

    public RoomNotFoundException(String roomCode){
        this.roomCode = roomCode;
    }

    @Nullable
    @Override
    public String getMessage() {
        return "Cannot find room " + roomCode;
    }
}
