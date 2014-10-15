package com.skywomantech.app.symptommanagement;

import android.content.ContentValues;


import static com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PhysicianEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrefsEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import static com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;


public class TestData {


    static final long id = 1111L;
    static final int TRUE=1;
    static final int FALSE=0;

    static ContentValues createTestPatient(String name, int active, long lastLogin ){
        ContentValues values = new ContentValues();
        values.put(PatientEntry.COLUMN_NAME, name);
        values.put(PatientEntry.COLUMN_ACTIVE, active);
        values.put(PatientEntry.COLUMN_LAST_LOGIN, lastLogin);
        values.put(PatientEntry.COLUMN_PATIENT_ID, id);
        values.put(PatientEntry.COLUMN_PROCESS_STATUS, 1);
        values.put(PatientEntry.COLUMN_PROCESSED, System.currentTimeMillis());
        return values;
    }

    static ContentValues createTestPrescription(long medId, String name){
        ContentValues values = new ContentValues();
        values.put(PrescriptionEntry.COLUMN_PATIENT_ID, id);
        values.put(PrescriptionEntry.COLUMN_MEDICATION_ID, medId);
        values.put(PrescriptionEntry.COLUMN_NAME, name);
        return values;
    }

    static ContentValues createTestPrefs(){
        ContentValues values = new ContentValues();
        values.put(PrefsEntry.COLUMN_NOTIFICATION, 1);
        values.put(PrefsEntry.COLUMN_PATIENT_ID, id);
        values.put(PrefsEntry.COLUMN_CREATED, System.currentTimeMillis());
        return values;
    }
    static ContentValues createTestPrefs(String timezoneString){
        ContentValues values = createTestPrefs();
        values.put(PrefsEntry.COLUMN_TIMEZONE, timezoneString);
        return values;
    }

    static ContentValues createTestPhysician(long drId, String name ){
        ContentValues values = new ContentValues();
        values.put(PhysicianEntry.COLUMN_PATIENT_ID, id);
        values.put(PhysicianEntry.COLUMN_NAME, name);
        values.put(PhysicianEntry.COLUMN_PHYSICIAN_ID, drId);
        return values;
    }

    static ContentValues createTestReminder( long reminderId, int isOn, String alarm){
        ContentValues values = new ContentValues();
        values.put(ReminderEntry.COLUMN_PATIENT_ID, id);
        values.put(ReminderEntry.COLUMN_REMINDER_ID, reminderId);
        values.put(ReminderEntry.COLUMN_CREATED, System.currentTimeMillis());
        values.put(ReminderEntry.COLUMN_ON, isOn);
        values.put(ReminderEntry.COLUMN_ALARM, alarm);
        return values;
    }

    static ContentValues createTestReminder( long reminderId ){
        return createTestReminder(reminderId, TRUE, "test.alarm" );
    }

    static ContentValues createTestPainLog( int severity, int eating){
        ContentValues values = new ContentValues();
        values.put(PainLogEntry.COLUMN_PATIENT_ID, id);
        values.put(PainLogEntry.COLUMN_SEVERITY, severity);
        values.put(PainLogEntry.COLUMN_EATING, eating);
        values.put(PainLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return values;
    }

    static ContentValues createTestMedLog( String med ){
        ContentValues values = new ContentValues();
        values.put(MedLogEntry.COLUMN_PATIENT_ID, id);
        values.put(MedLogEntry.COLUMN_MED, med);
        values.put(MedLogEntry.COLUMN_TAKEN, System.currentTimeMillis());
        values.put(MedLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return values;
    }

    static ContentValues createTestStatusLog(String note ){
        ContentValues values = new ContentValues();
        values.put(StatusLogEntry.COLUMN_PATIENT_ID, id);
        values.put(StatusLogEntry.COLUMN_NOTE, note);
        values.put(StatusLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return values;
    }


}