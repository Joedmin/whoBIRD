package org.tensorflow.lite.examples.soundclassifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.*;

public class BirdDBHelper extends SQLiteOpenHelper {

    // Database name and table columns
    private static final String DB_NAME = "BirdDatabase.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "BirdObservations";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_MILLIS = "TimeInMillis";
    private static final String COLUMN_LATITUDE = "Latitude";
    private static final String COLUMN_LONGITUDE = "Longitude";
    private static final String COLUMN_NAME = "SpeciesName";
    private static final String COLUMN_SPECIES_ID = "BirdNET_ID";
    private static final String COLUMN_PROBABILITY = "Probability";
    private static BirdDBHelper instance = null;
    
    public BirdDBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table for bird observations with all columns and their data types.
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_MILLIS + " LONG," +
                COLUMN_LATITUDE + " FLOAT," +
                COLUMN_LONGITUDE + " FLOAT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_SPECIES_ID + " INTEGER," +
                COLUMN_PROBABILITY + " FLOAT);";
        db.execSQL(CREATE_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table and create it again if there's a version change in the database schema.
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        this.onCreate(db);
    }
    
    public synchronized void addEntry(String name, float latitude, float longitude, int speciesId, float probability) {
        // Insert a new row into the table with all columns and their values from parameters.
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_MILLIS, System.currentTimeMillis()); // time in milliseconds
        cv.put(COLUMN_LATITUDE, latitude);
        cv.put(COLUMN_LONGITUDE, longitude);
        cv.put(COLUMN_SPECIES_ID, speciesId);
        cv.put(COLUMN_PROBABILITY, probability);
        
        db.insert(TABLE_NAME, null, cv); // Insert the row into the table with all columns and their values from parameters.
    }
    
    public synchronized void clearAllEntries() {
        SQLiteDatabase db = getWritableDatabase();
        String CLEAR_TABLE = "DELETE FROM "+ TABLE_NAME;
        
        db.execSQL(CLEAR_TABLE); // Delete all rows in the table, effectively clearing it out.
    }
    
    public synchronized List<String> exportAllEntriesAsCSV() {
        SQLiteDatabase db = getReadableDatabase();
        String SELECT_ALL = "SELECT * FROM "+ TABLE_NAME;
        
        Cursor cursor = db.rawQuery(SELECT_ALL, null); // Execute the query to select all rows from the table and store them in a cursor object for further processing.
        
        List<String> csvDataList = new ArrayList<>(); // Create an empty list of strings that will hold each row's data as CSV formatted string.
        
        if (cursor != null && cursor.moveToFirst()) { 
            do {
                long millis = cursor.getLong(1);        // time in milliseconds
                float latitude = cursor.getFloat(2);    // latitude
                float longitude = cursor.getFloat(3);   // longitude
                String nameStr = cursor.getString(4);   // name of the bird species
                int speciesId = cursor.getInt(5);       // id for the species in BirdNET
                float probability = cursor.getFloat(6); // estimated probability that this observation is correct
                
                String csvString = millis + "," + latitude + "," + longitude + "," + nameStr + "," + speciesId + "," + probability;

                csvDataList.add(csvString);    
            } while (cursor.moveToNext());
            cursor.close();
        }
        return csvDataList;
    }
    public static BirdDBHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new BirdDBHelper(context.getApplicationContext());
        }
        return instance;
    }
}
