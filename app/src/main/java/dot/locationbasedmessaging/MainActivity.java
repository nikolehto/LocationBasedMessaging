package dot.locationbasedmessaging;


/*
*  TODO:
*  - Get map
*  - Get location ( in service )
*  - Poll database () (in service)
*  - Sticky location service
*  - Pop-up Notification (from service)
*  - Hide Notification (from service)
*  - Write messages ( Main activity )
*  - Store messages to database ( Main activity - async task? )
*  - Read message ( Main activity? - async task? // return nearest message from service)
*  ...
* */
import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationResultReceiver.Receiver {

    int poll_time = 6000;
    boolean errorFlag = false;

    boolean isSet = false;
    int userRefresh = 0;
    final Handler delayHandler = new Handler();

    LocationResultReceiver mReceiver;
    Intent intent;

    EditText noteText;
    TextView locationText;
    TextView showMessageText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //showPermissionDialog();
        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        noteText = findViewById(R.id.editText);
        locationText = findViewById(R.id.location_textview);
        showMessageText = findViewById(R.id.message_textview);

        mReceiver = new LocationResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        // TODO Sticky
        intent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);
        intent.putExtra("receiver", mReceiver);
        pollTask(false);
    }
    /*
    private void showPermissionDialog() {
        if (!LocationService.checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    99
                    );
        }
    }
    */

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        // always update Location
        String location = resultData.getString("location");
        if(location != null)
        {
            updateLocationText(location);
        }

        switch (resultCode) {
            case LocationService.STATUS_MSG_FOUND:

                    String message = resultData.getString("message");
                    if(message != null) {
                        updateMessageText(message);
                    }
                    else
                    {
                        updateMessageText("DEBUG: NEVER HAPPEN"); // TODO REMOVE
                    }
                break;
            case LocationService.STATUS_MSG_NOT_FOUND:
                    updateMessageText("DEBUG, No message stored on location"); // TODO REMOVE
                break;
            case LocationService.STATUS_POSTED:
                Toast.makeText(this, "NOTE POSTED", Toast.LENGTH_LONG).show();
                break;

            //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }

            // if not started by user start new service
        if(userRefresh == 0) {
            // Last service was success, start new service
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pollTask(false);
                }
            }, poll_time);
        }
        else {
            userRefresh--;
        }
    }

    void updateMessageText(String message)
    {
        // TODO if differs - only then set
        showMessageText.setText(message);

    }

    void updateLocationText(String location)
    {
        // TODO if differs - only then set
        locationText.setText(location);
    }

    public void onPostClick(View v)
    {
        insertTask(true);
    }

    private void insertTask(boolean fromUser)
    {
        if(fromUser)
        {
            userRefresh++;
        }

        String note = noteText.getText().toString();
        intent.putExtra("task", LocationService.TASK_INSERT_TO_DB);
        intent.putExtra("note", note);
        startService(intent);
    }

    private void pollTask(boolean fromUser)
    {
        if(fromUser)
        {
            userRefresh++;
        }
        intent.removeExtra("note");
        intent.putExtra("task", LocationService.TASK_POLL_LOCATION);
        startService(intent);
    }

    public void debugButton(View v)
    {
        //updateText("DEBUG", "PRESSED");
        pollTask(true);
    }

}
