package zhenma.hackthon;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by dnalwqer on 4/16/16.
 */

class RequestCalendar extends AsyncTask<Void, Void, List<String>> {
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;
    private MainActivity activity;
    private long msPerDay = 86400000;
    private int flag;
    private Date requestTime;

    public RequestCalendar(GoogleAccountCredential credential, MainActivity activity) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("0Late")
                .build();
        this.activity = activity;
        this.flag = 0;
    }

    public RequestCalendar(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("0Late")
                .build();
        this.flag = 1;
    }

    /**
     * Background task to call Google Calendar API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            if(flag == 0)
                requestTime = activity.getSelectedDay();
            else
                requestTime = Calendar.getInstance().getTime();
            return getDataFromApi();
        } catch (Exception e) {
            Log.d("main", e.toString());
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     *
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<>();
        Events events;
        if (!(new DateTime(Calendar.getInstance().getTime().getTime())).toString().equals(requestTime.toString())) {
            events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(new DateTime(requestTime.getTime()))
                    .setTimeMax(new DateTime(requestTime.getTime() + msPerDay))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
        } else {
            events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setTimeMax(new DateTime(requestTime.getTime() + msPerDay))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
        }
        List<Event> items = events.getItems();
        System.out.println("flag:" + flag);
        if (flag == 0) {
            for (Event event : items) {
                DateTime startTime = event.getStart().getDateTime();
                DateTime endTime = event.getEnd().getDateTime();
                String eventName = event.getSummary();
                String location = event.getLocation();
                String s_time[] = startTime.toString().split("T");
                String format_time = s_time[0] + " " + s_time[1].substring(0, 5);
                activity.getDataBaseHelper().checkAndUpdate(event.getId(), activity.getSelectedDay().toString(), eventName, startTime.toString());
                eventStrings.add(event.getId());
            }


            activity.getDataBaseHelper().deleteRemoved(eventStrings, activity.getSelectedDay().toString());
        }
        else if(flag == 1){

            if(items.size()>0) {
                Globals.FIRST_TIME = items.get(0).getStart().getDateTime().toStringRfc3339();
                Globals.FIRST_EVENT = items.get(0).getSummary();
                Globals.FIRST_LOCATION = items.get(0).getLocation();
                Globals.FIRST_ID = items.get(0).getId();
            }
            System.out.println("firstLocation:" + items.get(0).getStart().getDateTime().toStringRfc3339());
        }
        return eventStrings;
    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(List<String> output) {
        if(flag == 0)
            activity.updateList();
    }

    @Override
    protected void onCancelled() {
        if (mLastError != null) {
            if (mLastError instanceof UserRecoverableAuthIOException) {
//                startActivityForResult(
//                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
//                        MainActivity.REQUEST_AUTHORIZATION);
            }
        }
    }
}
