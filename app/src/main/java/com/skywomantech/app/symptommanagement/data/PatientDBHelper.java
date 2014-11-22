package com.skywomantech.app.symptommanagement.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.skywomantech.app.symptommanagement.data.PatientCPContract.CredentialEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PhysicianEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrefsEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.CheckInLogEntry;

/**
 * This is a helper for the SQLite database used to store local information
 */
public class PatientDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // We only store the data locally if the user is patient
    // admin users must work with online data only
    // physicians also work with online data only.. not doing any storing of patient data
    public static final String DATABASE_NAME = "patient.db";

    public PatientDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * create the database tables when creating a new database
     * This should make multiple patients on multiple devices work with this design now
     *
     * @param sqLiteDatabase database we are working with
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_PATIENT_TABLE =
                "CREATE TABLE " + PatientEntry.TABLE_NAME + " (" +
                        PatientEntry._ID + " INTEGER PRIMARY KEY," +
                        PatientEntry.COLUMN_PATIENT_ID + " TEXT UNIQUE NOT NULL, " +
                        PatientEntry.COLUMN_FIRST_NAME + " TEXT, " +
                        PatientEntry.COLUMN_LAST_NAME + " TEXT, " +
                        PatientEntry.COLUMN_LAST_LOGIN + " REAL, " +
                        PatientEntry.COLUMN_BIRTHDATE + " TEXT, " +
                        PatientEntry.COLUMN_ACTIVE + " INTEGER, " +
                        PatientEntry.COLUMN_PROCESS_STATUS + " INTEGER, " +
                        PatientEntry.COLUMN_PROCESSED + " REAL, " +
                        "UNIQUE (" + PatientEntry.COLUMN_PATIENT_ID
                        + ") ON CONFLICT REPLACE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PATIENT_TABLE);

        final String SQL_CREATE_CREDENTIAL_TABLE =
                "CREATE TABLE " + CredentialEntry.TABLE_NAME + " (" +
                        CredentialEntry._ID + " INTEGER PRIMARY KEY," +
                        CredentialEntry.COLUMN_USER_ID + " TEXT UNIQUE NOT NULL, " +
                        CredentialEntry.COLUMN_USER_NAME + " TEXT, " +
                        CredentialEntry.COLUMN_PASSWORD + " TEXT, " +
                        CredentialEntry.COLUMN_LAST_LOGIN + " REAL, " +
                        CredentialEntry.COLUMN_USER_TYPE_VALUE + " REAL, " +
                        "UNIQUE (" + CredentialEntry.COLUMN_USER_ID
                        + ") ON CONFLICT REPLACE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_CREDENTIAL_TABLE);

        final String SQL_CREATE_PRESCRIPTION_TABLE =
                "CREATE TABLE " + PrescriptionEntry.TABLE_NAME + " (" +
                        PrescriptionEntry._ID + " INTEGER PRIMARY KEY," +
                        PrescriptionEntry.COLUMN_PATIENT_ID + " TEXT  NOT NULL, " +
                        PrescriptionEntry.COLUMN_MEDICATION_ID + " TEXT NOT NULL, " +
                        PrescriptionEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        " UNIQUE (" + PrescriptionEntry.COLUMN_PATIENT_ID + ", "
                        + PrescriptionEntry.COLUMN_MEDICATION_ID
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PRESCRIPTION_TABLE);

        final String SQL_CREATE_PHYSICIAN_TABLE =
                "CREATE TABLE " + PhysicianEntry.TABLE_NAME + " (" +
                        PhysicianEntry._ID + " INTEGER PRIMARY KEY," +
                        PhysicianEntry.COLUMN_PATIENT_ID + " TEXT  NOT NULL, " +
                        PhysicianEntry.COLUMN_PHYSICIAN_ID + " TEXT NOT NULL, " +
                        PhysicianEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        " UNIQUE (" + PhysicianEntry.COLUMN_PATIENT_ID + ", "
                        + PhysicianEntry.COLUMN_PHYSICIAN_ID
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PHYSICIAN_TABLE);

        final String SQL_CREATE_CHECK_IN_LOG_TABLE =
                "CREATE TABLE " + CheckInLogEntry.TABLE_NAME + " (" +
                        CheckInLogEntry._ID + " INTEGER PRIMARY KEY," +
                        CheckInLogEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        CheckInLogEntry.COLUMN_CHECKIN_ID + " REAL, " +
                        CheckInLogEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        " UNIQUE (" + CheckInLogEntry.COLUMN_PATIENT_ID + ", "
                        + CheckInLogEntry.COLUMN_CREATED
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_CHECK_IN_LOG_TABLE);

        final String SQL_CREATE_PAIN_LOG_TABLE =
                "CREATE TABLE " + PainLogEntry.TABLE_NAME + " (" +
                        PainLogEntry._ID + " INTEGER PRIMARY KEY," +
                        PainLogEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        PainLogEntry.COLUMN_PAIN_LOG_ID + " BIGINT, " +
                        PainLogEntry.COLUMN_SEVERITY + " INTEGER, " +
                        PainLogEntry.COLUMN_EATING + " INTEGER, " +
                        PainLogEntry.COLUMN_CHECKIN_ID + " REAL, " +
                        PainLogEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        " UNIQUE (" + PainLogEntry.COLUMN_PATIENT_ID + ", "
                        + PainLogEntry.COLUMN_CREATED
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PAIN_LOG_TABLE);

        final String SQL_CREATE_MED_LOG_TABLE =
                "CREATE TABLE " + MedLogEntry.TABLE_NAME + " (" +
                        MedLogEntry._ID + " INTEGER PRIMARY KEY," +
                        MedLogEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        MedLogEntry.COLUMN_MED_ID + " BIGINT, " +
                        MedLogEntry.COLUMN_MED_NAME + " TEXT NOT NULL, " +
                        MedLogEntry.COLUMN_TAKEN + " REAL NOT NULL, " +
                        MedLogEntry.COLUMN_CHECKIN_ID + " REAL, " +
                        MedLogEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        " UNIQUE (" + MedLogEntry.COLUMN_PATIENT_ID + ", "
                        + MedLogEntry.COLUMN_CREATED
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MED_LOG_TABLE);

        final String SQL_CREATE_STATUS_LOG_TABLE =
                "CREATE TABLE " + StatusLogEntry.TABLE_NAME + " (" +
                        StatusLogEntry._ID + " INTEGER PRIMARY KEY," +
                        StatusLogEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        StatusLogEntry.COLUMN_STATUS_LOG_ID + " BIGINT, " +
                        StatusLogEntry.COLUMN_NOTE + " TEXT, " +
                        StatusLogEntry.COLUMN_IMAGE + " TEXT, " +
                        StatusLogEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        " UNIQUE (" + StatusLogEntry.COLUMN_PATIENT_ID + ", "
                        + StatusLogEntry.COLUMN_CREATED
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_STATUS_LOG_TABLE);

        final String SQL_CREATE_REMINDER_TABLE =
                "CREATE TABLE " + ReminderEntry.TABLE_NAME + " (" +
                        ReminderEntry._ID + " INTEGER PRIMARY KEY," +
                        ReminderEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        ReminderEntry.COLUMN_REMINDER_ID + " BIGINT, " +
                        ReminderEntry.COLUMN_NAME + " TEXT, " +
                        ReminderEntry.COLUMN_TYPE + " TEXT, " +
                        ReminderEntry.COLUMN_DAY + " INTEGER, " +
                        ReminderEntry.COLUMN_HOUR + " INTEGER, " +
                        ReminderEntry.COLUMN_MINUTES + " INTEGER, " +
                        ReminderEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        ReminderEntry.COLUMN_ALARM + " TEXT, " +
                        ReminderEntry.COLUMN_ON + " INTEGER  NOT NULL, " +
                        " UNIQUE (" + ReminderEntry.COLUMN_PATIENT_ID + ", "
                        + ReminderEntry.COLUMN_CREATED
                        + ") ON CONFLICT REPLACE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_REMINDER_TABLE);

        final String SQL_CREATE_PREFS_TABLE =
                "CREATE TABLE " + PrefsEntry.TABLE_NAME + " (" +
                        PrefsEntry._ID + " INTEGER PRIMARY KEY," +
                        PrefsEntry.COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                        PrefsEntry.COLUMN_PREF_ID + " BIGINT, " +
                        PrefsEntry.COLUMN_NOTIFICATION + " INTEGER, " +
                        PrefsEntry.COLUMN_TIMEZONE + " TEXT, " +
                        PrefsEntry.COLUMN_CREATED + " REAL NOT NULL, " +
                        " UNIQUE (" + PrefsEntry.COLUMN_PATIENT_ID + ", "
                        + PrefsEntry.COLUMN_CREATED
                        + ") ON CONFLICT IGNORE " +
                        " );";
        sqLiteDatabase.execSQL(SQL_CREATE_PREFS_TABLE);
    }

    /**
     * delete the old tables and create new ones if upgrading the database to a new version
     *
     * @param sqLiteDatabase  database we are working with
     * @param oldVersion previous version number
     * @param newVersion new version number for comparison
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is mostly only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // We are assuming here that all patient data was stored previous to this update
        // We are only storing data locally if the user is Patient
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PatientEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CredentialEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PrescriptionEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PhysicianEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CheckInLogEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PainLogEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MedLogEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StatusLogEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PrefsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
