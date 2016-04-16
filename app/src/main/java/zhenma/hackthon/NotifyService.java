package zhenma.hackthon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class NotifyService extends Service {

    String event = null;
    Long time;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("notification start");
        Bundle mBundle = intent.getExtras();

        if (mBundle != null) {
            event = mBundle.getString("event");
            time = mBundle.getLong("time");

        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_app)
                .setContentTitle(event)
                .setContentText("Remaining Time: "+time+" mins. It's time to go!" );

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        return super.onStartCommand(intent, flags, startId);
    }
}
