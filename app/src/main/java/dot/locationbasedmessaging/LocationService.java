package dot.locationbasedmessaging;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Created by Niko on 12.2.2018.
 * Based on http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 */

public class LocationService extends IntentService {

    public static final int STATUS_POSTED = 1;
    public static final int STATUS_MSG_FOUND = 2;
    public static final int STATUS_MSG_NOT_FOUND = 3;

    public static final int TASK_POLL_LOCATION = 0;
    public static final int TASK_INSERT_TO_DB = 1;

    private static final String TAG = "LocationService";

    double radius = 100; // meters
    double longitude = 0.0f;
    double latitude = 0.0f;
    private MessageDatabaseAdapter messageDatabaseAdapter;
    private FusedLocationProviderClient mFusedLocationClient;

    public LocationService() {
        super(LocationService.class.getName());
        messageDatabaseAdapter = new MessageDatabaseAdapter(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        int taskType = intent.getIntExtra("task", TASK_POLL_LOCATION);
        updateLocation();
        Bundle bundle = new Bundle();

        //Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        switch (taskType) {
            case TASK_POLL_LOCATION:
                Log.d(TAG, "TASK POLL LOCATION "); // TODO DEBUG
                String msg = messageDatabaseAdapter.getMessageByLocation(longitude, latitude, radius);
                if (msg != "<NO RESULT>") {
                    Log.d(TAG, "MESSAGE FOUND " + msg); // TODO DEBUG
                    bundle.putString("location", longitude + "" + latitude);
                    bundle.putString("message", msg);
                    receiver.send(STATUS_MSG_FOUND, bundle);
                    String notificationText = formatResponse(longitude, latitude, msg);
                    sendNotification(notificationText); // TODO
                } else {
                    Log.d(TAG, "MESSAGE NOT FOUND, returns null "); // TODO DEBUG
                    receiver.send(STATUS_MSG_NOT_FOUND, Bundle.EMPTY);
                    clearNotification();
                }
                break;

            case TASK_INSERT_TO_DB:
                String note = intent.getStringExtra("note");
                messageDatabaseAdapter.insertData(note, longitude, latitude); // TODO insert LOCATION
                receiver.send(STATUS_POSTED, Bundle.EMPTY);
                break;
            default:
                Log.d(TAG, "UNKNOWN TASK "); // TODO DEBUG
        }


        Log.d(TAG, "Service Stopping!");
        this.stopSelf();
    }

    void sendNotification(String message) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "locationServiceID404")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("NOTE FOUND")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

    void clearNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(001);
    }

    /* TODO remove
        String getNearestMsg()
        {
            return "";
        }
    */
    String formatResponse(double longitude, double latitude, String msg) {
        // TODO
        String response = msg + "\nLocation " + String.format("%.4f", longitude) + "\u00b0N, " + String.format("%.4f", latitude) + "\u00b0E";
        return response;
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    boolean updateLocation() {
        //Location da = new Location();
        //da.getLatitude();
        //da.getLongitude()

        //bool permission = checkPermission();

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                        }
                    }
                });
        // TODO
        return true;
    }
}
