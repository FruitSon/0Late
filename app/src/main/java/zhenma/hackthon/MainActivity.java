package zhenma.hackthon;

import android.Manifest;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.race604.flyrefresh.FlyRefreshLayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, EasyPermissions.PermissionCallbacks,FlyRefreshLayout.OnPullRefreshListener {
    //UI
    private FlyRefreshLayout mFlylayout;
    private RecyclerView mListView;
    private ItemAdapter mAdapter;
    private DataBaseHelper helper;
    private ArrayList<ItemData> mDataSet = new ArrayList<>();
    private Handler mHandler = new Handler();
    public LinearLayoutManager mLayoutManager;
    public TextView mTextView;

    //data
    private static final String TAG = "SignOut";
    private GoogleApiClient mGoogleApiClient;
    private myThread mThread;
    public Handler handler;

    //calendar
    public Date selectedDay = Calendar.getInstance().getTime();
    private long msPerDay = 86400000;
    private int today = Calendar.getInstance().get(Calendar.DATE);

    GoogleAccountCredential mCredential;
    private String name = "";

    static final int REQUEST_ACCOUNT_PICKER = 100;
    static final int REQUEST_AUTHORIZATION = 101;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 102;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 103;
    
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    private GoogleApiClient mGoogleApiClientLoc;
    private Location mLastLocation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.time_list);
        mTextView = (TextView) findViewById(R.id.res);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            name = mBundle.getString("username");
        }
        
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();
        
        // Location
        mGoogleApiClientLoc = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        helper = new DataBaseHelper(getApplicationContext());
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                                                          getApplicationContext(), Arrays.asList(SCOPES))
        .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(name);

        initialUI();

        startService(new Intent(this,MonitorService.class));
    }
    
    protected void onStart() {
        mGoogleApiClientLoc.connect();
        super.onStart();
    }
    
    protected void onStop() {
        mGoogleApiClientLoc.disconnect();
        super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Go back to the main menu if the back key is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setTitle("Do you want to quit?")
            .setNegativeButton("Cancel",
                               new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                }
            }).setPositiveButton("OK",
                                 new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                                    int whichButton) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            Intent intent = new Intent(this, SignInActivity.class);
            Bundle mbundle = new Bundle();
            int flag = 1;
            mbundle.putInt("isSignOut", flag);
            intent.putExtras(mbundle);
            if (mGoogleApiClient.isConnected()) {
                signOut();
            }
            startActivity(intent);
            return true;
        }else if (id == R.id.action_calendar){
            //calendar,swipe horizontally
            final MaterialCalendarView dialogView = (MaterialCalendarView) getLayoutInflater()
                    .inflate(R.layout.horizontal_calendar, null, false);


            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Pick the time")
                    .setView(dialogView)
                    .setNeutralButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }
                    )
                    .setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    selectedDay = dialogView.getSelectedDate().getDate();
                                    DateTime datetime = new DateTime(selectedDay.getTime());
