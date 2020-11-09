package com.example.letsparty.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.letsparty.R;
import com.example.letsparty.entities.Player;
import com.example.letsparty.entities.Room;
import com.example.letsparty.games.Game;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameRunner extends AppCompatActivity {
    private Room room;
    private Player player;
    private List<String> gameIds;
    private boolean[] gameStarted;
    TextView scores;
    TextView winnerText;
    private boolean isWaiting = false;
    private int currentGameIndex = 0;
    private BroadcastReceiver nextGameBr;
    private BroadcastReceiver cancelGameBr;
    private TaskCompletionSource<Boolean> nextGameTcs;
    private CancellationTokenSource nextGameCts = new CancellationTokenSource();
    private Handler nextGameHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_runner);
        scores = findViewById(R.id.score);
        winnerText = findViewById(R.id.winnerText);


        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        this.gameIds = intent.getStringArrayListExtra("gameIds");
        this.gameStarted = new boolean[this.gameIds.size()];
        Arrays.fill(this.gameStarted, false);
        this.player = (Player) intent.getSerializableExtra(MainActivity.PLAYER);

        cancelGameBr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                cleanup();
                finish();
                Toast.makeText(context, "Host has ended the match", Toast.LENGTH_SHORT).show();
            }
        };
        IntentFilter cancelFilter = new IntentFilter("cancel_match");
        LocalBroadcastManager.getInstance(this).registerReceiver(cancelGameBr, cancelFilter);

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
        gameStarted[i] = true;
        startActivityForResult(intent, i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        winnerText.setText("Waiting for other players...");
        scores.setText("");
        if (resultCode == RESULT_OK)
        {
             //send game completion to server
            ServerConnector sc = ServerUtil.getServerConnector(this);
            long time = data.getLongExtra(Game.TIME_ELAPSED, 0);
            String gameId = data.getStringExtra(Game.GAME_ID);
            boolean success = data.getBooleanExtra(Game.SUCCESS, true);
            sc.gameFinish(this.room.getRoomCode(), player.getNickname(), gameId, time, 0, success);
            //sc.gameFinish();

            currentGameIndex = requestCode + 1;
            readyForNextGame(currentGameIndex);
        } else {
            this.quitRoom();
        }


    }

    private void readyForNextGame(int i) {
        isWaiting = true;
        waitForNextGame()
                .addOnSuccessListener(res -> {
                    isWaiting = false;
                    if (i < this.gameIds.size()) {
                        //if there are games remaining, go to next game

                        //handle errors of multiple broadcast results
                        //if the game has started, then don't start it again
                        if (gameStarted[i])
                            return;

                        //start the next game after a few seconds so user can see the results
                        nextGameHandler = new Handler();
                        nextGameHandler.postDelayed(
                                () -> runGame(i),
                                3000
                        );
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
        nextGameTcs = new TaskCompletionSource<>(nextGameCts.getToken());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        nextGameBr = new BroadcastReceiver()
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
                nextGameTcs.trySetResult(true);
                lbm.unregisterReceiver(this);
            }
        };

        IntentFilter filter = new IntentFilter("game_ready");
        lbm.registerReceiver(nextGameBr, filter);

        return nextGameTcs.getTask();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String quitMessage = "Quit the room?";
        builder.setMessage(quitMessage)
                .setPositiveButton("Quit", (dialog, i) -> this.quitRoom())
                .setNegativeButton("Cancel", (dialog, i) -> dialog.dismiss())
                .show();
    }

    public void quitRoom() {
        ServerConnector sc = ServerUtil.getServerConnector(this);
        sc.quitRoom(this.room.getRoomCode(), this.player)
                .addOnSuccessListener(res ->{
                    this.cleanup();
                    this.finish();
                })
                .addOnFailureListener(ex -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cleanup() {
        if (nextGameBr != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(nextGameBr);
        if (cancelGameBr != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(cancelGameBr);
        nextGameCts.cancel();
        if (nextGameHandler != null)
            nextGameHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("gameIds", new ArrayList<>(this.gameIds));
        outState.putBooleanArray("gameStarted", this.gameStarted);
        outState.putBoolean("waiting", isWaiting);
        outState.putInt("currentGameIndex", currentGameIndex);
        outState.putString("winnerText", winnerText.getText().toString());
        outState.putString("scoreText", scores.getText().toString());
        if (isWaiting) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(nextGameBr);
            nextGameCts.cancel();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.gameIds = savedInstanceState.getStringArrayList("gameIds");
        this.gameStarted = savedInstanceState.getBooleanArray("gameStarted");
        this.winnerText.setText(savedInstanceState.getString("winnerText"));
        this.scores.setText(savedInstanceState.getString("scoreText"));

        this.isWaiting = savedInstanceState.getBoolean("waiting");
        this.currentGameIndex = savedInstanceState.getInt("currentGameIndex");
        if (isWaiting)
            this.readyForNextGame(currentGameIndex);
    }

}
