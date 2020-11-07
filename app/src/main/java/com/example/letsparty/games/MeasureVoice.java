package com.example.letsparty.games;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.letsparty.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class MeasureVoice extends Game {

    MediaRecorder recorder = new MediaRecorder();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_voice);
        listen();

        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {

            public void run()
            {
                end();
            }

        }, super.getDelay());
    }

    public void listen()
    {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");

        try {
            recorder.prepare();
            recorder.start();
            recorder.getMaxAmplitude();
            System.out.println("Start Recording");
        }catch(IOException e){
            e.printStackTrace();
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    public void end()
    {
        int maxAmp = recorder.getMaxAmplitude();
        recorder.stop();
        System.out.println("**********AMP is: " + maxAmp);
        recorder.reset();
        recorder.release();
        System.out.println("recorded");
        gameFinished(true);
    }
}