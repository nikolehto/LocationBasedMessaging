package dot.locationbasedmessaging;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Niko on 12.2.2018.
 * Based on http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 */

public class LocationResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public LocationResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
