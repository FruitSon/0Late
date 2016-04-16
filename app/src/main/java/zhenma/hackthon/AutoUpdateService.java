package zhenma.hackthon;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CountDownTimer cdt = new CountDownTimer(Globals.AUTO_UPDATE_DURATION,1000){

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //// TODO: 4/15/16 update event list
                new RequestCalendar(Globals.GOOGLE_ACCOUNT_CREDENTIAL).execute();
                start();
            }
        };

        cdt.start();
    }

}
