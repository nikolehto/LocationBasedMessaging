package dot.locationbasedmessaging;

/**
 * Created by Niko on 13.2.2018.
 * based on http://abhiandroid.com/database/sqlite
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MessageDatabaseAdapter {
    LocationDbHelper myhelper;

    public MessageDatabaseAdapter(Context context)
    {
        myhelper = new LocationDbHelper(context);
    }

    public long insertData(String msg, double longitude, double latitude)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationDbHelper.MESSAGE, msg);
        contentValues.put(LocationDbHelper.LON, longitude);
        contentValues.put(LocationDbHelper.LAT, latitude);
        long id = dbb.insert(LocationDbHelper.TABLE_NAME, null , contentValues);
        return id;
    }

    public String getMessageByLocation(double q_longitude, double q_latitude, double q_radius)
    {
        SQLiteDatabase db = myhelper.getReadableDatabase();

        // To optimize query, but how to deal with (-180) - 180 change
        //String where = LocationDbHelper.LON + ">?" + LocationDbHelper.LON + "<?" + LocationDbHelper.LAT + ">?" + LocationDbHelper.LAT + "<?";
        //String args = {min_lon, max_lon, min_lat, max_lot};
        String orderBy = LocationDbHelper.UID;
        Cursor cursor =db.query(LocationDbHelper.TABLE_NAME, null,null,null,null,null,orderBy);
        String result = "<NO RESULT>";
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(LocationDbHelper.UID));
            String msg =cursor.getString(cursor.getColumnIndex(LocationDbHelper.MESSAGE));
            double lon =cursor.getDouble(cursor.getColumnIndex(LocationDbHelper.LON));
            double lat =cursor.getDouble(cursor.getColumnIndex(LocationDbHelper.LAT));

            boolean isNearest = true; // TODO implement functionality
             // before it this should return latest MSG
            if(isNearest)
            {
                Log.d("DATABASE: ", msg); // TODO DEBUG
                result = msg;
            }
        }
        cursor.close();
        db.close();
        return result;
    }

    public String getData()
    {
        SQLiteDatabase db = myhelper.getReadableDatabase(); // Correction to example - writable -> readable
        String[] columns = null;
        Cursor cursor =db.query(LocationDbHelper.TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(LocationDbHelper.UID));
            String msg =cursor.getString(cursor.getColumnIndex(LocationDbHelper.MESSAGE));
            double lon =cursor.getDouble(cursor.getColumnIndex(LocationDbHelper.LON));
            double lat =cursor.getDouble(cursor.getColumnIndex(LocationDbHelper.LAT));

            buffer.append(cid+ "   " + msg + "   " + lon + "   " + lat + "\n");
        }
        cursor.close();
        db.close();
        return buffer.toString();
    }

    public void deleteById(String id)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs ={id};

        db.delete(LocationDbHelper.TABLE_NAME , LocationDbHelper.UID+" = ?",whereArgs);
        //return  count;
    }

    static class LocationDbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "myDatabase";    // Database Name
        private static final String TABLE_NAME = "myTable";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String MESSAGE = "message";    //Column II
        private static final String LON = "lon";    // Column III
        private static final String LAT = "lat";    // Column IV
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MESSAGE+" VARCHAR(255) ,"+ LON+" DOUBLE ,"+ LAT+" DOUBLE);";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private Context context;

        public LocationDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) {
                e.printStackTrace();
                //Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}