package com.example.letsparty.serverconnector;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * An interface containing various methods to communicate with the server.
 * May need a better name.
 */
public interface ServerConnector {


    Task<Room> createRoom(Player player);
    Task<Room> joinRoom(String roomCode, Player player);
    Task<Boolean> quitRoom(String roomCode, Player player);
    void changeNickname(String roomCode, String playerId, String nickname);

    Task<Boolean> startMatch(String roomCode, Player player);

    //parameters not final, just placeholder for now
    //void gameFinish(String roomCode, String playerId, String gameId, double points);
    Task<Object> gameFinish(String roomCode, String playerName, String gameId, double time, double value, boolean success);
}
