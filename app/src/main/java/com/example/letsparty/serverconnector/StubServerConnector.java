package com.example.letsparty.serverconnector;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.games.ClearDanger;
import com.example.letsparty.games.Game;
import com.example.letsparty.games.Landscape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A server connector that is a stub, used for development.
 * Does not actually connect to the server.
 */
public class StubServerConnector implements ServerConnector {

    Set<Room> rooms = new HashSet<>();
    @Override
    public Room createRoom(String playerId) {
        Player host = new Player(playerId);
        Room room = new Room("7ES7", host);
        rooms.add(room);
        return room;
    }

    @Override
    public Room joinRoom(String roomCode, String playerId) {
        return null;
    }

    @Override
    public void quitRoom(String roomCode, String playerId) {

    }

    @Override
    public void changeNickname(String roomCode, String playerId, String nickname) {

    }

    @Override
    public List<Class<? extends Game>> startMatch(String roomCode) {

         return Stream.of(ClearDanger.class, Landscape.class).collect(Collectors.toList());
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
