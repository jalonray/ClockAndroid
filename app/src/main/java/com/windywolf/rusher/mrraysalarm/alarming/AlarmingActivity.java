package com.windywolf.rusher.mrraysalarm.alarming;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.windywolf.rusher.mrraysalarm.R;
import com.windywolf.rusher.mrraysalarm.bean.Alarm;
import com.windywolf.rusher.mrraysalarm.manager.DatabaseManager;

import java.util.Calendar;

/**
 * Created by Mr.Ray on 15/8/4.
 */
public class AlarmingActivity extends Activity {
    Button btnStop = null;
    MediaPlayer mediaPlayer = null;
    Alarm alarm = null;
    //    int[] allColors = null;
    public boolean isAlarmed = false;
    Vibrator v = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarming_layout);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Calendar calendar = Calendar.getInstance();
        String hour = calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + calendar.get(Calendar.HOUR_OF_DAY) : String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = calendar.get(Calendar.MINUTE) < 10 ? "0" + calendar.get(Calendar.MINUTE) : String.valueOf(calendar.get(Calendar.MINUTE));
        btnStop.setText(hour + ":" + minute);

        int id = getIntent().getIntExtra(Alarm.ALARM_ID, -1);
        if (id != -1) {
            DatabaseManager manager = DatabaseManager.init(this);
            alarm = manager.getAlarmById(id);
        }
//        try {
//            Field[] fields = Class.forName(getPackageName() + ".R$color").getDeclaredFields();
//            allColors = new int[fields.length];
//            for (int i = 0; i < fields.length; i++) {
//                allColors[i] = getResources().getColor(fields[i].getInt(null));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isAlarmed) {
            return;
        }
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                if (alarm.getMusicType()) {
                    mediaPlayer.setDataSource(alarm.getMusic());
                } else {
                    mediaPlayer.setDataSource(this, Uri.parse(alarm.getMusic()));
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        isAlarmed = true;
                    }
                });
                mediaPlayer.prepare();
            }
        } catch (Exception e) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v.hasVibrator()) {
            long[] patterns = new long[]{0, 100, 500, 600, 700, 800, 900, 1000};
            v.vibrate(patterns, 0);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (v != null) {
            v.cancel();
        }
        super.finish();
    }
}