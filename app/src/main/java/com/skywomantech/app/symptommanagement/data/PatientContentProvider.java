package com.skywomantech.app.symptommanagement.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static com.skywomantech.app.symptommanagement.data.PatientCPContract.CredentialEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PhysicianEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrefsEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;


public class PatientContentProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PatientDBHelper mOpenHelper;

    private static final int PATIENT = 100;
    private static final int PRESCRIPTION = 200;
    private static final int PRESCRIPTION_ID = 210;
    private static final int PHYSICIAN = 300;
    private static final int PHYSICIAN_ID = 310;
    private static final int PREF = 400;
    private static final int REMINDER = 500;
    private static final int REMINDER_ID = 510;
    private static final int PAIN_LOG = 600;
    private static final int PAIN_LOG_ID = 610;
    private static final int MED_LOG = 700;
    private static final int MED_LOG_ID = 710;
    private static final int STATUS_LOG = 800;
    private static final int STATUS_LOG_ID = 810;
    private static final int CREDENTIAL = 900;
    private static final int CREDENTIAL_ID = 910;


    @Override
    public boolean onCreate() {
        mOpenHelper = new PatientDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case PATIENT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PatientEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case CREDENTIAL: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CredentialEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case CREDENTIAL_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CredentialEntry.TABLE_NAME,
                        projection,
                        CredentialEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PREF: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PrefsEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PRESCRIPTION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PrescriptionEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PRESCRIPTION_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PrescriptionEntry.TABLE_NAME,
                        projection,
                        PrescriptionEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PHYSICIAN: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PhysicianEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PHYSICIAN_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PhysicianEntry.TABLE_NAME,
                        projection,
                        PhysicianEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }

            case REMINDER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ReminderEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case REMINDER_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ReminderEntry.TABLE_NAME,
                        projection,
                        ReminderEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }

            case PAIN_LOG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PainLogEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case PAIN_LOG_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PainLogEntry.TABLE_NAME,
                        projection,
                        PainLogEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case MED_LOG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MedLogEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case MED_LOG_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MedLogEntry.TABLE_NAME,
                        projection,
                        MedLogEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case STATUS_LOG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StatusLogEntry.TABLE_NAME,
                        projection,
                        selection,     // selection
                        selectionArgs, // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            case STATUS_LOG_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StatusLogEntry.TABLE_NAME,
                        projection,
                        StatusLogEntry._ID + "=" + ContentUris.parseId(uri),// selection
                        null,     // selection args
                        null,     // group by
                        null,     // having
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: + uri");

        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PATIENT: {
                return PatientEntry.CONTENT_ITEM_TYPE;
            }
            case CREDENTIAL: {
                return CredentialEntry.CONTENT_ITEM_TYPE;
            }
            case CREDENTIAL_ID: {
                return CredentialEntry.CONTENT_ITEM_TYPE;
            }
            case PREF: {
                return PrefsEntry.CONTENT_ITEM_TYPE;
            }
            case PRESCRIPTION: {
                return PrescriptionEntry.CONTENT_TYPE;
            }
            case PRESCRIPTION_ID: {
                return PrescriptionEntry.CONTENT_ITEM_TYPE;
            }
            case PHYSICIAN: {
                return PhysicianEntry.CONTENT_TYPE;
            }
            case PHYSICIAN_ID: {
                return PhysicianEntry.CONTENT_ITEM_TYPE;
            }
            case REMINDER: {
                return ReminderEntry.CONTENT_TYPE;
            }
            case REMINDER_ID: {
                return ReminderEntry.CONTENT_ITEM_TYPE;
            }
            case PAIN_LOG: {
                return PainLogEntry.CONTENT_TYPE;
            }
            case PAIN_LOG_ID: {
                return PainLogEntry.CONTENT_ITEM_TYPE;
            }
            case MED_LOG: {
                return MedLogEntry.CONTENT_TYPE;
            }
            case MED_LOG_ID: {
                return MedLogEntry.CONTENT_ITEM_TYPE;
            }
            case STATUS_LOG: {
                return StatusLogEntry.CONTENT_TYPE;
            }
            case STATUS_LOG_ID: {
                return StatusLogEntry.CONTENT_ITEM_TYPE;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: + uri");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri returnUri;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PATIENT: {
                long _id = db.insert(PatientEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PatientEntry.buildPatientEntryUriWithPatientId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case CREDENTIAL: {
                long _id = db.insert(CredentialEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = CredentialEntry.buildCredentialEntryUriWithDBId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PREF: {
                long _id = db.insert(PrefsEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PrefsEntry.buildPrefsEntryUriWithPrefId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PRESCRIPTION: {
                long _id = db.insert(PrescriptionEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PrescriptionEntry.buildPrescriptionEntryUriWithPrescriptionId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PHYSICIAN: {
                long _id = db.insert(PhysicianEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PhysicianEntry.buildPhysicianEntryUriWithPhysicianId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REMINDER: {
                long _id = db.insert(ReminderEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = ReminderEntry.buildReminderEntryUriWithId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PAIN_LOG: {
                long _id = db.insert(PainLogEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = PainLogEntry.buildPainLogEntryUriWithLogId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MED_LOG: {
                long _id = db.insert(MedLogEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MedLogEntry.buildMedLogEntryUriWithLogId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case STATUS_LOG: {
                long _id = db.insert(StatusLogEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = StatusLogEntry.buildStatusLogEntryUriWithLogId(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: + uri");

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PATIENT: {
                rowsDeleted = db.delete(PatientEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case CREDENTIAL: {
                rowsDeleted = db.delete(CredentialEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PREF: {
                rowsDeleted = db.delete(PrefsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PRESCRIPTION: {
                rowsDeleted = db.delete(PrescriptionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PHYSICIAN: {
                rowsDeleted = db.delete(PhysicianEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case REMINDER: {
                rowsDeleted = db.delete(ReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PAIN_LOG: {
                rowsDeleted = db.delete(PainLogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MED_LOG: {
                rowsDeleted = db.delete(MedLogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case STATUS_LOG: {
                rowsDeleted = db.delete(StatusLogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: + uri");

        }
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PATIENT: {
                rowsUpdated = db.update(PatientEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case CREDENTIAL:
            case CREDENTIAL_ID: {
                rowsUpdated = db.update(CredentialEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case PREF: {
                rowsUpdated = db.update(PrefsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case REMINDER:
            case REMINDER_ID: {
                rowsUpdated = db.update(ReminderEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: + uri");
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int returnCount = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PRESCRIPTION: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PrescriptionEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case PHYSICIAN: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PhysicianEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                break;
            }
            case REMINDER: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ReminderEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                break;
            }
            case PAIN_LOG: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PainLogEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                break;
            }
            case MED_LOG: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MedLogEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                break;
            }
            case STATUS_LOG: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StatusLogEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                break;
            }
            default:
                return super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    /**
     *  set up the valid Uri's for this content provider
     *
     * @return UriMatcher configured appropriately
     */
    private static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PatientCPContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PatientCPContract.PATIENT_PATH, PATIENT);
        matcher.addURI(authority, PatientCPContract.CREDENTIAL_PATH, CREDENTIAL);
        matcher.addURI(authority, PatientCPContract.CREDENTIAL_PATH + "/#", CREDENTIAL_ID);
        matcher.addURI(authority, PatientCPContract.PREFS_PATH, PREF);
        matcher.addURI(authority, PatientCPContract.PRESCRIPTION_PATH, PRESCRIPTION);
        matcher.addURI(authority, PatientCPContract.PRESCRIPTION_PATH + "/#", PRESCRIPTION_ID);
        matcher.addURI(authority, PatientCPContract.PHYSICIAN_PATH, PHYSICIAN);
        matcher.addURI(authority, PatientCPContract.PHYSICIAN_PATH + "/#", PHYSICIAN_ID);
        matcher.addURI(authority, PatientCPContract.REMINDER_PATH, REMINDER);
        matcher.addURI(authority, PatientCPContract.REMINDER_PATH + "/#", REMINDER_ID);
        matcher.addURI(authority, PatientCPContract.PAIN_LOG_PATH, PAIN_LOG);
        matcher.addURI(authority, PatientCPContract.PAIN_LOG_PATH + "/#", PAIN_LOG_ID);
        matcher.addURI(authority, PatientCPContract.MED_LOG_PATH, MED_LOG);
        matcher.addURI(authority, PatientCPContract.MED_LOG_PATH + "/#", MED_LOG_ID);
        matcher.addURI(authority, PatientCPContract.STATUS_LOG_PATH, STATUS_LOG);
        matcher.addURI(authority, PatientCPContract.STATUS_LOG_PATH + "/#", STATUS_LOG_ID);
        return matcher;
    }
}
