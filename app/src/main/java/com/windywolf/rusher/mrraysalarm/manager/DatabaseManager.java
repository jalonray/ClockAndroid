package com.windywolf.rusher.mrraysalarm.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.windywolf.rusher.mrraysalarm.bean.Alarm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Mr.Ray on 15/8/3.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    static DatabaseManager instance = null;
    static SQLiteDatabase database = null;

    static final String DATABASE_NAME = "MR_RAY_ALARM";
    static final int DATABASE_VERSION = 1;

    public static final String ALARM_TABLE = "alarm";
    public static final String COLUMN_ALARM_ID = "alarm_id";
    public static final String COLUMN_ALARM_ACTIVE = "alarm_active";
    public static final String COLUMN_ALARM_TIME = "alarm_time";
    public static final String COLUMN_ALARM_DAYS = "alarm_days";
    public static final String COLUMN_ALARM_MUSIC = "alarm_music";
    public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
    public static final String COLUMN_ALARM_NAME = "alarm_name";
    public static final String COLUMN_ALARM_MUSICTYPE = "alarm_musictype";
    public static final String COLUMN_ALARM_MUSICNAME = "alarm_musicname";

    public static DatabaseManager init(Context context) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    public static SQLiteDatabase getDatabase() {
        if (null == database) {
            database = instance.getWritableDatabase();
        }
        return database;
    }

    public void deactivate() {
        if (null != database && database.isOpen()) {
            database.close();
        }
        database = null;
        instance = null;
    }

    DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_MUSIC + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL, "
                + COLUMN_ALARM_MUSICTYPE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_MUSICNAME + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        onCreate(db);
    }

    public void dropTable() {
        getDatabase().execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
    }

    public void createTable() {
        getDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_MUSIC + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL, "
                + COLUMN_ALARM_MUSICTYPE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_MUSICNAME + " TEXT NOT NULL)");
    }

    public long insert(Alarm alarm) {
        ContentValues values = new ContentValues();
//        values.put(COLUMN_ALARM_ID, alarm.getId());
        values.put(COLUMN_ALARM_ACTIVE, alarm.getActive());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            values.put(COLUMN_ALARM_DAYS, buff);
        } catch (Exception e) {

        }
        values.put(COLUMN_ALARM_MUSIC, alarm.getMusic());
        values.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        values.put(COLUMN_ALARM_NAME, alarm.getName());
        values.put(COLUMN_ALARM_TIME, alarm.getTimeString());
        values.put(COLUMN_ALARM_MUSICTYPE, alarm.getMusicType());
        values.put(COLUMN_ALARM_MUSICNAME, alarm.getMusicName());

        return getDatabase().insert(ALARM_TABLE, null, values);
    }

    public int update(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALARM_MUSIC, alarm.getMusic());
        values.put(COLUMN_ALARM_TIME, alarm.getTimeString());
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            values.put(COLUMN_ALARM_DAYS, buff);
        } catch (Exception e) {

        }
        values.put(COLUMN_ALARM_NAME, alarm.getName());
        values.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        values.put(COLUMN_ALARM_ACTIVE, alarm.getActive());
        values.put(COLUMN_ALARM_MUSICTYPE, alarm.getMusicType());
        values.put(COLUMN_ALARM_MUSICNAME, alarm.getMusicName());
        return getDatabase().update(ALARM_TABLE, values, COLUMN_ALARM_ID + "=" + alarm.getId(), null);
    }

    // The name of alarm is unique--the time it created
    public Alarm getAlarmByName(String name) {
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_MUSIC,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_MUSICTYPE,
                COLUMN_ALARM_MUSICNAME
        };
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_NAME + "=" + name,
                null, null, null, null);
        Alarm alarm = null;
        if (c.moveToFirst()) {
            alarm = new Alarm();
            alarm.setId(c.getInt(0));
            alarm.setActive(c.getInt(1) == 1);
            alarm.setTime(c.getString(2));
            byte[] repeatDaysBytes = c.getBlob(3);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(repeatDaysBytes);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                Alarm.Day[] repeatDays;
                Object object = objectInputStream.readObject();
                if (object instanceof Alarm.Day[]) {
                    repeatDays = (Alarm.Day[]) object;
                    alarm.setDays(repeatDays);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            alarm.setMusic(c.getString(4));
            alarm.setVibrate(c.getInt(5) == 1);
            alarm.setName(c.getString(6));
            alarm.setMusicType(c.getInt(7) == 1);
            alarm.setMusicName(c.getString(8));
        }
        c.close();
        return alarm;
    }

    public int deleteEntry(Alarm alarm) {
        return deleteEntry(alarm.getId());
    }

    public int deleteEntry(int id) {
        return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
    }

    public int deleteAll() {
        return getDatabase().delete(ALARM_TABLE, "1", null);
    }

    public Alarm getAlarmById(int id) {
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_MUSIC,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_MUSICTYPE,
                COLUMN_ALARM_MUSICNAME
        };
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID + "=" + id,
                null, null, null, null);
        Alarm alarm = null;
        if (c.moveToFirst()) {
            alarm = new Alarm();
            alarm.setId(c.getInt(0));
            alarm.setActive(c.getInt(1) == 1);
            alarm.setTime(c.getString(2));
            byte[] repeatDaysBytes = c.getBlob(3);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(repeatDaysBytes);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                Alarm.Day[] repeatDays;
                Object object = objectInputStream.readObject();
                if (object instanceof Alarm.Day[]) {
                    repeatDays = (Alarm.Day[]) object;
                    alarm.setDays(repeatDays);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            alarm.setMusic(c.getString(4));
            alarm.setVibrate(c.getInt(5) == 1);
            alarm.setName(c.getString(6));
            alarm.setMusicType(c.getInt(7) == 1);
            alarm.setMusicName(c.getString(8));
        }
        c.close();
        return alarm;
    }

    public List<Alarm> getAll() {
        List<Alarm> alarmList = new ArrayList<>();
        Cursor cursor = getCursor();
        if (cursor.moveToFirst()) {
//            COLUMN_ALARM_ID
//                    COLUMN_ALARM_ACTIVE
//            COLUMN_ALARM_TIME
//                    COLUMN_ALARM_DAYS
//            COLUMN_ALARM_MUSIC
//                    COLUMN_ALARM_VIBRATE
//            COLUMN_ALARM_NAME
            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getInt(0));
                alarm.setActive(cursor.getInt(1) == 1);
                alarm.setTime(cursor.getString(2));
                byte[] buff = cursor.getBlob(3);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(buff);
                try {
                    ObjectInputStream objectInputString = new ObjectInputStream(inputStream);
                    Alarm.Day[] repeatDays;
                    Object object = objectInputString.readObject();
                    if (object instanceof Alarm.Day[]) {
                        repeatDays = (Alarm.Day[]) object;
                        alarm.setDays(repeatDays);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                alarm.setMusic(cursor.getString(4));
                alarm.setVibrate(cursor.getInt(5) == 1);
                alarm.setName(cursor.getString(6));
                alarm.setMusicType(cursor.getInt(7) == 1);
                alarm.setMusicName(cursor.getString(8));
                alarmList.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Sort alarms by time
        int size = alarmList.size();
        Alarm[] alarms = new Alarm[size];
        for (int i = 0; i < size; i++) {
            alarms[i] = alarmList.get(i);
        }
        Arrays.sort(alarms, new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                return lhs.getTimeString().compareTo(rhs.getTimeString());
            }
        });
        alarmList.clear();
        for (int i = 0; i < size; i++) {
            alarmList.add(alarms[i]);
        }
        return alarmList;
    }

    public Cursor getCursor() {
        String[] columns = new String[]{
                COLUMN_ALARM_ID,
                COLUMN_ALARM_ACTIVE,
                COLUMN_ALARM_TIME,
                COLUMN_ALARM_DAYS,
                COLUMN_ALARM_MUSIC,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_NAME,
                COLUMN_ALARM_MUSICTYPE,
                COLUMN_ALARM_MUSICNAME
        };
        return getDatabase().query(ALARM_TABLE, columns, null,
                null, null, null, null);
    }
}
