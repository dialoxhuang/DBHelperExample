/*  Dialox Huang(dialox@qq.com) 
 *
 *  Filename:
 *  -----------------------------------------------------------------
 *  YYYY-mm-dd  ticket#XXX
 */

/**
 * Created by changh on 15/5/18.
 */


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static DBHelper mInstance = null;

    SQLiteDatabase mDB = null;

    static Context mContext = null;

    static String currentChannelList;

    /**
     * Standard Keys
     */
    public static final String DB_KEY_SELECTED_CHANNEL_LISTNAME = "selected_channel_list_name";
    public static final String DB_KEY_CURRENT_LCN = "current_lcn";



    /**
     * Database scheme
     */
    public static final String DB_NAME = "dbhelper";
    public static final int DB_VERSION = 100;    // for version 1.0.0

    public static final String TABLE_CHANNELLIST = "channel_list";
    public static final String TABLE_FREQLIST = "freq_list";
    public static final String TABLE_KV = "key_value";

    public static final String FIELD_ID = "id";
    public static final String FIELD_LISTNAME = "list_name";
    public static final String FIELD_LCN = "lcn";
    public static final String FIELD_CHANNELNAME = "channel_name";
    public static final String FIELD_CHANNELTYPE = "channel_type";
    public static final String FIELD_FAVORITE = "favorite";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_FREQ = "freq";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";

    public static final String CREATE_TABLE_CHANNELLIST = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CHANNELLIST + " ( "
            + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FIELD_LISTNAME + " VARCHAR NOT NULL,"
            + FIELD_LCN + " VARCHAR NOT NULL,"
            + FIELD_CHANNELNAME + " VARCHAR NOT NULL,"
            + FIELD_CHANNELTYPE + " VARCHAR NOT NULL DEFAULT " + Channel.CHANNEL_TYPE_TV + ","
            + FIELD_FAVORITE + " INTEGER NOT NULL DEFAULT 0,"
            + FIELD_LOCATION + " VARCHAR NOT NULL"
            + ");";

    public static final String CREATE_INDEX_BY_LISTNAME_ON_CHANNELLIST = "CREATE INDEX IF NOT EXISTS "
            + DB_NAME + "." + "BY_LISTNAME "
            + "ON " + TABLE_CHANNELLIST + " ( "
            + FIELD_LISTNAME + ","
            + FIELD_LCN
            + ");";

    public static final String CREATE_INDEX_BY_TYPE_ON_CHANNELLIST = "CREATE INDEX IF NOT EXISTS "
            + DB_NAME + "." + "BY_TYPE "
            + "ON " + TABLE_CHANNELLIST + " ( "
            + FIELD_LISTNAME + ","
            + FIELD_CHANNELTYPE
            + ");";

    public static final String CREATE_INDEX_BY_FAVORITE_ON_CHANNELLIST = "CREATE INDEX IF NOT EXISTS "
            + DB_NAME + "." + "BY_FAVORITE "
            + "ON " + TABLE_CHANNELLIST + " ( "
            + FIELD_LISTNAME + ","
            + FIELD_CHANNELTYPE + ","
            + FIELD_FAVORITE
            + ");";

    public static final String CREATE_TABLE_FREQLIST = "CREATE TABLE IF NOT EXISTS "
            + TABLE_FREQLIST + " ( "
            + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FIELD_LOCATION + " VARCHAR NOT NULL,"
            + FIELD_FREQ + " INTEGER NOT NULL"
            + ");";

    public static final String CREATE_TABLE_KV = "CREATE TABLE IF NOT EXISTS "
            + TABLE_KV + " ( "
            + FIELD_KEY + " VARCHAR PRIMARY KEY,"
            + FIELD_VALUE + " VARCHAR NOT NULL DEFAULT ''"
            + ");";

    /**
     * Single instance for DB
     * @param context
     * @throws SQLiteException
     */
    public DBHelper(Context context) throws SQLiteException{
        super(context, DB_NAME, null, DB_VERSION);

        try {
            mDB = getWritableDatabase();
        }
        catch (Exception e) {
            mDB = getReadableDatabase();
        }

        mContext = context;
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
        }

        return mInstance;

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table & index
        db.execSQL(CREATE_TABLE_CHANNELLIST);
