package com.example.letsparty;

/**
 * A server connector that is a stub, used for development.
 * Does not actually connect to the server.
 */
public class StubServerConnector implements ServerConnector {
    @Override
    public Room createRoom(String playerId) {
        Player host = new Player(playerId);
        Room room = new Room("TEST", host);
        return room;
    }

    @Override
    public Room joinRoom(String roomCode, String playerId) {
        return null;
    }

    @Override
    public void startMatch(String roomCode) {

    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
