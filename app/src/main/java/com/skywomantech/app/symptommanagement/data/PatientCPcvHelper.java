package com.skywomantech.app.symptommanagement.data;

import android.content.ContentValues;

public class PatientCPcvHelper {


    public  static ContentValues createValuesObject(String id, Patient patient) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PatientEntry._ID, patient.getDbId()); // update needs this
        cv.put(PatientCPContract.PatientEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.PatientEntry.COLUMN_LAST_LOGIN, patient.getLastLogin());
        cv.put(PatientCPContract.PatientEntry.COLUMN_LAST_NAME, patient.getLastName());
        cv.put(PatientCPContract.PatientEntry.COLUMN_FIRST_NAME, patient.getFirstName());
        cv.put(PatientCPContract.PatientEntry.COLUMN_BIRTHDATE, patient.getBirthdate());
        return cv;
    }

    // used for updating
    public  static ContentValues createInsertValuesObject(String id, Patient patient) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PatientEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.PatientEntry.COLUMN_LAST_LOGIN, patient.getLastLogin());
        cv.put(PatientCPContract.PatientEntry.COLUMN_LAST_NAME, patient.getLastName());
        cv.put(PatientCPContract.PatientEntry.COLUMN_FIRST_NAME, patient.getFirstName());
        cv.put(PatientCPContract.PatientEntry.COLUMN_BIRTHDATE, patient.getBirthdate());
        return cv;
    }

    public  static ContentValues createValuesObject(String id, MedicationLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.MedLogEntry.COLUMN_MED_NAME, log.getMed().getName());
        cv.put(PatientCPContract.MedLogEntry.COLUMN_MED_ID, log.getMed().getId());
        cv.put(PatientCPContract.MedLogEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.MedLogEntry.COLUMN_TAKEN, log.getTaken());
        long thisTime = log.getCreated();
        if (thisTime <= 0L) thisTime = System.currentTimeMillis();
        cv.put(PatientCPContract.MedLogEntry.COLUMN_CREATED, thisTime);
        return cv;
    }

    public static ContentValues createValuesObject(String id, PainLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PainLogEntry.COLUMN_EATING, log.getEating().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_SEVERITY, log.getSeverity().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_PATIENT_ID, id);
        long thisTime = log.getCreated();
        if (thisTime <= 0L) thisTime = System.currentTimeMillis();
        cv.put(PatientCPContract.PainLogEntry.COLUMN_CREATED, thisTime);
        return cv;
    }

    public static ContentValues createValuesObject(String id, StatusLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_NOTE, log.getNote());
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_IMAGE, log.getImage_location());
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_PATIENT_ID, id);
        long thisTime = log.getCreated();
        if (thisTime <= 0L) thisTime = System.currentTimeMillis();
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_CREATED, thisTime);
        return cv;
    }

    // used for updating
    public static ContentValues createValuesObject(String id, Reminder rem) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.ReminderEntry._ID, rem.getDbId());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_ON, (rem.isOn() ? 1 : 0));
        cv.put(PatientCPContract.ReminderEntry.COLUMN_HOUR, rem.getHour());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.ReminderEntry.COLUMN_MINUTES, rem.getMinutes());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_NAME, rem.getName());
        long thisTime = rem.getCreated();
        if (thisTime <= 0L) thisTime = System.currentTimeMillis();
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_CREATED, thisTime);
        return cv;
    }

    public static ContentValues createInsertValuesObject(String id, Reminder rem) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.ReminderEntry.COLUMN_ON, (rem.isOn() ? 1 : 0));
        cv.put(PatientCPContract.ReminderEntry.COLUMN_HOUR, rem.getHour());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.ReminderEntry.COLUMN_MINUTES, rem.getMinutes());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_NAME, rem.getName());
        long thisTime = rem.getCreated();
        if (thisTime <= 0L) thisTime = System.currentTimeMillis();
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_CREATED, thisTime);
        return cv;
    }

    public static ContentValues createValuesObject(String id, Medication med) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PrescriptionEntry.COLUMN_NAME, med.getName());
        cv.put(PatientCPContract.PrescriptionEntry.COLUMN_MEDICATION_ID, med.getId());
        cv.put(PatientCPContract.PrescriptionEntry.COLUMN_PATIENT_ID, id);
        return cv;
    }
}
