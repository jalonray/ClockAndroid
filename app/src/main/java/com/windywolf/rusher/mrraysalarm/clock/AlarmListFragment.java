package com.windywolf.rusher.mrraysalarm.clock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.windywolf.rusher.mrraysalarm.R;
import com.windywolf.rusher.mrraysalarm.bean.Alarm;
import com.windywolf.rusher.mrraysalarm.manager.DatabaseManager;
import com.windywolf.rusher.mrraysalarm.receiver.AlarmServiceReceiver;

import java.util.List;

/**
 * Created by Mr.Ray on 15/8/5.
 */
public class AlarmListFragment extends Fragment implements View.OnClickListener{

    ListView listView = null;
    AlarmAdapter alarmAdapter = null;
    Button alarmAdd = null;
    TextView tvNoAlarm = null;

    public static AlarmListFragment newInstance() {
        return new AlarmListFragment();
    }

    public AlarmListFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_list_layout, container, false);
        alarmAdd = (Button) view.findViewById(R.id.ib_add);
        alarmAdd.setOnClickListener(this);
        listView = (ListView) view.findViewById(R.id.lv_alarm);
        listView.setDividerHeight(2);
        tvNoAlarm = (TextView) view.findViewById(R.id.tv_no_alarm);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DatabaseManager manager = DatabaseManager.init(getActivity());
        List<Alarm> alarmList = manager.getAll();
        manager.deactivate();
        if (alarmList != null && alarmList.size() != 0) {
            tvNoAlarm.setVisibility(View.GONE);
        }
        alarmAdapter = new AlarmAdapter(getActivity(), alarmList);
        listView.setAdapter(alarmAdapter);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), ClockSetActivity.class);
        getActivity().startActivity(intent);
    }

    public class AlarmAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Context context;
        List<Alarm> alarmList;

        public AlarmAdapter(Context context, List<Alarm> list) {
            this.context = context;
            alarmList = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (alarmList != null) {
                return alarmList.size();
            }
            return 0;
        }

        @Override
        public Alarm getItem(int position) {
            if (alarmList != null) {
                return alarmList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            AlarmHolder holder;
            if (convertView == null) {
                holder = new AlarmHolder();
                convertView = inflater.inflate(R.layout.alarm_list_item, parent, false);
                holder.alarmTime = (TextView) convertView.findViewById(R.id.tv_alarm_time);
                holder.active = (Switch) convertView.findViewById(R.id.switch_active);
                holder.ibDelete = (ImageButton) convertView.findViewById(R.id.ib_delete);
                holder.dayOfWeek = (TextView) convertView.findViewById(R.id.tv_day_of_week);
                convertView.setTag(holder);
            }
            holder = (AlarmHolder) convertView.getTag();
            final Alarm alarm = getItem(position);
            holder.alarmTime.setText(alarm.getTimeString());
            holder.active.setChecked(alarm.getActive());
            holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (alarm.getActive() != isChecked) {
                        alarm.setActive(isChecked);
                        DatabaseManager manager = DatabaseManager.init(context);
                        manager.update(alarm);
                        manager.deactivate();
                        Intent intent = new Intent(context, AlarmServiceReceiver.class);
                        intent.putExtra(Alarm.ALARM_ID, alarm.getId());
                        context.sendBroadcast(intent);
                    }
                }
            });
            holder.dayOfWeek.setText(alarm.getRepeatDaysString());
            holder.ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("确认删除？");
                    builder.setMessage("删除后此闹铃将无法继续为您服务(>_<)");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete from database
                            DatabaseManager manager = DatabaseManager.init(context);
                            manager.deleteEntry(alarm);
                            manager.deactivate();
                            // Delete from list
                            alarmList.remove(position);
                            if (alarmList.size() == 0) {
                                tvNoAlarm.setVisibility(View.VISIBLE);
                            }
                            alarmAdapter.notifyDataSetChanged();
                            // Cancel alarm manager
                            Intent intent = new Intent(context, AlarmServiceReceiver.class);
                            intent.putExtra(Alarm.ALARM_ID, alarm.getId());
                            context.sendBroadcast(intent);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Alarm alarm = alarmAdapter.getItem(position);
                    if (alarm != null) {
                        Intent intent = new Intent(getActivity(), ClockSetActivity.class);
                        intent.putExtra(Alarm.ALARM_ID, alarm.getId());
                        getActivity().startActivity(intent);
                    }
                }
            });
            return convertView;
        }
    }

    static class AlarmHolder {
        TextView alarmTime = null;
        Switch active = null;
        ImageButton ibDelete = null;
        TextView dayOfWeek = null;
    }
}
