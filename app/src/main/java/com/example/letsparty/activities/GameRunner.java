package com.example.letsparty.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.letsparty.R;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.games.Game;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameRunner extends AppCompatActivity {
    private Room room;
    private Player player;
    private List<String> gameIds;
    TextView scores;
    TextView winnerText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_runner);
        scores = findViewById(R.id.score);
        winnerText = findViewById(R.id.winnerText);


        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        this.gameIds = intent.getStringArrayListExtra("gameIds");
        this.player = (Player) intent.getSerializableExtra(MainActivity.PLAYER);

        //only start the game if the state isn't recreated
        if (savedInstanceState == null)
            runGame(0);
    }

    private void runGame(int i){
        //obtain the game to be played
        Class<? extends Game> gameClass = Game.GAME_IDS.get(this.gameIds.get(i));
        if (gameClass == null){
            throw new RuntimeException("Game with id " +  gameIds.get(i) + " does not exist");
        }

        //start the game
        Intent intent = new Intent(this, gameClass);
        intent.putExtra(MainActivity.ROOM, room);
        startActivityForResult(intent, i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        winnerText.setText("Waiting for other players...");
        scores.setText("");
        if (resultCode == RESULT_OK)
        {
            //placeholder for calculating point. Can be changed later

            //send game completion to server
            ServerConnector sc = ServerUtil.getServerConnector(this);
            long time = data.getLongExtra(Game.TIME_ELAPSED, 0);
            String gameId = data.getStringExtra(Game.GAME_ID);
            boolean success = data.getBooleanExtra(Game.SUCCESS, true);
            sc.gameFinish(this.room.getRoomCode(), player.getNickname(), gameId, time, 0, success);
            //sc.gameFinish();
        }

        int i = requestCode + 1;
        readyForNextGame(i);
    }

    private void readyForNextGame(int i) {
        waitForNextGame()
                .addOnCompleteListener(task -> {
                    if (i < this.gameIds.size()) {
                        //if there are games remaining, go to next game
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask()
                        {

                            public void run()
                            {
                                runGame(i);
                            }

                        }, 4000);

                    } else {
                        //if no games remaining, go to result screen
                        Intent intent = new Intent(this, Results.class);
                        intent.putExtra(MainActivity.ROOM, this.room);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private Task<Boolean> waitForNextGame(){
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver br = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String winner = intent.getStringExtra("winner");
                System.out.println("***************received winner in GameRunner is: " + winner);
                winnerText.setText("Winner is: " + winner);
                List<Player> playersList = room.getPlayers();
                for(int i=0; i<playersList.size(); i++)
                {
                    Player p = playersList.get(i);
                    if (p.getNickname().equals(winner))
                    {
                        p.setScore(p.getScore() + 5);
                        playersList.set(i, p);
                    }
                    scores.append(p.getNickname() + ": " + p.getScore() + "\n");
                }
                tcs.trySetResult(true);
                lbm.unregisterReceiver(this);
            }
        };

        IntentFilter filter = new IntentFilter("game_ready");
        lbm.registerReceiver(br, filter);

        return tcs.getTask();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("gameIds", new ArrayList<>(this.gameIds));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.gameIds = savedInstanceState.getStringArrayList("gameIds");
    }

    private static class GameBroadcastReceiver extends BroadcastReceiver{
        private TaskCompletionSource<Boolean> tcs;

        GameBroadcastReceiver(TaskCompletionSource<Boolean> tcs){
            this.tcs = tcs;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            tcs.setResult(true);
            Log.d("broadcast", "next game received");

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.unregisterReceiver(this);
        }
    }
}
