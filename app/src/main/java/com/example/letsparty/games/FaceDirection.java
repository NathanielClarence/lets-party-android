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

public class FaceDirection extends Game {

    private ImageView imageView;
    private TextView txt_direction;
    private String direction;
    private float rotationDeg;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        txt_direction = findViewById(R.id.textView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Random random = new Random();
        int rand = random.nextInt(4);
        String faceThis;
        switch (rand){
            case 0:
                faceThis = "NORTH";
                break;
            case 1:
                faceThis = "EAST";
                break;
            case 2:
                faceThis = "SOUTH";
                break;
            case 3:
                faceThis = "WEST";
                break;
            default:
                faceThis = "NORTH";
                break;
        }

        SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
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


                if (direction.equals(faceThis)){
                    //add points to this user
                    //move to next game/end
                    System.out.println("Success");
                    gameFinished();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
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

                Log.e("TEST DIR", "direction: "+direction);
                txt_direction.setText("Facing "+direction);
                Log.e("ROTATION", String.valueOf(-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelrometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void ResetButton(View view){
        imageView.setRotation(180);
    }
}