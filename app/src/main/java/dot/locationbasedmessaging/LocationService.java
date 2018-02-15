package dot.locationbasedmessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public LocationService() {
        super(LocationService.class.getName());
        messageDatabaseAdapter = new MessageDatabaseAdapter(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        int taskType = intent.getIntExtra("task", TASK_POLL_LOCATION);
        updateLocation();
        Bundle bundle = new Bundle();

        switch(taskType)
        {
            case TASK_POLL_LOCATION:
                Log.d(TAG, "TASK POLL LOCATION "); // TODO DEBUG
                String msg = messageDatabaseAdapter.getMessageByLocation(longitude, latitude, radius);
                if(msg != "<NO RESULT>")
                {
                    Log.d(TAG, "MESSAGE FOUND " + msg); // TODO DEBUG
                    bundle.putString("location", "TODO_LOCATION");
                    bundle.putString("message", msg);
                    receiver.send(STATUS_MSG_FOUND, bundle);
                    //sendNotification(); TODO
                }
                else
                {
                    Log.d(TAG, "MESSAGE NOT FOUND, returns null "); // TODO DEBUG
                    receiver.send(STATUS_MSG_NOT_FOUND, Bundle.EMPTY);
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

    void sendNotification(String temperature, String source) {
        String message = "City:  Temperature: " + temperature + "\nFROM: " + source;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "locationServiceID404")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Weather Information")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }


/* TODO remove
    String getNearestMsg()
    {
        return "";
    }
*/
    String formatResponse()
    {
        // TODO
        String msg = "TODO FORMAT RESPONSE";
        return msg;
    }

    boolean updateLocation()
    {
        //Location da = new Location();
        //da.getLatitude();
        //da.getLongitude()

        // TODO
        return true;
    }
}
