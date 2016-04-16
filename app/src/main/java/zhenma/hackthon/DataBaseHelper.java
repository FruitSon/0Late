package zhenma.hackthon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuehanyu on 4/15/16.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "Calender_Event";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "EventTable";

    public DataBaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create table Orders(Id integer primary key, CustomName text, OrderPrice integer, Country text);
        String sql = "create table if not exists " + TABLE_NAME + " (ID text primary key, Date text, Summary text, Time text, Transport integer)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    public void checkAndUpdate(String eventID, String Date, String Summary, String Time){
        int result = -1;
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("select Transport from "+TABLE_NAME+" where ID=?", new String[]{eventID});
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                result = cursor.getInt(0);
                db.execSQL("delete from "+TABLE_NAME+" where ID=?", new String[]{eventID});
            }
            ContentValues values = new ContentValues();
            values.put("ID", eventID);
            values.put("Date", Date);
            values.put("Summary", Summary);
            values.put("Time", Time);
            values.put("Transport",result);
            db.insert(TABLE_NAME, null, values);
        }catch(Exception exc){
            Log.d("dataSet", exc.getMessage());
        }
    }

    public void deleteRemoved(List<String> eventIDS, String Date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select ID from "+TABLE_NAME+" where Date=?", new String[]{Date});
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String thisID = cursor.getString(0);
                boolean flag = false;
                for(int i = 0 ; i < eventIDS.size() ; ++i){
                    if(thisID.equals(eventIDS.get(i))){
                        flag = true;
                        break;
                    }
                }
                if(!flag)
                    db.execSQL("delete from " + TABLE_NAME + " where ID=?", new String[]{thisID});
                cursor.moveToNext();
            }
        }
    }

    public void updateTransport(String ID, int transport){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("update " + TABLE_NAME + " set Transport=? where ID=?", new String[]{transport + "", ID});
    }

    public List<ItemData> getEvents(String Date){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " where Date=?", new String[]{Date});
        List<ItemData> result = new ArrayList<>();
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ItemData thisEvent = new ItemData(Color.parseColor("#76A9FC"), R.mipmap.ic_assessment_white_24dp,
                        cursor.getString(2), cursor.getString(1), R.mipmap.walk_g,
                        R.mipmap.drive_g, R.mipmap.bus_g, cursor.getString(0), Date.toString());
                switch (cursor.getInt(4)){
                    case 0:
                        thisEvent.walk = R.mipmap.walk_c;
                        break;
                    case 1:
                        thisEvent.drive = R.mipmap.drive_c;
                        break;
                    case 2:
                        thisEvent.bus = R.mipmap.bus_c;
                        break;
                }
                result.add(thisEvent);
                cursor.moveToNext();
            }
        }
        return result;
    }

    public int getTransport(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select Transport from " + TABLE_NAME + " where ID=?", new String[]{id});
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                return cursor.getInt(0);
            }
        }
        return -1;
    }
}