//        db.execSQL(CREATE_INDEX_BY_LISTNAME_ON_CHANNELLIST);
//        db.execSQL(CREATE_INDEX_BY_TYPE_ON_CHANNELLIST);
//        db.execSQL(CREATE_INDEX_BY_FAVORITE_ON_CHANNELLIST);
        db.execSQL(CREATE_TABLE_FREQLIST);
        db.execSQL(CREATE_TABLE_KV);

        // setup the initial data
        setupInitData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * This method helps to build up the initial data into the database
     */
    public void setupInitData(SQLiteDatabase db) {
        // a class holds the scope definition fields
        class FreqScope {
            public String mArea;        // Area name, i.e. CHINA
            public int mBegin;          // Low boundary freq in MHz
            public int mEnd;            // High boundary freq in MHz
            public int mStep;           // channel bandwidth in MHz

            // A base constructor
            public FreqScope(String area, int begin, int end, int step) {
                mArea = area;
                mBegin = begin;
                mEnd = end;
                mStep = step;
            }
        }


        mDB = db;

        // frequency scope definition here
        ArrayList<FreqScope> scopeArray = new ArrayList<FreqScope>();
        scopeArray.add(new FreqScope("CHINA", 474000, 858000, 8000));
        scopeArray.add(new FreqScope("EUROPE", 474000, 858000, 8000));
        scopeArray.add(new FreqScope("AUSTRALIA", 474000, 858000, 7000));

        for (FreqScope scope : scopeArray) {
            String area = scope.mArea;
            for (int i=scope.mBegin; i<=scope.mEnd; i+=scope.mStep) {
                ContentValues values = new ContentValues();
                values.put(FIELD_ID, (String)null);
                values.put(FIELD_LOCATION, area);
                values.put(FIELD_FREQ, i);

                insert(TABLE_FREQLIST, values);
            }
        }

        // add channel list data
        ArrayList<Channel> channelArray = new ArrayList<Channel>();
        channelArray.add( new Channel("11", "CCTV-1", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("12", "CCTV-2", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("13", "CCTV-3", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("14", "CCTV-4", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("15", "CCTV-5", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("16", "CCTV-6", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("17", "CCTV-7", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("18", "CCTV-8", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("19", "CCTV-9", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("20", "CCTV-10", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("21", "CCTV-11", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("22", "CCTV-12", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("23", "CCTV-News", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("24", "CCTV-儿童", Channel.CHANNEL_TYPE_TV) );
        channelArray.add( new Channel("25", "CCTV-戏曲", Channel.CHANNEL_TYPE_TV) );

        for (Channel chn:channelArray ) {
            ContentValues values = new ContentValues();
            values.put(FIELD_ID, (String)null );
            values.put(FIELD_LISTNAME, "CHINA");
            values.put(FIELD_LCN, chn.lcn );
            values.put(FIELD_CHANNELNAME, chn.channelName );
            values.put(FIELD_CHANNELTYPE, chn.channelType);
            values.put(FIELD_LOCATION, "CHINA");
            long ret = insert(TABLE_CHANNELLIST, values);
        }

    }
    /**
     * Insert a record into a table
     * @param table
     * @param values
     * @return
     */
    public long insert(String table, ContentValues values) {
        return mDB.insert(table, null, values);
    }

    /**
     * SQL query
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @return
     */
    public Cursor query(String table,
                        String[] columns,
                        String selection,
                        String[] selectionArgs,
                        String groupBy, String having, String orderBy, String limit) {
        return mDB.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Execute SQL directly
     * @param sql
     * @return
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {

        return mDB.rawQuery(sql, selectionArgs);
    }

    /**
     * Delete a record
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public long delete(String table, String whereClause, String[] whereArgs) {
        return mDB.delete(table, whereClause, whereArgs);
    }

    /**
     * Update records
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public long update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return mDB.update(table, values, whereClause, whereArgs);
    }

    /**
     * Delete all records in a table
     * @param table
     * @return
     */
    public long deleteAll(String table) {

        return mDB.delete(table, null, null);
    }

    /**
     * High level DB APIs
     */

    /**
     * Store a k-v pair, key will be converted into UpperCase only in database
     * @param key
     * @param value
     * @return
     */
    public long storeKeyValue(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_VALUE, value);

        String oldValue = loadKeyValue(key);
        if (oldValue == null) {    // Not in yet, insert it
            cv.put(FIELD_KEY, key.toUpperCase());
            return insert(TABLE_KV, cv);
        } else { // Already in, need to update
            if (value.equals(oldValue))
                return 0;
            else
                return update(TABLE_KV, cv, "key=?", new String[] {key.toUpperCase()});
        }
    }

    /**
     * Load value by key
     * @param key
     * @return
     */
    public String loadKeyValue(String key) {
        Cursor cursor = query( TABLE_KV,
                                new String[] {FIELD_VALUE},
                                "key=?",
                                new String[] {key.toUpperCase()},
                                null,
                                null,
                                null,
                                "1");

        String ret=null;

        if ( cursor.moveToNext() ) {
            ret = cursor.getString(cursor.getColumnIndex(FIELD_VALUE));
        }

        return ret;
    }

    /**
     * Load value by key, if key doesn't exist, return the default value
     * @param key
     * @param defaultValue
     * @return
     */
    public String loadKeyValue(String key, String defaultValue) {
        String ret = loadKeyValue(key);
        if (ret==null)
            ret = defaultValue;

        return ret;
    }

    /**
     * Get the freq location list
     * @return An ArrayList\<String\>
     */
    public ArrayList<String> getFreqNameList() {
        // get all location strings in freq table
        Cursor cursor = query( TABLE_FREQLIST,                  // table name
                                new String[] {FIELD_LOCATION},  // get location field
                                null,                           // no select
                                null,                           // no select args
                                FIELD_LOCATION,                 // group by
                                null,                           // having
                                FIELD_LOCATION,                 // order by
                                null);                          // no limit

        if ( cursor == null )
            return null;

        // look through the locations
        ArrayList<String> ret = new ArrayList<String>();
        while (cursor.moveToNext()) {
            ret.add(cursor.getString(cursor.getColumnIndex(FIELD_LOCATION)));
        }

        // close the cursor
        cursor.close();

        return ret;
    }

    /**
     * Get the frequency list by location
     * @param location
     * @return
     */
    public ArrayList<Integer> getFreq(String location) {
        Cursor cursor = query( TABLE_FREQLIST,              // table name
                                new String[] {FIELD_FREQ},
                                FIELD_LOCATION + "=?",      // selection
                                new String[] {location},
                                null,
                                null,
                                FIELD_FREQ,
                                null
        );
        if (cursor==null)
            return null;

        ArrayList<Integer> ret = new ArrayList<Integer>();
        while (!cursor.moveToNext())
        {
            ret.add(new Integer(cursor.getInt(cursor.getColumnIndex(FIELD_FREQ))));
        }
        cursor.close();


        return ret;
    }

    /**
     * Save channel list into database
     * @param chnList
     * @return
     */
    public boolean saveChannelList(String listName, ArrayList<Channel> chnList) {
        Cursor cursor = query( TABLE_CHANNELLIST,
                                null,
                                FIELD_CHANNELNAME + "=?",
                                new String[] {listName},
                                null,
                                null,
                                FIELD_LCN,
                                null
        );

        // need to remove the existing channels?
        if (cursor!=null && cursor.getCount()>0 ) {
            delete(TABLE_CHANNELLIST, FIELD_CHANNELNAME, new String[]{listName});
        }

        // insert into table
        int i=0;
        for (Channel channel:chnList) {
            ContentValues values = new ContentValues();
            values.put(FIELD_LISTNAME, listName);
            values.put(FIELD_CHANNELNAME, channel.channelName);
            values.put(FIELD_CHANNELTYPE, channel.channelType);
            values.put(FIELD_LCN, channel.lcn);
            values.put(FIELD_LOCATION,"N/A");
            insert(TABLE_CHANNELLIST, values);
            i++;
        }

        return true;
    }

    /**
     * Load channel list by channel list name
     * @param listName
     * @return An ArrayList<Channel>
     */
    public ArrayList<Channel> loadChannelList(String listName, String channelType) {
        Cursor cursor = query(TABLE_CHANNELLIST,
                null,
                FIELD_LISTNAME + "=? AND " + FIELD_CHANNELTYPE + "=?",
                new String[] {listName, channelType},
                null,
                null,
                FIELD_LCN,
                null);
        ArrayList<Channel> channelList = new ArrayList<Channel>();

        if (cursor==null || cursor.getCount()==0){
            return channelList;
        }


        while (cursor.moveToNext()) {
            Channel channel = new Channel(
                    cursor.getString(cursor.getColumnIndex(FIELD_LCN)),
                    cursor.getString(cursor.getColumnIndex(FIELD_CHANNELNAME)),
                    cursor.getString(cursor.getColumnIndex(FIELD_CHANNELTYPE))
            );

            if (channel!=null)
                channelList.add(channel);

        }

        // save the current channel list name
        if (channelList.size() > 0) {
            long ret = storeKeyValue(DB_KEY_SELECTED_CHANNEL_LISTNAME, listName);
            // reset current lcn to ""
            if (ret!=0)
                storeKeyValue(DB_KEY_CURRENT_LCN, "");
        }

        return channelList;
    }

    /**
     * Set favorite flag for a channel
     * @param listName
     * @param lcn
     * @param favorite
     * @return
     */
    public boolean setFavorite(String listName, String lcn, boolean favorite) {

        ContentValues value = new ContentValues();
        value.put(FIELD_FAVORITE, (favorite)?1:0);
        update(TABLE_CHANNELLIST,
                value,
                FIELD_LISTNAME + "=? AND " + FIELD_LCN + "=?",
                new String[]{listName, lcn}
        );
        return true ;
    }

    /**
     * Get the area name list from FreqList table
     * @return
     */
    public String[] getAreaList() {
        Cursor cursor = query(TABLE_FREQLIST,
                new String[] {FIELD_LOCATION},
                null,
                null,
                FIELD_LOCATION,
                null,
                FIELD_LOCATION,
                null);

        if (cursor==null || cursor.getCount()==0) {
            return new String[] {};
        }

        String[] ret = new String[cursor.getCount()];
        int index=0;
        while (cursor.moveToNext()) {
            ret[index++] = cursor.getString(cursor.getColumnIndex(FIELD_LOCATION));

        }

        return ret;
    }


}
