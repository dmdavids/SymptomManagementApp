package com.skywomantech.app.symptommanagement;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.skywomantech.app.symptommanagement.patient.StatusLogEntryFragment;

import static com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PhysicianEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrefsEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;

public class TestPatientProvider extends AndroidTestCase {

    public void setUp() {
        deleteAllRecords();
    }

    public void deleteAllRecords() {
        deleteRecords(PatientEntry.CONTENT_URI);
        deleteRecords(PrefsEntry.CONTENT_URI);
        deleteRecords(ReminderEntry.CONTENT_URI);
        deleteRecords(PrescriptionEntry.CONTENT_URI);
        deleteRecords(PhysicianEntry.CONTENT_URI);
        deleteRecords(PainLogEntry.CONTENT_URI);
        deleteRecords(MedLogEntry.CONTENT_URI);
        deleteRecords(StatusLogEntry.CONTENT_URI);
    }

    public void testInsertReadDeleteAllRecords() {
        testInsertReadPatientRecord();
        testInsertReadPrefRecord();
        testInsertReadReminderRecord();
        testInsertReadPrescriptionRecord();
        testInsertReadPhysicianRecord();
        testInsertReadPainLogRecord();
        testInsertReadMedLogRecord();
        testInsertReadStatusRecord();
    }

    public void testUpdateRecords() {
        // the only records that should be updated
        // in this app are the patient, prefs, and reminders
        ContentValues insertObj =
                TestData.createTestPatient("Frank Neal", TestData.TRUE, System.currentTimeMillis());
        ContentValues updateObj =
                TestData.createTestPatient("Frank J Neal", TestData.TRUE, System.currentTimeMillis());
        insertUpdate(mContext, PatientEntry.CONTENT_URI, insertObj, updateObj, PatientEntry._ID,
                PatientEntry.COLUMN_NAME);

        insertObj = TestData.createTestPrefs("NONE");
        updateObj = TestData.createTestPrefs("UTC");
        insertUpdate(mContext, PrefsEntry.CONTENT_URI, insertObj, updateObj, PrefsEntry._ID,
                PrefsEntry.COLUMN_TIMEZONE);

        insertObj = TestData.createTestReminder(987L, TestData.TRUE, "old.alarm");
        updateObj = TestData.createTestReminder(987L, TestData.TRUE, "new.alarm");
        insertUpdate(mContext, ReminderEntry.CONTENT_URI, insertObj, updateObj, ReminderEntry._ID,
                ReminderEntry.COLUMN_ALARM);
    }

    public long insertUpdate(Context context, Uri contentUri,
                             ContentValues insertObj, ContentValues updateObj, String id,
                             String compareColumn) {

        // insert a new record
        long insertId = insertReadRecord(insertObj, contentUri);
        updateObj.put(id, insertId);

        context.getContentResolver().update(contentUri, updateObj, null, null);
        Cursor cursor = mContext.getContentResolver().query(
                contentUri,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertTrue(cursor.moveToFirst());

        // compare a column of data
        int idx = cursor.getColumnIndex(compareColumn);
        assertFalse(idx == -1);
        assertEquals(cursor.getString(idx).toString(), updateObj.get(compareColumn).toString());
        return insertId;
    }

    public void deleteRecords(Uri contentUri) {
        mContext.getContentResolver().delete(
                contentUri,
                null,
                null
        );
        Cursor cursor = mContext.getContentResolver().query(
                contentUri,
                null,
                null,
                null,
                null
        );
        cursor.close();
    }

    public void testInsertReadPatientRecord() {
        ContentValues testObj =
                TestData.createTestPatient("Frank Neal", TestData.TRUE, System.currentTimeMillis());
        insertReadRecord(testObj, PatientEntry.CONTENT_URI);
    }

    public void testInsertReadPrefRecord() {
        ContentValues testObj = TestData.createTestPrefs();
        insertReadRecord(testObj, PrefsEntry.CONTENT_URI);
    }

    public void testInsertReadReminderRecord() {
        ContentValues testObj = TestData.createTestReminder(123L);
        insertReadRecord(testObj, ReminderEntry.CONTENT_URI);
    }

    public void testInsertReadPhysicianRecord() {
        ContentValues testObj = TestData.createTestPhysician(99L, "Dr. Wise");
        insertReadRecord(testObj, PhysicianEntry.CONTENT_URI);
    }

    public void testInsertReadPrescriptionRecord() {
        ContentValues testObj = TestData.createTestPrescription(44L, "Jumping Juice");
        insertReadRecord(testObj, PrescriptionEntry.CONTENT_URI);
    }

    public void testInsertReadPainLogRecord() {
        ContentValues testObj = TestData.createTestPainLog(3, 1);
        insertReadRecord(testObj, PainLogEntry.CONTENT_URI);
    }

    public void testInsertReadMedLogRecord() {
        ContentValues testObj = TestData.createTestMedLog("Water");
        insertReadRecord(testObj, MedLogEntry.CONTENT_URI);
    }

    public void testInsertReadStatusRecord() {
       ContentValues testObj = TestData.createTestStatusLog("This is a silly note to put here.");
        insertReadRecord(testObj, StatusLogEntry.CONTENT_URI);
    }

    public long insertReadRecord(ContentValues testObj, Uri contentUri) {
        Uri uri = mContext.getContentResolver().insert(contentUri, testObj);
        long rowId = ContentUris.parseId(uri);
        assertTrue(rowId != -1);
        Cursor cursor = mContext.getContentResolver().query(
                contentUri,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        assertTrue(cursor.moveToFirst());
        return rowId;
    }

}
