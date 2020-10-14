package com.example.letsparty.exceptions;

public class RoomNotFoundException extends RuntimeException {
    private final String roomCode;

    public RoomNotFoundException(String roomCode){
        this.roomCode = roomCode;
    }
}
