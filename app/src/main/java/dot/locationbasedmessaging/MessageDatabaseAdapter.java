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

public class MessageDatabaseAdapter {
    LocationDbHelper myhelper;

    public MessageDatabaseAdapter(Context context)
    {
        myhelper = new LocationDbHelper(context);
    }

    public long insertData(String city, String temperature)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocationDbHelper.CITY, city);
        contentValues.put(LocationDbHelper.TEMPERATURE, temperature);
        long id = dbb.insert(LocationDbHelper.TABLE_NAME, null , contentValues);
        return id;
    }

    public String getLatestTemperatureByCity(String city) {
        SQLiteDatabase db = myhelper.getReadableDatabase();

        String[] columns = null;
        String where = LocationDbHelper.CITY + "=?";
        String[] args = {city};
        String orderBy = LocationDbHelper.UID +" DESC";
        Cursor cursor = db.query(LocationDbHelper.TABLE_NAME, columns, where, args, null, null, orderBy);

        if(cursor.moveToFirst()) {
            String temperature = cursor.getString(cursor.getColumnIndex(LocationDbHelper.TEMPERATURE));
            cursor.getString(cursor.getColumnIndex(LocationDbHelper.UID));
            return temperature;
        }
        else {
            // TODO better error handing
            return "<NO RECORD>";
        }
    }

    public String getData()
    {
        SQLiteDatabase db = myhelper.getReadableDatabase(); // Correction to example - writable -> readable
        String[] columns = {LocationDbHelper.UID, LocationDbHelper.CITY, LocationDbHelper.TEMPERATURE};
        Cursor cursor =db.query(LocationDbHelper.TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(LocationDbHelper.UID));
            String city =cursor.getString(cursor.getColumnIndex(LocationDbHelper.CITY));
            String  temperature =cursor.getString(cursor.getColumnIndex(LocationDbHelper.TEMPERATURE));
            buffer.append(cid+ "   " + city + "   " + temperature +" \n");
        }
        cursor.close();
        db.close();
        return buffer.toString();
    }

    public int delete(String city)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs ={city};

        int count =db.delete(LocationDbHelper.TABLE_NAME , LocationDbHelper.CITY+" = ?",whereArgs);
        return  count;
    }

    static class LocationDbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "myDatabase";    // Database Name
        private static final String TABLE_NAME = "myTable";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String MESSAGE = "Message";    //Column II
        private static final String LON = "Lon";    // Column III
        private static final String LAT = "Lat";    // Column IV
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MESSAGE+" VARCHAR(255) ,"+ LON+" VARCHAR(255) ,"+ LAT+" VARCHAR(255));";
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