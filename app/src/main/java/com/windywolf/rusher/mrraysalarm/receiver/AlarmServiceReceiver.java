package com.windywolf.rusher.mrraysalarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.windywolf.rusher.mrraysalarm.bean.Alarm;
import com.windywolf.rusher.mrraysalarm.service.AlarmService;

/**
 * Created by Mr.Ray on 15/8/5.
 */
public class AlarmServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        int id = intent.getIntExtra(Alarm.ALARM_ID, -1);
        if(id != -1){
            serviceIntent.putExtra(Alarm.ALARM_ID, id);
        }
        context.startService(serviceIntent);
    }
}
