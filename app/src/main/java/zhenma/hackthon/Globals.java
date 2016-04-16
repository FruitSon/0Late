package zhenma.hackthon;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by RZ on 4/15/16.
 */
public class Globals {
    public static long AUTO_UPDATE_DURATION = 3600000; //1 hour
    public static int POLLING_DURATION = 10000;    //10s
    public static int AUTO_CHECK_DURATION = 60000;     //1min
    public static GoogleAccountCredential GOOGLE_ACCOUNT_CREDENTIAL = null;
    public static String FIRST_LOCATION = null;
    public static String FIRST_EVENT = null;
    public static String FIRST_TIME = null;
    public static String FIRST_ID = null;

}
