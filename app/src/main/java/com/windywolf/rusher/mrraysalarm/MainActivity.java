package com.windywolf.rusher.mrraysalarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.windywolf.rusher.mrraysalarm.clock.AlarmListFragment;
import com.windywolf.rusher.mrraysalarm.clock.ClockSetFragment;


public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null){
            AlarmListFragment alarmListFragment = AlarmListFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.pager_container, alarmListFragment).commit();
        }
    }
}
