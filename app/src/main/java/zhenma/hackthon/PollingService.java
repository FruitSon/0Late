package zhenma.hackthon;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("polling service is started");
        System.out.println("the xxx time for comparsions:" + (count++));
        compareTime();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            //// TODO: 4/15/16 update event list, event in sharedpreference
            //// TODO: 4/15/16 start notification
            System.out.println("alerrrrrrrrrrrrrrrrrrrrrrrt! tessssssssst! ");
            startService(new Intent(this, MonitorService.class));
            Globals.eventUnderTracking = false;
            //// TODO: 4/16/16  
            stopSelf();
        }
    }

    private long estimateTimeOnRoad(){
        long ET = 10;

        return ET;
    }

}
