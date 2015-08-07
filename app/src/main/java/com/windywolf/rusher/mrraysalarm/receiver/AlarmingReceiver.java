package com.windywolf.rusher.mrraysalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.windywolf.rusher.mrraysalarm.alarming.AlarmingActivity;
import com.windywolf.rusher.mrraysalarm.bean.Alarm;

/**
 * Created by Mr.Ray on 15/8/4.
 */
public class AlarmingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(Alarm.ALARM_ID, -1);
        if (id != -1) {
            Intent alarmServiceIntent = new Intent(context, AlarmServiceReceiver.class);
            alarmServiceIntent.putExtra(Alarm.ALARM_ID, id);
            context.sendBroadcast(alarmServiceIntent);

            Intent alarmActivityIntent = new Intent(context, AlarmingActivity.class);
            alarmActivityIntent.putExtra(Alarm.ALARM_ID, id);
            alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmActivityIntent);
        }
    }
}
