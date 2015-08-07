package com.windywolf.rusher.mrraysalarm.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.windywolf.rusher.mrraysalarm.bean.Alarm;
import com.windywolf.rusher.mrraysalarm.manager.DatabaseManager;
import com.windywolf.rusher.mrraysalarm.receiver.AlarmingReceiver;

import java.util.List;

/**
 * Created by Mr.Ray on 7/30/15.
 */
public class AlarmService extends Service {

    Alarm alarm = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get alarm info
        String name = intent.getStringExtra(Alarm.ALARM_NAME);
        int id = intent.getIntExtra(Alarm.ALARM_ID, -1);
        DatabaseManager databaseManager = DatabaseManager.init(getApplicationContext());
        if (!TextUtils.isEmpty(name)) {
            alarm = databaseManager.getAlarmByName(name);
        } else if (id != -1) {
            DatabaseManager.init(getApplicationContext());
            alarm = databaseManager.getAlarmById(id);
        } else {
            databaseManager.deactivate();
        }
        // if not specific alarm, start all alarms
        if (null == alarm) {
            if (databaseManager == null) {
                databaseManager = DatabaseManager.init(getApplicationContext());
            }
            List<Alarm> list = databaseManager.getAll();
            AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
            for (Alarm alarmItem : list) {
                if (alarmItem.getActive()) {
                    Intent alarmingIntent = new Intent(getApplicationContext(), AlarmingReceiver.class);
                    alarmingIntent.putExtra(Alarm.ALARM_ID, alarmItem.getId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmItem.getId(), alarmingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmItem.getTime().getTimeInMillis(), pendingIntent);
                }
            }

            // start specific alarm
        } else {
            if (alarm.getActive()) {
                Intent alarmingIntent = new Intent(getApplicationContext(), AlarmingReceiver.class);
                alarmingIntent.putExtra(Alarm.ALARM_ID, alarm.getId());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarm.getId(), alarmingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTime().getTimeInMillis(), pendingIntent);

                // stop specific alarm
            } else {
                Intent alarmingIntent = new Intent(getApplicationContext(), AlarmingReceiver.class);
                alarmingIntent.putExtra(Alarm.ALARM_ID, alarm.getId());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarm.getId(), alarmingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
        }
        if(databaseManager != null){
            databaseManager.deactivate();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
