package dot.locationbasedmessaging;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
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

    double longitude;
    double latitude;

    float radius = 100; // meters

    private MessageDatabaseAdapter messageDatabaseAdapter;
    private FusedLocationProviderClient mFusedLocationClient;

    public LocationService() {
        super(LocationService.class.getName());
        messageDatabaseAdapter = new MessageDatabaseAdapter(this);
        //
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        longitude = intent.getDoubleExtra("longitude", 0.0f);
        latitude = intent.getDoubleExtra("latitude", 0.0f);

        int taskType = intent.getIntExtra("task", TASK_POLL_LOCATION);
        Bundle bundle = new Bundle();


        bundle.putString("location", formatLocation(longitude,latitude));


        switch (taskType) {
            case TASK_POLL_LOCATION:
                Log.d(TAG, "TASK POLL LOCATION "); // TODO DEBUG
                MessageDatabaseAdapter.MessageContainer resultcontainer = messageDatabaseAdapter.getMessageByLocation(longitude, latitude, radius);
                String msg = resultcontainer.message;
                String distance = resultcontainer.distance;
                if (msg != "<NO RESULT>") {
                    String msgString = formatMessage(msg, distance);
                    Log.d(TAG, "MESSAGE FOUND " + msgString); // TODO DEBUG
                    bundle.putString("message", msgString);
                    String notificationText = formatResponse(longitude, latitude, msg);
                    sendNotification(notificationText); // TODO
                    receiver.send(STATUS_MSG_FOUND, bundle);

                } else {
                    Log.d(TAG, "MESSAGE NOT FOUND, returns null "); // TODO DEBUG
                    receiver.send(STATUS_MSG_NOT_FOUND, bundle);
                    clearNotification();
                }
                break;

            case TASK_INSERT_TO_DB:
                String note = intent.getStringExtra("note");
                messageDatabaseAdapter.insertData(note, longitude, latitude); // TODO insert LOCATION
                receiver.send(STATUS_POSTED, bundle);
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

    String formatMessage(String msg, String distance) {
        String response = "Message: " + msg + "\n" + "Distance: " + distance;
        return response;
    }

    String formatLocation(double longitude, double latitude) {

        String response = "Location: " + String.format("%.4f", longitude) + "\u00b0N, " + String.format("%.4f", latitude) + "\u00b0E";
        return response;
    }

    String formatResponse(double longitude, double latitude, String msg) {
        // TODO
        String response = msg + "\nLocation " + String.format("%.4f", longitude) + "\u00b0N, " + String.format("%.4f", latitude) + "\u00b0E";
        return response;
    }

}
