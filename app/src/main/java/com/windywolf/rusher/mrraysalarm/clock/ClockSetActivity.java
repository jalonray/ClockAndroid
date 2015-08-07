package com.windywolf.rusher.mrraysalarm.clock;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.windywolf.rusher.mrraysalarm.R;
import com.windywolf.rusher.mrraysalarm.bean.Alarm;

/**
 * Created by Mr.Ray on 15/8/6.
 */
public class ClockSetActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_set_activity);
        int id = getIntent().getIntExtra(Alarm.ALARM_ID, -1);
        ClockSetFragment fragment = ClockSetFragment.newInstance();
        if(id != -1){
            Bundle args = new Bundle();
            args.putInt(Alarm.ALARM_ID, id);
            fragment.setArguments(args);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.alarm_set_container, fragment).commit();
    }
}
