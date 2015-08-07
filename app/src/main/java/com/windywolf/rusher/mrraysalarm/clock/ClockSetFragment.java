package com.windywolf.rusher.mrraysalarm.clock;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.windywolf.rusher.mrraysalarm.R;
import com.windywolf.rusher.mrraysalarm.bean.Alarm;
import com.windywolf.rusher.mrraysalarm.manager.DatabaseManager;
import com.windywolf.rusher.mrraysalarm.receiver.AlarmServiceReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Mr.Ray on 7/30/15.
 */
public class ClockSetFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    TimePicker tpTimer;
    Button btnSetMusic;
    Button btnSetClock;
    static ArrayList<String> musicPathList = new ArrayList<>();
    static ArrayList<String> musicNameList = new ArrayList<>();
    static ArrayList<String> musicTypeList = new ArrayList<>();
    MediaPlayer mediaPlayer;
    CheckBox[] checkBoxes = null;
    CheckBox checkAll = null;
    ArrayList<Alarm.Day> days = new ArrayList<>();
    String musicPath;
    String musicName;
    String musicType;
    int select = -1;
    final String TYPE_URL = "type_url";
    final String TYPE_MUSIC = "type_music";
    Alarm alarm = null;
    ProgressDialog progressDialog = null;
    private static boolean loadFinish = false;

    public static ClockSetFragment newInstance() {
        return new ClockSetFragment();
    }

    public ClockSetFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (alarm == null) {
            alarm = new Alarm();
            alarm.setId(-1);
        }
        if (!loadFinish) {
            new MusicLoad().execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_clock_layout, container, false);
        tpTimer = (TimePicker) view.findViewById(R.id.tp_timer);
        btnSetClock = (Button) view.findViewById(R.id.btn_setClock);
        btnSetClock.setOnClickListener(this);
        btnSetMusic = (Button) view.findViewById(R.id.btn_setMusic);
        btnSetMusic.setOnClickListener(this);
        checkBoxes = new CheckBox[7];
        checkBoxes[0] = (CheckBox) view.findViewById(R.id.cb_sun);
        checkBoxes[1] = (CheckBox) view.findViewById(R.id.cb_mon);
        checkBoxes[2] = (CheckBox) view.findViewById(R.id.cb_tue);
        checkBoxes[3] = (CheckBox) view.findViewById(R.id.cb_wed);
        checkBoxes[4] = (CheckBox) view.findViewById(R.id.cb_thur);
        checkBoxes[5] = (CheckBox) view.findViewById(R.id.cb_fri);
        checkBoxes[6] = (CheckBox) view.findViewById(R.id.cb_sat);
        checkAll = (CheckBox) view.findViewById(R.id.cb_all);
        int id = -1;
        if(getArguments() != null){
            id = getArguments().getInt(Alarm.ALARM_ID);
        }
        if (id != -1) {
            DatabaseManager manager = DatabaseManager.init(getActivity());
            alarm = manager.getAlarmById(id);
            manager.deactivate();
            musicPath = alarm.getMusic();
            musicName = alarm.getMusicName();
            musicType = alarm.getMusicType() ? TYPE_MUSIC : TYPE_URL;
            for(Alarm.Day d : alarm.getDays()){
                days.add(d);
            }
            tpTimer.setCurrentHour(alarm.getTime().get(Calendar.HOUR_OF_DAY));
            tpTimer.setCurrentMinute(alarm.getTime().get(Calendar.MINUTE));
            tpTimer.setEnabled(false);
            btnSetMusic.setText(musicName);
            int checks = 0;
            int i = 0;
            for(Alarm.Day d : Alarm.Day.values()){
                if (days.contains(d)){
                    checks++;
                    checkBoxes[i].setChecked(true);
                }
                i++;
            }
            if(checks == 7){
                checkAll.setChecked(true);
            }
        } else {
            days.add(Alarm.Day.SUNDAY);
            days.add(Alarm.Day.MONDAY);
            days.add(Alarm.Day.TUESDAY);
            days.add(Alarm.Day.WEDNESDAY);
            days.add(Alarm.Day.THURSDAY);
            days.add(Alarm.Day.FRIDAY);
            days.add(Alarm.Day.SATURDAY);
            for(CheckBox b : checkBoxes){
                b.setChecked(true);
            }
            checkAll.setChecked(true);
        }
        checkAll.setOnCheckedChangeListener(this);
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener(this);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void searchMusic(String path) {
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory() && f.getName().indexOf(".") != 0) {
                    searchMusic(f.getPath());
                }
                if (f.isFile()) {
                    String extension = f.getPath().substring(f.getPath().length() - 3);
                    if (extension.equals("wav") || extension.equals("wma") || extension.equals("ogg")) {
                        musicPathList.add(f.getAbsolutePath());
                        musicNameList.add(f.getName());
                        musicTypeList.add(TYPE_MUSIC);
                    }
                    if (extension.equals("mp3") && f.length() > 1000000) {
                        musicPathList.add(f.getAbsolutePath());
                        musicNameList.add(f.getName());
                        musicTypeList.add(TYPE_MUSIC);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // Set alarm time
            case R.id.btn_setClock:
                if (TextUtils.isEmpty(musicPath)) {
                    Toast.makeText(getActivity(), "请选择闹铃O(∩_∩)O", Toast.LENGTH_SHORT).show();
                } else if (days.size() == 0) {
                    Toast.makeText(getActivity(), "不恩能够一天都不选⊙﹏⊙", Toast.LENGTH_SHORT).show();
                } else {
                    // Save alarm info
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, tpTimer.getCurrentHour());
                    calendar.set(Calendar.MINUTE, tpTimer.getCurrentMinute());
                    calendar.set(Calendar.SECOND, 0);
                    // use CRT to be the name
                    String alarmName = String.valueOf(System.currentTimeMillis());
                    alarm.setName(alarmName);
                    alarm.setVibrate(true);
                    alarm.setMusic(musicPath);
                    alarm.setDays(days.toArray(new Alarm.Day[days.size()]));
                    alarm.setActive(true);
                    alarm.setMusicName(musicName);
                    alarm.setTime(calendar);
                    alarm.setMusicType(TextUtils.equals(musicType, TYPE_MUSIC));
                    DatabaseManager databaseManager = DatabaseManager.init(getActivity());
                    if(alarm.getId() != -1){
                        databaseManager.update(alarm);
                    } else {
                        databaseManager.insert(alarm);
                        alarm = databaseManager.getAlarmByName(alarmName);
                    }
                    databaseManager.deactivate();
                    Intent intent = new Intent(getActivity(), AlarmServiceReceiver.class);
                    intent.putExtra(Alarm.ALARM_ID, alarm.getId());
                    getActivity().sendBroadcast(intent);
                    Toast.makeText(getActivity(), alarm.getNextTimeMessage(), Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;

            // Set alarm music
            case R.id.btn_setMusic:
                if (!loadFinish) {
                    // show progress dialog
                    Toast.makeText(getActivity(), "加载时间可能很长哦~", Toast.LENGTH_SHORT).show();
                    progressDialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
                } else {
                    showDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        if (musicNameList == null || musicNameList.isEmpty()) {
            Toast.makeText(getActivity(), "没有发现可用的音乐o(>﹏<)o", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("选择铃声");
        if (musicNameList != null && !musicNameList.isEmpty()) {

            // get CharSequence from musicNameList for set single choice items
            CharSequence[] items = new CharSequence[musicNameList.size()];
            for (int i = 0; i < musicNameList.size(); i++) {
                items[i] = musicNameList.get(i);
            }

            // set dialog single choice items
            dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select = which;
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    } else {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                    }
                    try {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

                        // Set different music source
                        if (TextUtils.equals(musicTypeList.get(which), TYPE_MUSIC)) {
                            mediaPlayer.setDataSource(musicPathList.get(which));
                        } else if (TextUtils.equals(musicTypeList.get(which), TYPE_URL)) {
                            mediaPlayer.setDataSource(getActivity(), Uri.parse(musicPathList.get(which)));
                        } else {
                            mediaPlayer.release();
                            mediaPlayer = null;
                            return;
                        }
                        mediaPlayer.setLooping(false);
                        // It is important to finish prepare before start
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        // It is important to prepare before start
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        try {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                        } catch (Exception e1) {
                            if (mediaPlayer != null) {
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        }
                    }
                }

                // set positive button
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (select != -1) {
                        musicName = musicNameList.get(select);
                        musicPath = musicPathList.get(select);
                        musicType = musicTypeList.get(select);
                        btnSetMusic.setText(musicName);
                    }
                }

                // set negative button
            }).setNegativeButton("取消", null
                    // release media player
            ).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });

            // show dialog
            dialog.show();
        }
    }

    protected class MusicLoad extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (loadFinish) {
                return null;
            }
            RingtoneManager manager = new RingtoneManager(getActivity());
            manager.setType(RingtoneManager.TYPE_ALARM);
            Cursor c = manager.getCursor();
            if (c.moveToFirst()) {
                do {
                    musicNameList.add(manager.getRingtone(c.getPosition()).getTitle(getActivity()));
                    musicPathList.add(manager.getRingtoneUri(c.getPosition()).toString());
                    musicTypeList.add(TYPE_URL);
                } while (c.moveToNext());
            }
            searchMusic(Environment.getExternalStorageDirectory().getPath());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadFinish = true;
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog.cancel();
                progressDialog = null;
                showDialog();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_all:
                if (isChecked) {
                    for (CheckBox box : checkBoxes) {
                        box.setChecked(true);
                    }
                }
                break;
            case R.id.cb_sun:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.SUNDAY)) {
                        days.add(Alarm.Day.SUNDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.SUNDAY)) {
                        days.remove(Alarm.Day.SUNDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_mon:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.MONDAY)) {
                        days.add(Alarm.Day.MONDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.MONDAY)) {
                        days.remove(Alarm.Day.MONDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_tue:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.TUESDAY)) {
                        days.add(Alarm.Day.TUESDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.TUESDAY)) {
                        days.remove(Alarm.Day.TUESDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_wed:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.WEDNESDAY)) {
                        days.add(Alarm.Day.WEDNESDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.WEDNESDAY)) {
                        days.remove(Alarm.Day.WEDNESDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_thur:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.THURSDAY)) {
                        days.add(Alarm.Day.THURSDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.THURSDAY)) {
                        days.remove(Alarm.Day.THURSDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_fri:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.FRIDAY)) {
                        days.add(Alarm.Day.FRIDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.FRIDAY)) {
                        days.remove(Alarm.Day.FRIDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            case R.id.cb_sat:
                if (isChecked) {
                    if (!days.contains(Alarm.Day.SATURDAY)) {
                        days.add(Alarm.Day.SATURDAY);
                        if (days.size() == 7) {
                            checkAll.setChecked(true);
                        }
                    }
                } else {
                    if (days.contains(Alarm.Day.SATURDAY)) {
                        days.remove(Alarm.Day.SATURDAY);
                        checkAll.setChecked(false);
                    }
                }
                break;
            default:
                break;
        }
    }
}
