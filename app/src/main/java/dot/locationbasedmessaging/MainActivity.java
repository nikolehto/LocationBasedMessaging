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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationResultReceiver.Receiver {

    int poll_time = 3000;
    boolean errorFlag = false;

    boolean isSet = false;

    LocationResultReceiver mReceiver;
    Intent intent;

    EditText noteText;
    TextView locationText;
    TextView showMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        noteText = findViewById(R.id.editText);
        locationText = findViewById(R.id.location_textview);
        showMessageText = findViewById(R.id.message_textview);

        mReceiver = new LocationResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        // TODO Sticky
        intent = new Intent(Intent.ACTION_SYNC, null, this, LocationService.class);

        //intent.putExtra("city", city);
        intent.putExtra("receiver", mReceiver);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case LocationService.STATUS_RUNNING:
                break;
            case LocationService.STATUS_FINISHED:
                    String location = resultData.getString("location");
                    String message = resultData.getString("message");
                    if(message != null && location != null) {
                        updateText(location, message);
                    }
                break;
            case LocationService.STATUS_POSTED:
                break;
            case LocationService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);

                if (errorFlag == false) {
                    errorFlag = true;
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
        }
    }

    void updateText(String location, String message)
    {
        if(!isSet)
        {
            locationText.setText(location);
            showMessageText.setText(message);
        }
        isSet = true;
    }
/*
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
*/
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
