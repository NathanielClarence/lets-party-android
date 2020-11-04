package com.example.letsparty.serverconnector;

import android.content.Context;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.games.Game;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.List;

/**
 * An interface containing various methods to communicate with the server.
 * May need a better name.
 */
public interface ServerConnector {


    Task<Room> createRoom(Player player);
    Task<Room> joinRoom(String roomCode, Player player);
    void quitRoom(String roomCode, String playerId);
    void changeNickname(String roomCode, String playerId, String nickname);

    Task<List<String>> startMatch(String roomCode);

    //parameters not final, just placeholder for now
    void gameFinish(String roomCode, String playerId, String gameId, double points);
}
