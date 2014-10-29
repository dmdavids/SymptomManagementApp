/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skywomantech.app.symptommanagement;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.skywomantech.app.symptommanagement.data.PatientDBHelper;


import java.util.Map;
import java.util.Set;

import static com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PhysicianEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrefsEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;

public class TestPatientDB extends AndroidTestCase {

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(PatientDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new PatientDBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testPatientInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testPatient =
                TestData.createTestPatient("patient1", 1, System.currentTimeMillis());

        long rowId = db.insert(PatientEntry.TABLE_NAME, null, testPatient);
        assertTrue(rowId != -1);
        System.out.println("Patient row id: " + rowId);

        Cursor cursor = db.query(
                PatientEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();

        testPatient.put(PatientEntry.COLUMN_LAST_LOGIN, System.currentTimeMillis());
        db.update(PatientEntry.TABLE_NAME, testPatient, null, null);
        cursor = db.query(
                PatientEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);
        int idx = cursor.getColumnIndex(PatientEntry.COLUMN_LAST_LOGIN);
        assertFalse(idx == -1);
        assertTrue(cursor.getLong(idx) == testPatient.getAsLong(PatientEntry.COLUMN_LAST_LOGIN));
        cursor.close();
    }

    public void testPrescriptionInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestPrescription(11L, "Happy Juice");

        long rowId = db.insert(PrescriptionEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Prescription row id: " + rowId);

        Cursor cursor = db.query(
                PrescriptionEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    public void testPrefsInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestPrefs();

        long rowId = db.insert(PrefsEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Prefs row id: " + rowId);

        Cursor cursor = db.query(
                PrefsEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();

        testObject.put(PrefsEntry.COLUMN_CREATED, System.currentTimeMillis());
        db.update(PrefsEntry.TABLE_NAME, testObject, null, null);
        cursor = db.query(
                PrefsEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);
        int idx = cursor.getColumnIndex(PrefsEntry.COLUMN_CREATED);
        assertFalse(idx == -1);
        assertTrue(cursor.getLong(idx) == testObject.getAsLong(PrefsEntry.COLUMN_CREATED));
        cursor.close();
    }

    public void testPhysicianInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestPhysician(12L, "Dr. Giggles");

        long rowId = db.insert(PhysicianEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Physician row id: " + rowId);

        Cursor cursor = db.query(
                PhysicianEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    public void testPainLogInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestPainLog(2, 3);

        long rowId = db.insert(PainLogEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Pain Log row id: " + rowId);

        Cursor cursor = db.query(
                PainLogEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    public void testMedLogInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestMedLog("Happy Pills");

        long rowId = db.insert(MedLogEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Med Log row id: " + rowId);

        Cursor cursor = db.query(
                MedLogEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    public void testStatusLogInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestStatusLog("This is a test note");

       long rowId = db.insert(StatusLogEntry.TABLE_NAME, null, testObject);
       assertTrue(rowId != -1);
        System.out.println("Status Log row id: " + rowId);

        Cursor cursor = db.query(
                StatusLogEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    public void testReminderInsertRead() {
        PatientDBHelper dbHelper = new PatientDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testObject =
                TestData.createTestReminder(55L);

        long rowId = db.insert(ReminderEntry.TABLE_NAME, null, testObject);
        assertTrue(rowId != -1);
        System.out.println("Reminder row id: " + rowId);

        Cursor cursor = db.query(
                ReminderEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        cursor.close();

        testObject.put(ReminderEntry.COLUMN_CREATED, System.currentTimeMillis());
        db.update(ReminderEntry.TABLE_NAME, testObject, null, null);
        cursor = db.query(
                ReminderEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);
        int idx = cursor.getColumnIndex(ReminderEntry.COLUMN_CREATED);
        assertFalse(idx == -1);
        assertTrue(cursor.getLong(idx) == testObject.getAsLong(ReminderEntry.COLUMN_CREATED));
        cursor.close();
    }

    static void validateItemCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }
}
