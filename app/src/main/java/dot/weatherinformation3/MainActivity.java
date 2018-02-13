package dot.weatherinformation3;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements TemperatureResultReceiver.Receiver {

    //FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getContext());
    final String APIkey = "077dcca6103cc9b1548abcee08a850a7";
    String city = "Oulu,FI";
    String units = "metric";
    //String url;

    public static final int FROM_DB = 1;
    public static final int FROM_INTERNET = 2;

    private String notification_channel = "just a random id";

    final int updateRate = 600; // 6sek 10min + request time
    final int errorUpdateRate = 1200; // 12sek 20min + request time

    boolean errorFlag = false;
    boolean userRefresh = false;
    boolean isRecent = false;
    boolean isSet = false;
    final Handler delayHandler = new Handler();

    private String latestTemp = "";

    TemperatureResultReceiver mReceiver;
    NotificationChannel mChannel;

    NotificationManager notificationManager;
    Intent intent;

    //MyObserver myObserver = new MyObserver(new Handler());

    TextView temp_text;
    TextView city_text;
    TextView source_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        temp_text = findViewById(R.id.temperature);
        city_text = findViewById(R.id.citytext);
        source_text = findViewById(R.id.source);

        updateCity();

        mReceiver = new TemperatureResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        intent = new Intent(Intent.ACTION_SYNC, null, this, TemperatureService.class);
        intent.putExtra("city", city);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("APIkey", APIkey);

        startUpdateTEMP();
        //startUpdateTEMP();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case TemperatureService.STATUS_RUNNING:
                // temp_text.setText("updating"); // TODO remove
                //setProgressBarIndeterminateVisibility(true);
                break;
            case TemperatureService.STATUS_FROM_DB:
                if(!isRecent && !isSet) { // update only when
                    String DB_result = resultData.getString("DB_result");
                    updateText(DB_result, FROM_DB);
                    //temp_text.setText(DB_result);
                }
                break;
            case TemperatureService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                //setProgressBarIndeterminateVisibility(false);
                String result = resultData.getString("result");
                //temp_text.setText(result);
                updateText(result, FROM_INTERNET);
                errorFlag = false;

                if(userRefresh) // if user launched activity - skip one delayed call
                {
                    userRefresh = false;
                    break;
                }

                // Last service was success, start new service
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startUpdateTEMP();
                    }
                }, updateRate);

                break;
            case TemperatureService.STATUS_ERROR:
                /* Handle the error */
                String DB_result = resultData.getString("DB_result");
                if(DB_result != null)
                {
                    updateText(DB_result, FROM_DB);
                }

                String error = resultData.getString(Intent.EXTRA_TEXT);

                if (errorFlag == false) {
                    errorFlag = true;
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }

                if(userRefresh)
                {
                    userRefresh = false;
                    break;
                }

                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startUpdateTEMP();
                    }
                }, errorUpdateRate);
                break;
        }
    }

    void startUpdateTEMP() {
        startService(intent);
    }

    void updateText(String temperature, int source)
    {
        isSet = true;

        // todo only if neccessary
        if(source == FROM_DB)
        {
            source_text.setText("STORED");
            sendNotification(temperature, "STORED");
        }
        else // if (source == FROM_INTERNET)
        {
            isRecent = true;
            source_text.setText("REAL-TIME");
            sendNotification(temperature, "REAL-TIME");
        }

        temp_text.setText(temperature);
        latestTemp = temperature;
    }

    void updateCity()
    {
        city_text.setText(city);
    }

    @Override
    protected void onStop() {
        if(latestTemp != "")
        {
            sendNotification(latestTemp, "STOPPED");
        }
        super.onStop();
    }

    void sendNotification(String temperature, String source) {
        //Get an instance of NotificationManager//

        String message = "City: " + city + "\nTemperature: " + temperature + "\nFROM: " + source;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, notification_channel)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Weather Information")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);

        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

        mNotificationManager.notify(001, mBuilder.build());
    }

    /*
    private class UpdateTemperatureAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try
            {
                InputStream is;
                URL path = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) path.openConnection();
                int timeout = 60 * 1000;

                connection.setReadTimeout(timeout); // set request timeout
                connection.setConnectTimeout(timeout);
                connection.setRequestMethod("GET"); //set HTTP method
                connection.connect();

                StringBuffer buffer = new StringBuffer();
                is = connection.getInputStream();
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
                    // TODO: Error handling?
                    return "ERROR_0";
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return "ERROR_1";
        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);

            temp_text.setText(result);

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        String parseResponse(String response) throws JSONException {
            JSONObject jObj = null;
            String tempValue = null;

            jObj = new JSONObject(response);
            tempValue = jObj.getJSONObject("main").getString("temp");

            return tempValue;
        }
    }
    */
}
