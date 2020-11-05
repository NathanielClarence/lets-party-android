package com.example.letsparty.serverconnector;

import android.util.Log;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

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

// Dialog for player name
        return getRoom(mFunctions, host)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                            FirebaseFunctionsException.Code code = ffe.getCode();
                            Object details = ffe.getDetails();
                        }
                        // ...
                        /* Log.w(TAG, "addNumbers:onFailure", e);
                        showSnackbar("An error occurred.");
                        return; */
                    }
                    // ...
                    String result = task.getResult();
                    //binding.fieldAddResult.setText(String.valueOf(result));
                    Log.d("createRoom", result );

                })
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
        return null;
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
