package zhenma.hackthon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener{

    private static final String TAG = "SignOutActivity";
    private GoogleApiClient mGoogleApiClient;
    private TextView mTextView;

    //calendar
    private Calendar mCurrentTime = Calendar.getInstance();
    private Date selectedDay = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextView = (TextView) findViewById(R.id.username);
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String username = mBundle.getString("username");
            mTextView.setText(username);
        }
        else {
            mTextView.setText("");
        }
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calendar,swipe vertically
//                DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
//                    public void onDateSet(DatePicker view, int year, int monthOfYear,
//                                          int dayOfMonth) {
//                        mCurrentTime.set(Calendar.YEAR, year);
//                        mCurrentTime.set(Calendar.MONTH, monthOfYear);
//                        mCurrentTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                    }
//                };
//
//                new DatePickerDialog(view.getContext(), mDateListener,
//                        mCurrentTime.get(Calendar.YEAR),
//                        mCurrentTime.get(Calendar.MONTH),
//                        mCurrentTime.get(Calendar.DAY_OF_MONTH)).show();


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
                                        Date selectedDay = dialogView.getSelectedDate().getDate();
                                        System.out.println(selectedDay);

                                        dialogInterface.dismiss();

                                    }
                                }
                        ).create().show();
            }
        });
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Go back to the main menu if the back key is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
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
            } else {
                return super.onKeyDown(keyCode, event);
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
        if (id == R.id.action_settings) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        Log.d("logout", "Logout");
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
