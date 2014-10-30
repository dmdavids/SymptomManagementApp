package com.skywomantech.app.symptommanagement.data;

import android.content.ContentValues;

public class PatientCPcvHelper {

    public  static ContentValues createValuesObject(String id, MedicationLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.MedLogEntry.COLUMN_MED_NAME, log.getMed().getName());
        cv.put(PatientCPContract.MedLogEntry.COLUMN_MED_ID, log.getMed().getId());
        cv.put(PatientCPContract.MedLogEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.MedLogEntry.COLUMN_TAKEN, log.getTaken());
        cv.put(PatientCPContract.MedLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }

    public static ContentValues createValuesObject(String id, PainLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PainLogEntry.COLUMN_EATING, log.getEating().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_SEVERITY, log.getSeverity().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.PainLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }

    public static ContentValues createValuesObject(String id, StatusLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_NOTE, log.getNote());
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_IMAGE, log.getImage_location());
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.StatusLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }


    public static ContentValues createValuesObject(String id, Reminder rem) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.ReminderEntry.COLUMN_ON, (rem.isOn() ? 1 : 0));
        cv.put(PatientCPContract.ReminderEntry.COLUMN_HOUR, rem.getHour());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_PATIENT_ID, id);
        cv.put(PatientCPContract.ReminderEntry.COLUMN_MINUTES, rem.getMinutes());
        cv.put(PatientCPContract.ReminderEntry.COLUMN_NAME, rem.getName());
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
