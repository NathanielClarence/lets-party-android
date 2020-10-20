package com.example.letsparty.serverconnector;

import com.example.letsparty.entities.Room;
import com.example.letsparty.games.Game;

import com.google.firebase.functions.FirebaseFunctions;

import java.util.List;

/**
 * An interface containing various methods to communicate with the server.
 * May need a better name.
 */
public interface ServerConnector {

    //Room createRoom(String playerId);
    Room createRoom(FirebaseFunctions mFunctions, String playerId);
    Room joinRoom(String roomCode, String playerId);
    void quitRoom(String roomCode, String playerId);
    void changeNickname(String roomCode, String playerId, String nickname);

    List<String> startMatch(String roomCode);

    //parameters not final, just placeholder for now
    void gameFinish(String roomCode, String playerId, String gameId, double points);
}
