package com.example.letsparty.serverconnector;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.exceptions.AlreadyJoinedException;
import com.example.letsparty.exceptions.RoomNotFoundException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseServerConnector implements ServerConnector{

    FirebaseFunctions mFunctions;

    FirebaseServerConnector(FirebaseFunctions mFunctions){
       this.mFunctions = mFunctions;
    }

    @Override
    public Task<Room> createRoom(Player host) {
        return getRoom(mFunctions, host)
                .onSuccessTask(roomCode -> Tasks.forResult(new Room(roomCode, host)));
    }
    private Task<String> getRoom(FirebaseFunctions mFunctions, Player player) {
        // Create the arguments to the callable function.
        // Here is a website to get help with calling functions
        // https://firebase.google.com/docs/functions/callable#java_2
        Map<String, Object> data = new HashMap<>();
        data.put("playerName", player.getNickname());
        data.put("token", player.getToken());
       // data.put("push", true);

        return mFunctions
                .getHttpsCallable("getRoom")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    return result;
                });
    }
    @Override
    public Task<Room> joinRoom(String roomCode, Player player) {
        // Create the arguments to the callable function.
        // Here is a website to get help with calling functions
        // https://firebase.google.com/docs/functions/callable#java_2
        Map<String, Object> data = new HashMap<>();
        data.put("code", roomCode);
        data.put("playerName", player.getNickname());
        data.put("token", player.getToken());

        return mFunctions.getHttpsCallable("joinRoom")
                .call(data)
                //check for exceptions
                .continueWith(task -> {
                    String result = (String) task.getResult().getData();
                    switch(result){
                        case "ROOMNOTFOUND": throw new RoomNotFoundException(roomCode);
                        case "BADNAME": throw new AlreadyJoinedException(roomCode, player.getNickname());
                        default: return result;
                    }
                })
                //return room
                .onSuccessTask(roomString -> {
                    Room room = Room.processRoomString(roomString, roomCode, player);
                    return Tasks.forResult(room);
                });
    }

    @Override
    public void quitRoom(String roomCode, String playerId) {

    }

    @Override
    public void changeNickname(String roomCode, String playerId, String nickname) {

    }

    @Override
    public Task<List<String>> startMatch(String roomCode) {
        return null;
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
