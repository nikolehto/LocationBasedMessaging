package dot.weatherinformation3;

/**
 * Created by Niko on 13.2.2018.
 * based on http://abhiandroid.com/database/sqlite
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TemperatureDatabaseAdapter {
    TemperatureDbHelper myhelper;

    public TemperatureDatabaseAdapter(Context context)
    {
        myhelper = new TemperatureDbHelper(context);
    }

    public long insertData(String city, String temperature)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TemperatureDbHelper.CITY, city);
        contentValues.put(TemperatureDbHelper.TEMPERATURE, temperature);
        long id = dbb.insert(TemperatureDbHelper.TABLE_NAME, null , contentValues);
        return id;
    }

    public String getLatestTemperatureByCity(String city) {
        SQLiteDatabase db = myhelper.getReadableDatabase();

        String[] columns = null;
        String where = TemperatureDbHelper.CITY + "=?";
        String[] args = {city};
        String orderBy = TemperatureDbHelper.UID +" DESC";
        Cursor cursor = db.query(TemperatureDbHelper.TABLE_NAME, columns, where, args, null, null, orderBy);

        if(cursor.moveToFirst()) {
            String temperature = cursor.getString(cursor.getColumnIndex(TemperatureDbHelper.TEMPERATURE));
            cursor.getString(cursor.getColumnIndex(TemperatureDbHelper.UID));
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
        String[] columns = {TemperatureDbHelper.UID,TemperatureDbHelper.CITY,TemperatureDbHelper.TEMPERATURE};
        Cursor cursor =db.query(TemperatureDbHelper.TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(TemperatureDbHelper.UID));
            String city =cursor.getString(cursor.getColumnIndex(TemperatureDbHelper.CITY));
            String  temperature =cursor.getString(cursor.getColumnIndex(TemperatureDbHelper.TEMPERATURE));
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

        int count =db.delete(TemperatureDbHelper.TABLE_NAME ,TemperatureDbHelper.CITY+" = ?",whereArgs);
        return  count;
    }

    static class TemperatureDbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "myDatabase";    // Database Name
        private static final String TABLE_NAME = "myTable";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String CITY = "City";    //Column II
        private static final String TEMPERATURE= "Temperature";    // Column III
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CITY+" VARCHAR(255) ,"+ TEMPERATURE+" VARCHAR(225));";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private Context context;

        public TemperatureDbHelper(Context context) {
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