package com.example.letsparty.serverconnector;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class FirebaseServerConnector implements ServerConnector{

    Set<Room> rooms = new HashSet<>();
    public FirebaseServerConnector(FirebaseFunctions mFunctions){
       // this.mFunctions = mFunctions;
    }


    @Override
    public Room createRoom(FirebaseFunctions mFunctions, String playerId) {
        Player host = new Player(playerId);


        getRoom(mFunctions, playerId)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
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

                    }
                });
         Room room = new Room("7ES7", host);
         rooms.add(room);
         return room;

    }
    private Task<String> getRoom(FirebaseFunctions mFunctions, String playerName) {
        // Create the arguments to the callable function.
        // Here is a website to get help with calling functions
        // https://firebase.google.com/docs/functions/callable#java_2
        Map<String, Object> data = new HashMap<>();
        data.put("playerName", playerName);
       // data.put("push", true);

        return mFunctions
                .getHttpsCallable("getRoom")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
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
    public List<String> startMatch(String roomCode) {
        return null;
    }

    @Override
    public void gameFinish(String roomCode, String playerId, String gameId, double points) {

    }
}
