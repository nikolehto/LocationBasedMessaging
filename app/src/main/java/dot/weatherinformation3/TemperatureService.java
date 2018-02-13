package dot.weatherinformation3;

import android.app.IntentService;
import android.content.Intent;
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


public class TemperatureService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FROM_DB = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_ERROR = 3;
    private static final String TAG = "TemperatureService";

    private final String APIURL = "http://api.openweathermap.org/data/2.5/weather?";
    private String city = "";
    private String APIkey = "";
    private int DEBUGdelay = 1800; // TODO remove delay

    private TemperatureDatabaseAdapter temperatureDatabaseAdapter;

    public TemperatureService() {
        super(TemperatureService.class.getName());
        temperatureDatabaseAdapter = new TemperatureDatabaseAdapter(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Log.d(TAG, "Service Started!");

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        city = intent.getStringExtra("city");
        APIkey = intent.getStringExtra("APIkey");

        Bundle bundle = new Bundle();

        String url = buildURI();
        if (!TextUtils.isEmpty(url)) {
            String latestTemp = temperatureDatabaseAdapter.getLatestTemperatureByCity(city);
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
                    temperatureDatabaseAdapter.insertData(city, result);
                }
            } catch (IOException e) {
                // TODO Error
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
                        /* Sending error message back to activity */
                //bundle.putString(Intent.EXTRA_TEXT, e.toString());
                //receiver.send(STATUS_ERROR, bundle);
            } catch (JSONException e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(STATUS_ERROR, bundle);
                // TODO Error
            }
        }

        //Log.d(TAG, "Service Stopping!");
        this.stopSelf();
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

    String parseResponse(String response) throws JSONException {
        JSONObject jObj = null;
        String tempValue = null;

        jObj = new JSONObject(response);
        tempValue = jObj.getJSONObject("main").getString("temp");

        return tempValue;
    }

    String buildURI(){
        Uri.Builder builder = Uri.parse(APIURL).buildUpon();

        String contentType = "application/json";

        if(city == "" || APIkey == "")
        {
            return null;
        }

        builder.appendQueryParameter("q", city);
        builder.appendQueryParameter("units", "metric");
        builder.appendQueryParameter("APPID", APIkey);

        return builder.toString();
    }
}
