package dot.locationbasedmessaging;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
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

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_POSTED = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_ERROR = 3;

    public static final int TASK_POLL_LOCATION = 0;
    public static final int TASK_INSERT_TO_DB = 1;

    private static final String TAG = "LocationService";

    private MessageDatabaseAdapter messageDatabaseAdapter;

    public LocationService() {
        super(LocationService.class.getName());
        messageDatabaseAdapter = new MessageDatabaseAdapter(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        int taskType = intent.getIntExtra("task", TASK_POLL_LOCATION);
        updateLocation();

        Bundle bundle = new Bundle();

        switch(taskType)
        {
            case TASK_POLL_LOCATION:
                getNearestMsg();
                break;
            case TASK_INSERT_TO_DB:
                String note = intent.getStringExtra("note");
                messageDatabaseAdapter.insertData(); // TODO insert LOCATION
                break;
        }


        String latestTemp = messageDatabaseAdapter.getLatestTemperatureByCity(city);
        if(latestTemp != "<NO RECORD>") {
            bundle.putString("DB_result", latestTemp);
            receiver.send(STATUS_FROM_DB, bundle);
        }
        else {
            /* Update UI: Download Service is Running */
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        }

        try {
            try {
                Thread.sleep(DEBUGdelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String result = getTemperature(url);

            if (result != null) {
                bundle.putString("result", result);
                receiver.send(STATUS_FINISHED, bundle);
                messageDatabaseAdapter.insertData(city, result);
            }
        } catch (IOException e) {
            // TODO Error
            bundle.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(STATUS_ERROR, bundle);
                    /* Sending error message back to activity */
            //bundle.putString(Intent.EXTRA_TEXT, e.toString());
            //receiver.send(STATUS_ERROR, bundle);
        }


        //Log.d(TAG, "Service Stopping!");
        this.stopSelf();
    }


    String getNearestMsg()
    {
        return "";
    }

    private String getTemperature(String url) throws IOException, JSONException {

            URL path = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) path.openConnection();
            int timeout = 60 * 1000;

            connection.setReadTimeout(timeout); // set request timeout
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod("GET"); //set HTTP method
            connection.connect();

            StringBuffer buffer = new StringBuffer();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ( (line = br.readLine()) != null )
            {
                buffer.append(line + "rn");
            }

            is.close();
            connection.disconnect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                String temperature = parseResponse(buffer.toString());
                return temperature;
            }
            else
            {
                return "NO_RESPONSE";
            }
    }

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
