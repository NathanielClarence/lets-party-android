package com.example.letsparty.serverconnector;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

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
    private Context context;

    StubServerConnector(){
       this.context = new Activity();
    }


    @Override
    public Task<Room> createRoom(Player host) {

        Room room = new Room("7ES7", host);
        rooms.add(room);
        return Tasks.forResult(room);

    }

    @Override
    public Task<Room> joinRoom(String roomCode, Player player) {
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
        ArrayList<String> gameList = new ArrayList<>(
                                        Stream.of("ClearDanger", "Landscape", "MeasureVoice", "ShakePhone")
                                                .collect(Collectors.toList())
                                    );

        new Handler().postDelayed(() -> {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent("players_ready");
            intent.putStringArrayListExtra("game_ids", gameList);
            lbm.sendBroadcast(intent);
        }, 3000);

        return Tasks.forResult(gameList);
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {
        new Handler().postDelayed(() -> {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent("game_ready");
            lbm.sendBroadcast(intent);
        }, 3000);
    }
}
