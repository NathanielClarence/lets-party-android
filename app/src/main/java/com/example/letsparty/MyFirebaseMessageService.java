package com.example.letsparty;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class MyFirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "Firebase" ;
    public static ArrayList<String> playerList = new ArrayList<>();

    public MyFirebaseMessageService() {
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
       // sendRegistrationToServer(token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
        {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
               // scheduleJob();
            } else {
                // Handle message within 10 seconds
               // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        String action = remoteMessage.getData().get("action");
        System.out.println("*****************Received action is: " + action);
        switch(action)
        {
            case "START": sendStartBroadcast(remoteMessage.getData());
            case "JOIN": break;
            case "LISTUPDATE":
                //Data received as string
                String pList = (String) remoteMessage.getData().get("users");
                //parse string to arraylist
                String[] players = pList.split(String.valueOf(','));

                Log.d("LISTUPDATE", pList);
                //update list of players in lobby
//                Lobby.updatePlayers(players);
                MyFirebaseMessageService.playerList.clear();
                for (String e: players){
                    MyFirebaseMessageService.playerList.add(e.replaceAll("[^a-zA-Z0-9]", ""));
                }
                break;
            case "ENDGAME":
                System.out.println("*********ENDGAME********");
                sendEndGameBroadcast(remoteMessage.getData().get("winner"));
                break;
            //case others: break;
            case "ROOMEND": sendMatchCancelledBroadcast(); break;
            default: break;
        }
    }

    public static void addPlayerToList(String n){
        if (!MyFirebaseMessageService.playerList.contains(n)) {
            MyFirebaseMessageService.playerList.add(n);
            Log.e("SS", MyFirebaseMessageService.playerList.toString());
        }
    }

    public static ArrayList<String> getPlayerList(){
        return playerList;
    }

    private void sendStartBroadcast(Map<String, String> data){
        ArrayList<String> games = new ArrayList<>();

        //convert games data from json into array list
        try {
            JSONArray jsonGames = new JSONArray(data.get("games"));
            for (int i = 0; i < jsonGames.length(); i++){
                games.add(jsonGames.optString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //broadcast the list of games
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("start_match");
        intent.putStringArrayListExtra("gameIds", games);
        lbm.sendBroadcast(intent);
    }


    private void sendEndGameBroadcast(String winner)
    {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("game_ready");
        intent.putExtra("winner", winner);
        lbm.sendBroadcast(intent);
    }

    private void sendMatchCancelledBroadcast() {
        //broadcast that the match is cancelled
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("start_match");
        intent.putExtra("cancelled", true);
        lbm.sendBroadcast(intent);
    }

}
