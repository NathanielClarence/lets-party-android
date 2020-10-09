package com.example.letsparty;

/**
 * An interface containing various methods to communicate with the server.
 * May need a better name.
 */
public interface ServerConnector {
    Room createRoom(String playerId);

    Room joinRoom(String roomCode, String playerId);

    void startMatch(String roomCode);

    //parameters not final, just placeholder for now
    void gameFinish(String roomCode, String playerId, String gameId, double points);
}
