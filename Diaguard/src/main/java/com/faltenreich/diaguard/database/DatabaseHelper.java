package com.faltenreich.diaguard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.faltenreich.diaguard.database.measurements.Activity;
import com.faltenreich.diaguard.database.measurements.BloodSugar;
import com.faltenreich.diaguard.database.measurements.HbA1c;
import com.faltenreich.diaguard.database.measurements.Insulin;
import com.faltenreich.diaguard.database.measurements.Meal;
import com.faltenreich.diaguard.database.measurements.Pressure;
import com.faltenreich.diaguard.database.measurements.Pulse;
import com.faltenreich.diaguard.database.measurements.Weight;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Filip on 20.10.13.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // Metadata
    private static final String DATABASE_NAME = "diaguard.db";

    private static final int DATABASE_VERSION_1_0 = 17;
    private static final int DATABASE_VERSION_1_1 = 18;
    private static final int DATABASE_VERSION_1_3 = 19;
    private static final int DATABASE_VERSION_CURRENT = DATABASE_VERSION_1_3;

    public static final Class[] tables = new Class[] {
            Entry.class,
            Food.class,
            BloodSugar.class,
            Insulin.class,
            Meal.class,
            Activity.class,
            HbA1c.class,
            Weight.class,
            Pulse.class,
            Pressure.class
    };

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION_CURRENT);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        for (Class tableClass : tables) {
            try {
                TableUtils.createTable(connectionSource, tableClass);
            }
            catch (SQLException exception) {
                Log.e(DatabaseHelper.class.getName(), "Couldn't create table " + tableClass.getSimpleName(), exception);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if(oldVersion < newVersion) {
            onCreate(sqLiteDatabase);
            int upgradeFromVersion = oldVersion;
            while (upgradeFromVersion < newVersion) {
                switch (upgradeFromVersion) {
                    case DATABASE_VERSION_1_0:
                        upgradeToVersion18(sqLiteDatabase);
                        break;
                    case DATABASE_VERSION_1_1:
                        upgradeToVersion19(sqLiteDatabase);
                        break;
                }
                upgradeFromVersion++;
            }
        }
    }

    private void upgradeToVersion19(SQLiteDatabase sqliteDatabase) {
        // TODO
    }

    // region Deprecated

    public static final String DESCENDING = " DESC";
    public static final String ASCENDING = " ASC";

    // Primary key: Underscore for CursorAdapter to work
    public static final String ID = "_id";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";

    // Entry
    public static final String ENTRY = "entry";
    public static final String DATE = "date";
    public static final String NOTE = "note";
    public static final String IS_VISIBLE = "is_visible";

    public static final String VALUE = "value";
    public static final String ENTRY_ID = "entry_id";

    // Food
    public static final String FOOD = "food";
    public static final String CARBOHYDRATES = "carbohydrates";
    public static final String NAME = "name";
    public static final String IMAGE = "image";

    // Blood Sugar
    public static final String BLOODSUGAR = "bloodsugar";
    public static final String MGDL = "mgdl";

    // Insulin
    public static final String INSULIN = "insulin";
    public static final String BOLUS = "insulin";
    public static final String CORRECTION = "correction";
    public static final String BASAL = "basal";

    // Meal
    public static final String MEAL = "meal";
    public static final String FOOD_ID = "food_id";

    // Activity
    public static final String ACTIVITY = "activity";
    public static final String MINUTES = "minutes";
    public static final String TYPE = "type";

    // HbA1c
    public static final String HBA1C = "hba1c";
    public static final String PERCENT = "percent";

    // Weight
    public static final String WEIGHT = "weight";
    public static final String KILOGRAM = "kilogram";

    // Pulse
    public static final String PULSE = "pulse";
    public static final String FREQUENCY = "frequency";

    // Pressure
    public static final String PRESSURE = "pressure";
    public static final String SYSTOLIC = "systolic";
    public static final String DIASTOLIC = "diastolic";

    // Deprecated
    public static final String EVENTS = "events";
    public static final String NOTES = "notes";
    public static final String MEASUREMENT = "measurement";
    public static final String CATEGORY = "category";
    public static final String MEASUREMENT_ID = "measurementId";
    public static final String FOOD_EATEN = "food_eaten";

    private void onCreateVersion17(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                EVENTS + " (" +
                ID + " INTEGER PRIMARY KEY, " +
                VALUE + " REAL NOT NULL, " +
                DATE + " TEXT NOT NULL, " +
                NOTES + " TEXT, " +
                CATEGORY + " TEXT NOT NULL);");
    }

    private void onCreateVersion18(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                ENTRY + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DATE + " TEXT NOT NULL, " +
                NOTE + " TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                MEASUREMENT + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VALUE + " REAL NOT NULL, " +
                CATEGORY + " TEXT NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + " (" + ID + ") ON DELETE CASCADE);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                FOOD + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CARBOHYDRATES + " REAL NOT NULL, " +
                NAME + " TEXT NOT NULL);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                FOOD_EATEN + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEASUREMENT_ID + " INTEGER, " +
                FOOD_ID + " INTEGER, " +
                "FOREIGN KEY(" + MEASUREMENT_ID + ") REFERENCES " + MEASUREMENT + " (" + ID + ") ON UPDATE CASCADE, " +
                "FOREIGN KEY(" + FOOD_ID + ") REFERENCES " + FOOD + " (" + ID + ") ON DELETE CASCADE);");
    }

    private void onCreateVersion19Deprecated(SQLiteDatabase sqLiteDatabase) {

        // DEPRECATED

        sqLiteDatabase.execSQL("DROP TABLE IF  EXISTS " + MEASUREMENT + ";");
        sqLiteDatabase.execSQL("DROP TABLE IF  EXISTS " + FOOD + ";");
        sqLiteDatabase.execSQL("DROP TABLE IF  EXISTS " + FOOD_EATEN + ";");

        // GENERAL

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                ENTRY + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                DATE + " TEXT NOT NULL, " +
                NOTE + " TEXT, " +
                IS_VISIBLE + " INT DEFAULT 1);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                FOOD + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                NAME + " TEXT NOT NULL, " +
                IMAGE + " TEXT);");

        // MEASUREMENTS

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                BLOODSUGAR + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                MGDL + " REAL NOT NULL," +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                INSULIN + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                BOLUS + " REAL, " +
                CORRECTION + " REAL, " +
                BASAL + " REAL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                MEAL + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                CARBOHYDRATES + " REAL NOT NULL, " +
                FOOD_ID + " INTEGER, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + FOOD_ID + ") REFERENCES " + FOOD + "(" + ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                ACTIVITY + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                MINUTES + " INTEGER NOT NULL, " +
                TYPE + " INTEGER NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                HBA1C + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                PERCENT + " REAL NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                WEIGHT + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                KILOGRAM + " REAL NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                PULSE + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                FREQUENCY + " REAL NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                PRESSURE + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CREATED_AT + " TEXT, " +
                UPDATED_AT + " TEXT, " +
                SYSTOLIC + " REAL NOT NULL, " +
                DIASTOLIC + " REAL NOT NULL, " +
                ENTRY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ENTRY_ID + ") REFERENCES " + ENTRY + "(" + ID + ") ON DELETE CASCADE);");
    }

    private void upgradeToVersion18(SQLiteDatabase sqliteDatabase) {
        String query = "SELECT * FROM " + DatabaseHelper.EVENTS + " ORDER BY " + DatabaseHelper.DATE;
        Cursor cursor = sqliteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                // Entry
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.DATE, cursor.getString(2));
                values.put(DatabaseHelper.NOTE, cursor.getString(3));
                long entryId = sqliteDatabase.insertOrThrow(DatabaseHelper.ENTRY, null, values);

                // Measurement
                values = new ContentValues();
                values.put(DatabaseHelper.VALUE, Float.parseFloat(cursor.getString(1)));
                values.put(DatabaseHelper.CATEGORY, cursor.getString(4));
                values.put(DatabaseHelper.ENTRY_ID, entryId);
                sqliteDatabase.insertOrThrow(DatabaseHelper.MEASUREMENT, null, values);

                cursor.moveToNext();
            }
        }
        sqliteDatabase.execSQL("DROP TABLE IF EXISTS " + EVENTS + ";");
    }

    // endregion
}