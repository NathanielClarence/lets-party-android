package com.example.letsparty.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;

import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.letsparty.R;
import com.example.letsparty.databinding.ActivityLobbyBinding;
import com.example.letsparty.entities.Room;
import com.example.letsparty.serverconnector.ServerConnector;
import com.example.letsparty.serverconnector.ServerUtil;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Lobby extends AppCompatActivity {

    private Room room;
    private String playerId;
    private List<String> gameIds;
    private Bitmap bitmap;
    private ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLobbyBinding binding = ActivityLobbyBinding.inflate(getLayoutInflater());

        Intent intent = getIntent();
        this.room = (Room) intent.getSerializableExtra(MainActivity.ROOM);
        String roomCode = room.getRoomCode();

        binding.textView.setText(roomCode);
        generateQRCode(roomCode);

        this.playerId = intent.getStringExtra(MainActivity.PLAYER_ID);
        boolean isHost = room.getHost().getId().equals(this.playerId);
        binding.startButton.setVisibility(isHost ? View.VISIBLE : View.INVISIBLE);
        binding.readyButton.setVisibility(isHost ? View.INVISIBLE : View.VISIBLE);

        //binding.editTextTextPersonName.on
        binding.startButton.setOnClickListener(view -> startMatch());
        binding.readyButton.setOnClickListener(view -> waitForMatchStart());

        setContentView(binding.getRoot());
    }

    private void startMatch() {
        ServerConnector sc = ServerUtil.getServerConnector();
        //tell server the match has started and obtain list of games from server
        sc.startMatch(room.getRoomCode())
            .addOnSuccessListener(gameIds -> {
                //start the game runner activity
                Intent intent = new Intent(this, GameRunner.class);
                intent.putStringArrayListExtra("gameIds",new ArrayList<>(gameIds));
                intent.putExtra(MainActivity.ROOM, this.room);
                startActivity(intent);
            });

    }

    private void waitForMatchStart() {
    }

    private void generateQRCode(String RoomCode){
        qrImage = (ImageView) findViewById(R.id.imageView2);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder("letsparty::"+RoomCode, null, QRGContents.Type.TEXT, smallerDimension);

        try{
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        }catch (WriterException e) {
            Log.v("QR ERROR: ", e.toString());
        }
    }
}