//                                    System.out.println(datetime);
                                    dialogInterface.dismiss();
                                    mCredential.setSelectedAccountName(name);
                                    freshData();
                                }
                            }
                    ).create().show();
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
        mCredential.newChooseAccountIntent();
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    
    
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            mCredential.setSelectedAccountName(name);
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            new RequestCalendar(mCredential, this, 0).execute();
        }
    }
    
    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = name;
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                                       mCredential.newChooseAccountIntent(),
                                       REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                                               this,
                                               "This app needs to access your Google account (via Contacts).",
                                               REQUEST_PERMISSION_GET_ACCOUNTS,
                                               Manifest.permission.GET_ACCOUNTS);
        }
    }
    
    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
                                    int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                    data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                        getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    mCredential.setSelectedAccountName(name);
                    getResultsFromApi();
                }
                break;
        }
    }
    
    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                                                   requestCode, permissions, grantResults, this);
    }
    
    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }
    
    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
    
    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    
    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
        GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
        apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
    
    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
        GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
        apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    
    
    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
                                                       final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                                                       MainActivity.this,
                                                       connectionStatusCode,
                                                       REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
    
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onConnected", "Yes");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                                                          mGoogleApiClientLoc);
        
        if (mLastLocation != null) {
//            Log.d("Latitude: ", "" + mLastLocation.getLatitude());
//            Log.d("Longitude: ", "" + mLastLocation.getLongitude());
        }else {
//            Log.d("mLastLocation==null","true");
        }
        //Thread for google map
        mThread = new myThread(mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), "Sachem Circle", "");
        handler = new myHandler();
        mThread.setHandler(handler);
        mThread.start();
    }
    
    @Override
    public void onConnectionSuspended(int i) {
        
    }
    
    @Override
    public void onLocationChanged(Location location) {
        
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        
    }

    public Date getSelectedDay() {
        return this.selectedDay;
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    class myHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            String xml=(String)msg.obj;
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db=factory.newDocumentBuilder();
                Document doc=db.parse(new ByteArrayInputStream(xml.getBytes()));
                
                Element root=doc.getDocumentElement();
                
                NodeList nodelist = root.getElementsByTagName("duration");
                String res = "Default: 1 Hour";
                if (nodelist != null) {
                    Element element = (Element)nodelist.item(0);
                    res = element.getElementsByTagName("text").item(0).getFirstChild().getNodeValue();
                }
                mTextView.setText(res);
                System.out.println("Get the result");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    private void initialUI(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        
        mFlylayout = (FlyRefreshLayout) findViewById(R.id.fly_layout);
        mFlylayout.setOnPullRefreshListener(this);

        mListView = (RecyclerView) findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(mLayoutManager);
        mAdapter = new ItemAdapter(this);

        mListView.setAdapter(mAdapter);
        mListView.setItemAnimator(new TimeListItem());
        
        View actionButton = mFlylayout.getHeaderActionButton();
        if (actionButton != null) {
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFlylayout.startRefresh();
                }
            });
        }
        freshData();
    }
    
    private void freshData() {
        mDataSet.clear();
        mAdapter.notifyDataSetChanged();
        mCredential.setSelectedAccountName(name);

        new RequestCalendar(mCredential, this, 1).execute();
        if(mThread != null && mThread.isAlive()){
            mThread.interrupt();
            mThread = new myThread(mLastLocation.getLatitude()+","+mLastLocation.getLongitude(), "New York", "");
            mThread.setHandler(handler);
            mThread.start();
        }
        getResultsFromApi();
    }

    @Override
    public void onRefresh(FlyRefreshLayout view) {
        View child = mListView.getChildAt(0);
        if (child != null) {
            bounceAnimateView(child.findViewById(R.id.icon));
        }
        
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFlylayout.onRefreshFinish();
            }
        }, 2000);
    }
    
    private void bounceAnimateView(View view) {
        if (view == null) {
            return;
        }
        
        Animator swing = ObjectAnimator.ofFloat(view, "rotationX", 0, 30, -20, 0);
        swing.setDuration(400);
        swing.setInterpolator(new AccelerateInterpolator());
        swing.start();
    }
    
    @Override
    public void onRefreshAnimationEnd(FlyRefreshLayout view) {
        freshData();
    }
    
    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        
        private LayoutInflater mInflater;
        private DateFormat dateFormat;
        
        public ItemAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            dateFormat = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.ENGLISH);
        }
        
        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.time_list_item, viewGroup, false);
            return new ItemViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
            final ItemData data = mDataSet.get(i);
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(data.color);
            itemViewHolder.icon.setBackgroundDrawable(drawable);
            itemViewHolder.icon.setImageResource(data.icon);
            itemViewHolder.title.setText(data.title);
            itemViewHolder.subTitle.setText(data.startTime);
            itemViewHolder.walk.setImageResource(data.walk);
            itemViewHolder.drive.setImageResource(data.drive);
            itemViewHolder.bus.setImageResource(data.bus);
            itemViewHolder.id.setText(data.id);
        }
        
        @Override
        public int getItemCount() {
            return mDataSet.size();
        }
    }
    
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        
        ImageView icon;
        TextView title;
        TextView subTitle;
        ImageView walk;
        ImageView drive;
        ImageView bus;
        TextView id;

        public ItemViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.subtitle);
            walk = (ImageView) itemView.findViewById(R.id.walk);
            drive = (ImageView) itemView.findViewById(R.id.drive);
            bus = (ImageView) itemView.findViewById(R.id.bus);
            id = (TextView) itemView.findViewById(R.id.item_id);
        }
    }
    
    private void resetTrans(android.widget.LinearLayout l){
        ((android.widget.ImageView)l.getChildAt(0)).setImageResource(R.mipmap.walk_g);
        ((android.widget.ImageView)l.getChildAt(1)).setImageResource(R.mipmap.drive_g);
        ((android.widget.ImageView)l.getChildAt(2)).setImageResource(R.mipmap.bus_g);
    }
    
    public void clickWalk(View v){
        resetTrans((android.widget.LinearLayout) v.getParent());
        ((android.widget.ImageView)v).setImageResource(R.mipmap.walk_c);
        android.widget.RelativeLayout l = (android.widget.RelativeLayout)v.getParent().getParent();
        String id = ((android.widget.TextView)l.getChildAt(2)).getText().toString();
        helper.updateTransport(id, 0);
        Log.d("click", "walk:" + id);
    }
    
    public void clickDrive(View v){
        resetTrans((android.widget.LinearLayout)v.getParent());
        ((android.widget.ImageView)v).setImageResource(R.mipmap.drive_c);
        android.widget.RelativeLayout l = (android.widget.RelativeLayout)v.getParent().getParent();
        String id = ((android.widget.TextView)l.getChildAt(2)).getText().toString();
        helper.updateTransport(id, 1);
        Log.d("click", "Drive:" + id);
    }
    
    public void clickBus(View v){
        resetTrans((android.widget.LinearLayout)v.getParent());
        ((android.widget.ImageView)v).setImageResource(R.mipmap.bus_c);
        android.widget.RelativeLayout l = (android.widget.RelativeLayout) v.getParent().getParent();
        String id = ((android.widget.TextView)l.getChildAt(2)).getText().toString();
        helper.updateTransport(id, 2);
        Log.d("click", "Bus:" + id);
    }

    public void updateList(){
        List<ItemData> events = helper.getEvents(selectedDay.toString());
        for (int i = 0; i < events.size(); ++i) {
            ItemData event = events.get(i);
            mDataSet.add(event);
        }
        mAdapter.notifyItemInserted(0);
        mLayoutManager.scrollToPosition(0);
    }

    public DataBaseHelper getDataBaseHelper(){
        return helper;
    }
}
