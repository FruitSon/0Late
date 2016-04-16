package zhenma.hackthon;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class PollingService extends Service {

    String res = "-1";

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
        System.out.println("the xxx time for comparsions:" + (count++));
        compareTime();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void compareTime() {
        //estimate time
        SharedPreferences sp3 = getSharedPreferences("latestEvent", MODE_PRIVATE);
        String temp = sp3.getString("1Time", "");

        String tempTime = temp.substring(0, 10) + " " + temp.substring(11, 19);

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
        int remainTime = 0;
        remainTime = (int) (eventTime.getTime() - currentTime.getTime()) / 1000 / 60;
        int travelTime = formatTime(estimateTimeOnRoad());

        //test
        System.out.println("TEST: travel time:" + travelTime);
        System.out.println("TEST: remain time:" + remainTime);

        if(!res.equals("-1")) {
            if (travelTime >= remainTime) {

                //send notification
                System.out.println("alerrrrrrrrrrrrrrrrrrrrrrrt! tessssssssst! ");
                Intent notify = new Intent(this, NotifyService.class);
                Bundle notificationData = new Bundle();
                notificationData.putString("event", Globals.FIRST_EVENT);
                notificationData.putLong("time", remainTime);
                notify.putExtras(notificationData);

                startService(notify);

                startService(new Intent(this, MonitorService.class));
                AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(getApplicationContext(), PollingService.class);
                PendingIntent pi2 = PendingIntent.getService(getApplicationContext(), 10, i,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                alarmMgr.cancel(pi2);


                stopSelf();
            }
        }
    }

    private String estimateTimeOnRoad() {
        Location curLocation = null;

        GoogleApiClient mGoogleApiClientLoc2 = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "0";
        }
        curLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClientLoc2);
        myThread mThread;

        //Thread for google map
        if(curLocation!=null) {
            mThread = new myThread(curLocation.getLatitude() + "," + curLocation.getLongitude(),
                    Globals.FIRST_LOCATION, new DataBaseHelper(getApplicationContext()).getTransport(Globals.FIRST_ID));

        }else{
            mThread = new myThread("",
                Globals.FIRST_LOCATION, new DataBaseHelper(getApplicationContext()).getTransport(Globals.FIRST_ID));

        }
        Handler handler = new h();
        mThread.setHandler(handler);
        mThread.start();
        return res;
    }

    class h extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String xml=(String)msg.obj;
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db=factory.newDocumentBuilder();
                Document doc=db.parse(new ByteArrayInputStream(xml.getBytes()));

                Element root=doc.getDocumentElement();

                NodeList nodelist = root.getElementsByTagName("duration");
                if (nodelist != null) {
                    Element element = (Element)nodelist.item(0);
                    res = element.getElementsByTagName("text").item(0).getFirstChild().getNodeValue();
                }
                System.out.println("Get the result");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public int formatTime(String t) {
        String []res = t.split("\\s+");
        int time = 0;
        if (res.length > 2) {
            time += Integer.parseInt(res[0]) * 60 + Integer.parseInt(res[2]);
        }
        else {
            time += Integer.parseInt(res[0]);
        }
        return time;
    }

}
