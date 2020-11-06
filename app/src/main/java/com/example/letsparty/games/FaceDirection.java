package com.example.letsparty.games;

//import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letsparty.R;
import com.example.letsparty.games.Game;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class FaceDirection extends Game {

    private ImageView imageView;
    private TextView txt_direction;
    private TextView txt_instruction;

    private String direction;
    private float rotationDeg;
    private String directionToWin;

    public SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;
    private SensorEventListener sensorEventListenerAccelrometer;
    private SensorEventListener sensorEventListenerMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    private long startTime;
    private long endTime;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_direction);

        imageView = findViewById(R.id.imageView);
        txt_direction = findViewById(R.id.textView);
        txt_instruction = findViewById(R.id.txt_instruction);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Random random = new Random();
        int rand = random.nextInt(4);
        //String faceThis;
        switch (rand){
            case 0:
                directionToWin = "NORTH";
                break;
            case 1:
                directionToWin = "EAST";
                break;
            case 2:
                directionToWin = "SOUTH";
                break;
            case 3:
                directionToWin = "WEST";
                break;
            default:
                directionToWin = "NORTH";
                break;
        }

        txt_instruction.setText("Face "+directionToWin+"!");
        //directionToWin = "NORTH";
         sensorEventListenerAccelrometer = new SensorEventListener() {
             private int run = 0;

            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                imageView.setRotation((float) (floatOrientation[0]*180/3.14159));

                rotationDeg = (float) (floatOrientation[0]*180/3.14159);

                if (rotationDeg < 10 && rotationDeg>-10){
                    direction = "NORTH";
                }else if (rotationDeg >80 && rotationDeg < 100){
                    direction = "EAST";
                }else if (rotationDeg < -80 && rotationDeg > -100){
                    direction = "WEST";
                }else if (rotationDeg > 170 || rotationDeg < -170){
                    direction = "SOUTH";
                }else{
                    direction = "NOT A CARDINAL DIRECTION";
                }

                Log.e("TEST DIR", "direction: "+direction);
                txt_direction.setText("Facing "+direction);
                Log.e("ROTATION", String.valueOf(-floatOrientation[0]*180/3.14159));

                if (run >= 10){
                    if (direction.equals(directionToWin)){
                        //add points to this user
                        //move to next game/end
                        System.out.println("Success");
                        //sensorManager.unregisterListener(this);
                        directionFinish();
                        gameFinished(true);
                    }
                }else{
                    run++;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                imageView.setRotation((float) (floatOrientation[0]*180/3.14159));

                rotationDeg = (float) (floatOrientation[0]*180/3.14159);

                if (rotationDeg < 10 && rotationDeg>-10){
                    direction = "NORTH";
                }else if (rotationDeg >80 && rotationDeg < 100){
                    direction = "EAST";
                }else if (rotationDeg < -80 && rotationDeg > -100){
                    direction = "WEST";
                }else if (rotationDeg > 170 || rotationDeg < -170){
                    direction = "SOUTH";
                }else{
                    direction = "NOT A CARDINAL DIRECTION";
                }

//                Log.e("TEST DIR", "direction: "+direction);
//                txt_direction.setText("Facing "+direction);
//                Log.e("ROTATION", String.valueOf(-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);

        this.startTime = System.currentTimeMillis();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {

            public void run() {
                Log.e(TAG, "Failed due to running out of time");
                directionFinish();
            }

        }, 10000);
    }

    public void directionFinish(){
        this.timer.cancel();
        sensorManager.unregisterListener(sensorEventListenerAccelrometer);
        sensorManager.unregisterListener(sensorEventListenerMagneticField);
    }
//    public void ResetButton(View view){
//        imageView.setRotation(180);
//    }
}