package dot.weatherinformation3;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
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
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements TemperatureResultReceiver.Receiver {
    final String APIkey = "077dcca6103cc9b1548abcee08a850a7";
    final String APIURL = "http://api.openweathermap.org/data/2.5/weather?"; //weather?lat=35&lon=139
    String location = "Oulu,FI";
    String units = "metric";
    String url;

    TemperatureResultReceiver mReceiver;

    NotificationManager notificationManager;
    Intent intent;
    //MyObserver myObserver = new MyObserver(new Handler());

    TextView temp_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildURI();

        mReceiver = new TemperatureResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, TemperatureService.class);
        intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);

        startService(intent);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        temp_text =(TextView) findViewById(R.id.temperature);

        //startUpdateTEMP();
        //startUpdateTEMP();
    }

    void buildURI(){
        Uri.Builder builder = Uri.parse(APIURL).buildUpon();

        String contentType = "application/json";

        builder.appendQueryParameter("q", location);
        builder.appendQueryParameter("units", units);
        builder.appendQueryParameter("APPID", APIkey);

        url = builder.toString();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case TemperatureService.STATUS_RUNNING:
                //setProgressBarIndeterminateVisibility(true);
                break;
            case TemperatureService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                //setProgressBarIndeterminateVisibility(false);
                String result = resultData.getString("result");
                temp_text.setText(result);
                /* Update ListView with result */
                // arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, results);
                // listView.setAdapter(arrayAdapter);
                break;
            case TemperatureService.STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    void startUpdateTEMP() {
        buildURI();
        new UpdateTemperatureAsyncTask().execute(url);
    }

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
}
