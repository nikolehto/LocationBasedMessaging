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
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationResultReceiver.Receiver {
    //FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(getContext());
    public static final int FROM_DB = 1;
    public static final int FROM_INTERNET = 2;

    private final String APIkey = "077dcca6103cc9b1548abcee08a850a7";
    private String notification_channel = "just a random id";
    private String city = "Oulu,FI";

    final int updateRate = 600; // 6sec + request time
    final int errorUpdateRate = 1200; // 12sec + request time

    boolean errorFlag = false;
    boolean userRefresh = false;
    boolean isRecent = false;
    boolean isSet = false;
    String latestTemp = "";

    final Handler delayHandler = new Handler();

    LocationResultReceiver mReceiver;
    Intent intent;

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

        mReceiver = new LocationResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        intent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);
        intent.putExtra("city", city);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("APIkey", APIkey);

        startUpdateTEMP();
        //startUpdateTEMP();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case LocationService.STATUS_RUNNING:
                break;
            case LocationService.STATUS_FROM_DB:
                if(!isRecent && !isSet) { // update only when
                    String DB_result = resultData.getString("DB_result");
                    if(DB_result != null) {
                        updateText(DB_result, FROM_DB);
                    }
                    //temp_text.setText(DB_result);
                }
                break;
            case LocationService.STATUS_FINISHED:
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
            case LocationService.STATUS_ERROR:
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
        String message = "City: " + city + "\nTemperature: " + temperature + "\nFROM: " + source;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, notification_channel)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Weather Information")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
    }

    /*
    Alternative way - discontinued

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
