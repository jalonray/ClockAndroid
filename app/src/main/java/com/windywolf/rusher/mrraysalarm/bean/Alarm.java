package com.windywolf.rusher.mrraysalarm.bean;

import android.media.RingtoneManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mr.Ray on 15/8/3.
 */
public class Alarm {


    public enum Day {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY;

        @Override
        public String toString() {
            switch (this.ordinal()) {
                case 0:
                    return "Sunday";
                case 1:
                    return "Monday";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Wednesday";
                case 4:
                    return "Thursday";
                case 5:
                    return "Friday";
                case 6:
                    return "Saturday";
            }
            return super.toString();
        }
    }

    private int id;
    private boolean active = true;
    private Calendar time = Calendar.getInstance();
    private Day[] days = {Day.SUNDAY, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY};
    private String music;
    private boolean vibrate = true;
    private String name = "Clock";
    private boolean musicType = true;
    private String musicName;
    public static String ALARM_ID = "alarm_id";
    public static String ALARM_NAME = "alarm_name";
    public static long MILLIS_OF_SECOND = 1000;
    public static long MILLIS_OF_MINUTE = MILLIS_OF_SECOND * 60;
    public static long MILLIS_OF_HOUR = MILLIS_OF_MINUTE * 60;
    public static long MILLIS_OF_DAY = MILLIS_OF_HOUR * 24;

    public Alarm() {

    }

    public int getId() {
        return id;
    }

    public boolean getActive() {
        return active;
    }

    public String getTimeString() {
        StringBuilder builder = new StringBuilder();
        if (time.get(Calendar.HOUR_OF_DAY) <= 9) {
            builder.append("0");
        }
        builder.append(time.get(Calendar.HOUR_OF_DAY));
        builder.append(':');
        if (time.get(Calendar.MINUTE) <= 9) {
            builder.append("0");
        }
        builder.append(time.get(Calendar.MINUTE));
        return builder.toString();
    }

    public Calendar getTime() {
        if (time.before(Calendar.getInstance())) {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }
        while (!Arrays.asList(getDays()).contains(Day.values()[time.get(Calendar.DAY_OF_WEEK) - 1])) {
            time.add(Calendar.DAY_OF_WEEK, 1);
        }
        return time;
    }

    public String getMusicName(){
        return musicName;
    }

    public boolean getMusicType() {
        return musicType;
    }

    public String getMusic() {
        return music;
    }

    public boolean getVibrate() {
        return vibrate;
    }

    public String getName() {
        return name;
    }

    public Day[] getDays() {
        return days;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTime(Calendar calendar) {
        this.time = calendar;
    }


    public void setMusicName(String musicName){
        this.musicName = musicName;
    }

    public void setTime(String time) {
        String[] timePieces = time.split(":");
        Calendar newTime = Calendar.getInstance();
        newTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]));
        newTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
        newTime.set(Calendar.SECOND, 0);
        setTime(newTime);
    }

    public void setDays(Day[] days) {
        this.days = days;
    }

    public void addDay(Day day) {
        boolean contains = false;
        for (Day d : getDays()) {
            if (d.equals(day)) {
                contains = true;
            }
        }
        if (!contains) {
            List<Day> result = new LinkedList<>();
            for (Day d : getDays())
                result.add(d);
            result.add(day);
            setDays(result.toArray(new Day[result.size()]));
        }
    }

    public void removeDay(Day day) {
        List<Day> result = new LinkedList<>();
        for (Day d : getDays()) {
            if (!d.equals(day))
                result.add(d);
            setDays(result.toArray(new Day[result.size()]));
        }
    }

    public void setMusicType(boolean type) {
        this.musicType = type;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepeatDaysString() {
        StringBuilder daysStringBuilder = new StringBuilder();
        if (getDays().length == Day.values().length) {
            daysStringBuilder.append("Every Day");
        } else {
            Arrays.sort(getDays(), new Comparator<Day>() {
                @Override
                public int compare(Day lhs, Day rhs) {
                    return lhs.ordinal() - rhs.ordinal();
                }
            });
            for (Day d : getDays()) {
                daysStringBuilder.append(getChineseDayOfWeed(d.toString()));
                daysStringBuilder.append(", ");
            }
            if(daysStringBuilder.length() >= 2) {
                daysStringBuilder.setLength(daysStringBuilder.length() - 2);
            }
        }
        return daysStringBuilder.toString();
    }

    public String getNextTimeMessage() {
        long timeDiff = getTime().getTimeInMillis() - System.currentTimeMillis();
        long days = timeDiff / MILLIS_OF_DAY;
        long hours = (timeDiff - days * MILLIS_OF_DAY) / MILLIS_OF_HOUR;
        long minutes = (timeDiff - days * MILLIS_OF_DAY - hours * MILLIS_OF_HOUR) / MILLIS_OF_MINUTE;
        long seconds = (timeDiff - days * MILLIS_OF_DAY - hours * MILLIS_OF_HOUR - minutes * MILLIS_OF_MINUTE) / MILLIS_OF_SECOND;
        StringBuilder builder = new StringBuilder();
        builder.append("下次闹铃将于");
        if (days > 0) {
            builder.append(days + "天");
        }
        if (hours > 0) {
            builder.append(hours + "小时");
        }
        if (minutes > 0) {
            builder.append(minutes + "分");
        }
        if (seconds > 0) {
            builder.append(seconds + "秒");
        }
        builder.append("后响起");
        return builder.toString();
    }

    private String getChineseDayOfWeed(String day){
        switch (day){
            case "Sunday":
                return "Sun";
            case "Monday":
                return "Mon";
            case "Tuesday":
                return "Tue";
            case "Wednesday":
                return "Wed";
            case "Thursday":
                return "Thur";
            case "Friday":
                return "Fri";
            case "Saturday":
                return "Sat";
            default:
                return "";
        }
    }
}
