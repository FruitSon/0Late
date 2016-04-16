package zhenma.hackthon;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PollingService extends Service {
    public PollingService() {
    }

    int count = 0;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        System.out.println("polling service is started");
//        System.out.println("the xxx time for comparsions:" + (count++));
//        compareTime();
//
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("polling service is started");
        System.out.println("the xxx time for comparsions:"+(count++));
        compareTime();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void compareTime(){
        //estimate time
        SharedPreferences sp3 = getSharedPreferences("latestEvent", MODE_PRIVATE);
        String temp = sp3.getString("1Time", "");

        String tempTime = temp.substring(0, 10) + " "+temp.substring(11, 19);

        Date currentTime = Calendar.getInstance().getTime();
        Date eventTime = Calendar.getInstance().getTime();

        //format time data into 24-hour system
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            eventTime = sdf.parse(tempTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //calculate the remaining time, in min
        long remainTime = 0;
        remainTime = (eventTime.getTime() - currentTime.getTime()) / 1000 / 60;
        long travelTime = estimateTimeOnRoad();

        //test
        System.out.println("TEST: travel time:" +travelTime);
        System.out.println("TEST: remain time:" +remainTime);


        if(travelTime >= remainTime){
            //get Event title
            SharedPreferences sp2 = getSharedPreferences("latestEvent", MODE_PRIVATE);
            String event = sp2.getString("1Event","");

            //send notification
            System.out.println("alerrrrrrrrrrrrrrrrrrrrrrrt! tessssssssst! ");
            Intent notify = new Intent(this,NotifyService.class);
            Bundle notificationData = new Bundle();
            notificationData.putString("event", event);
            notificationData.putLong("time", travelTime);
            notify.putExtras(notificationData);

            startService(notify);

            startService(new Intent(this, MonitorService.class));
            AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(getApplicationContext(), PollingService.class);
            PendingIntent pi2 = PendingIntent.getService(getApplicationContext(), 10, i,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmMgr.cancel(pi2);

            // TODO: 4/15/16 update event list, event in sharedpreference

            stopSelf();
        }
    }

    private long estimateTimeOnRoad(){
        long ET = 10;

        return ET;
    }

}
