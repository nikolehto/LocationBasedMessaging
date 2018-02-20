package dot.locationbasedmessaging;


/*
*  TODO:
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
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

    LocationResultReceiver mReceiver;
    Intent intent;

    EditText noteText;
    TextView locationText;
    TextView showMessageText;
    LocationManager locationManager;

    double latitude = 0.0;
    double longitude = 0.0;

    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                pollTask();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setContentView(R.layout.activity_main);
        showPermissionDialog();

        noteText = findViewById(R.id.editText);
        locationText = findViewById(R.id.location_textview);
        showMessageText = findViewById(R.id.message_textview);

        mReceiver = new LocationResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        // TODO Sticky
        intent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);
        intent.putExtra("receiver", mReceiver);
        startListener();
    }

    @SuppressLint("MissingPermission")
    protected void startListener() {
        // first update current location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            pollTask();
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000,
                0.0f,
                listener
        );
    }

    private void showPermissionDialog() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {}
        else
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                99
                );
    }

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
        if(longitude != 0.0f && latitude != 0.0f) {
            insertTask();
        }
        else {
            Toast.makeText(this, "No GPS yet", Toast.LENGTH_LONG).show();
        }
    }

    protected void insertTask()
    {
        String note = noteText.getText().toString();
        intent.putExtra("task", LocationService.TASK_INSERT_TO_DB);
        intent.putExtra("note", note);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        startService(intent);
    }

    protected void pollTask()
    {
        intent.removeExtra("note");
        intent.putExtra("task", LocationService.TASK_POLL_LOCATION);
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        startService(intent);
    }

    public void debugButton(View v)
    {
        //updateText("DEBUG", "PRESSED");
        pollTask();
    }

}
