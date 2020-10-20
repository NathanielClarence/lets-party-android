package com.example.letsparty.games;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.EditText;

import com.example.letsparty.R;
import com.example.letsparty.GameUtil;

import java.io.IOException;

public class MeasureVoice extends Game {

    MediaRecorder recorder = new MediaRecorder();
    private double soundDecibel;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    private EditText soundLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_voice);

        soundLevel = (EditText) findViewById(R.id.txt_sound_level);
    }

    public void start(View v) {
        Button btn = (Button) v;
        String tag = (String) btn.getTag();
        if (recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            try {
                recorder.prepare();
                recorder.start();
            }catch(Exception e){
                System.out.println(e);
                android.util.Log.e("Exception: ", "Exception: "+e);
            }
        }
    }

    public void onButtonClicked(View v) {
        Button btn = (Button) v;
        String tag = (String) btn.getTag();

        if("rec".equals(tag)){
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            try {
                recorder.prepare();
                recorder.start();
                System.out.println("Start Recording");
            }catch(IOException e){
                System.out.println(e);
                android.util.Log.e("", "IO Exception: "+e);
            }catch(SecurityException e){
                System.out.println(e);
                android.util.Log.e("", "Security Exception: "+e);
            }
            System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        }

        if("stop".equals(tag)){
            recorder.stop();
            recorder.release();

            soundDecibel = EMA_FILTER * recorder.getMaxAmplitude() + (1.0 - EMA_FILTER) * mEMA;
            soundLevel.setText(Double.toString(soundDecibel)+" dB");
            System.out.println(soundDecibel);
            recorder.reset();
            //add points/success/failed prompt here
            System.out.println("recorded");
        }
    }
}