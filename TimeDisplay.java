package org.ecs160.a2;

import com.codename1.ui.Button;

import java.util.Calendar;

public class TimeDisplay extends Button {
    int seconds;
    int minutes;
    int hours;
    int days;
    public String timeStamp;
    int lastSecond;
    boolean timerRunning;
    String verbose;
    String taskName;

    public TimeDisplay(int timing, String tName) {
        updateTimeStamp(timing);
        setTimeString();
        Calendar curTime = Calendar.getInstance();
        lastSecond = curTime.get(Calendar.SECOND);
        timerRunning =  false;
        setText(timeStamp);
        taskName = tName;
        verboseString();
    }

    public void start() {
        timerRunning = true;
        getAllStyles().setFgColor(0x00CD00);
    }

    public void stop() {
        timerRunning = false;
        getAllStyles().setFgColor(0x752c29);
    }

    public boolean animate() {
        if (timePassed()) {
            secondPassed();
            setText(timeStamp);
            return true;
        }

        return false;
    }

    public String getVerbose() { return verbose; }

    public void updateTimeStamp(int runTime) {
        seconds = (runTime%3600)%60;
        minutes = (runTime%3600)/60;
        hours = (runTime/3600);
        days = hours/24;
        hours = hours-(days*24);

        setTimeString();
        Calendar curTime = Calendar.getInstance();
        lastSecond = curTime.get(Calendar.SECOND);
        setText(timeStamp);
    }

    private boolean timePassed() {
        Calendar curTime = Calendar.getInstance();
        int  curSec = curTime.get(Calendar.SECOND);
        if (curSec != lastSecond) {
            lastSecond = curSec;
            return true;
        }
        else {return false;}
    }

    private void secondPassed() {
        seconds++;
        if (seconds == 60) {
            seconds = 0;
            minutes++;
        }
        if (minutes == 60) {
            minutes = 0;
            hours++;
        }
        if (hours == 24) {
            hours = 0;
            days++;
        }
        verboseString();
        setTimeString();
    }

    private void setTimeString() {
        if (days > 0)
        {
            timeStamp = days + ":" + pad(hours);
        }
        else if (hours > 0) {
            timeStamp = pad(hours) + ":" + pad(minutes);
        }
        else {
            timeStamp = pad(minutes) + ":" + pad(seconds);
        }
    }

    private void verboseString() {
        String sDays = "D:" + pad(days);
        String sHours = " - H:" + pad(hours);
        String sMins = " - M:" + pad(minutes);
        String sSecs = " - S:" + pad(seconds);
        verbose = sDays + sHours + sMins + sSecs;
    }

    private String pad(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }
}