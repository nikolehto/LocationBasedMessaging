package dot.weatherinformation3;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

//import java.util.logging.Handler;

/**
 * Created by Niko on 6.2.2018.
 */

public class MyObserver extends ContentObserver {
    public MyObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange,null);
    }

    @Override
    public void onChange(boolean selfChange,
                         Uri uri) {
        //Write your code here
    }


}
