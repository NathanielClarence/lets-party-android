package com.example.letsparty.serverconnector;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

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

    StubServerConnector(){}

    @Override
    public Task<Room> createRoom(Player host) {

        Room room = new Room("7ES7", host);
        rooms.add(room);
        return Tasks.forResult(room);
    }

    @Override
    public Task<Room> joinRoom(String roomCode, String playerId) {
        return null;
    }

    @Override
    public void quitRoom(String roomCode, String playerId) {

    }

    @Override
    public void changeNickname(String roomCode, String playerId, String nickname) {

    }

    @Override
    public Task<List<String>> startMatch(String roomCode) {

         return Tasks.forResult(
                    Stream.of("ClearDanger", "Landscape", "MeasureVoice", "ShakePhone")
                            .collect(Collectors.toList())
         );
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
