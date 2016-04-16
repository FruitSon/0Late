package zhenma.hackthon;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MonitorService extends Service {


    public MonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("monitor service start");
        autoCheck();
    }

    public void autoCheck(){


        CountDownTimer timer = new CountDownTimer(Globals.AUTO_CHECK_DURATION,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {

                System.out.println("autocheck test");

                SharedPreferences tempSP = getSharedPreferences("latestEvent", MODE_PRIVATE);
                String temp = tempSP.getString("1Time", "");
                long deltaTime = 0;

                //calculate waiting time
                if(temp!="") {
                    deltaTime = calculateWaitingTime(temp);
                    System.out.println("deltaTime:" + deltaTime);

                    //test
                    deltaTime = 119;
                    System.out.println("deltaTime Test:" + deltaTime);

                    if(deltaTime > 120) {
                        //restart timer
                        start();
                        System.out.println("check deltaTime:" + deltaTime);
                    }
                    else {
                        System.out.println("start polling");
                        startPolling(getApplicationContext(),Globals.POLLING_DURATION);
                        stopSelf();
                    }

                }
                else{
                    //// TODO: 4/15/16 update event list
                    deltaTime = calculateWaitingTime(temp);
                }


            }

        };

        timer.start();
    }

    public void startPolling(Context context, int duration) {
        Intent intent = new Intent(context, PollingService.class);
        PendingIntent pi = PendingIntent.getService(context, 10, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //RTC_WAKEUP, wake up the device when it goes off.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), duration, pi);
    }

    public long calculateWaitingTime(String temp){

        Date currentTime = Calendar.getInstance().getTime();
        Date eventTime = Calendar.getInstance().getTime();
        long delta = 0;

        String tempTime = temp.substring(0, 10) + " "+temp.substring(11, 19);

        //format time data into 24-hour system
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            eventTime = sdf.parse(tempTime);
            System.out.println("formatted time:"+eventTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //calculate the waiting time, in min
        delta = (eventTime.getTime() - currentTime.getTime()) / 1000 / 60;




        //date format transformation test
//        System.out.println("format test:\n" + "eventTime:" + eventTime + "\n" + "currentTime:" + currentTime);

        return delta;
    }

}
