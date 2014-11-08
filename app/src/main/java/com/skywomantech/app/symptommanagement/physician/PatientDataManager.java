package com.skywomantech.app.symptommanagement.physician;


import android.content.Context;
import android.database.Cursor;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class PatientDataManager {

    private final static String LOG_TAG = PatientDataManager.class.getSimpleName();

    public static synchronized HistoryLog[] createLogList(Patient mPatient) {
        HistoryLogSorter sorter = new HistoryLogSorter();
        TreeSet<HistoryLog> sortedLogs = new TreeSet<HistoryLog>(
                Collections.reverseOrder(sorter));
        Collection<PainLog> plogs = mPatient.getPainLog();
        if (plogs != null) {
            for (PainLog p : plogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(p.getCreated());
                h.setType(HistoryLog.LogType.PAIN_LOG);
                String severity = (p.getSeverity() == PainLog.Severity.SEVERE) ? "SEVERE"
                        : (p.getSeverity() == PainLog.Severity.MODERATE) ? "Moderate"
                        : "Well-Defined";
                String eating = (p.getEating() == PainLog.Eating.NOT_EATING) ? "NOT EATING"
                        : (p.getEating() == PainLog.Eating.SOME_EATING) ? "Some Eating" : "Eating";
                String info = "Pain : " + severity + " -- " + eating;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<MedicationLog> mlogs = mPatient.getMedLog();
        if (mlogs != null) {
            for (MedicationLog m : mlogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(m.getCreated());
                h.setType(HistoryLog.LogType.MED_LOG);
                String name = m.getMed().getName();
                String taken = m.getTakenDateFormattedString(" hh:mm a 'on' E, MMM d yyyy");
                String info = name + " taken " + taken;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<StatusLog> slogs = mPatient.getStatusLog();
        if (slogs != null) {
            for (StatusLog s : slogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(s.getCreated());
                h.setType(HistoryLog.LogType.STATUS_LOG);
                String image = (s.getImage_location() != null && !s.getImage_location().isEmpty())
                        ? "Image Taken" : "";
                String info = "Note: " + s.getNote() + " " + image;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }

        if (sortedLogs.size() <= 0) return new HistoryLog[0];
        return sortedLogs.toArray(new HistoryLog[sortedLogs.size()]);
    }

    public static class HistoryLogSorter implements Comparator<HistoryLog> {
        public int compare(HistoryLog x, HistoryLog y) {
            return Long.compare(x.getCreated(), y.getCreated());
        }
    }

    public static void getLogsFromCP(Context context, Patient patientRecord) {
        patientRecord.setPainLog(getUpdatedPainLogs(context, patientRecord.getId()));
        patientRecord.setMedLog(getUpdatedMedLogs(context, patientRecord.getId()));
        patientRecord.setStatusLog(getUpdatedStatusLogs(context, patientRecord.getId()));
    }

    private static Set<PainLog> getUpdatedPainLogs(Context context, String id) {
        Set<PainLog> logs = new HashSet<PainLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.PainLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            PainLog log = new PainLog();
            log.setEating(PainLog.Eating.findByValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_EATING))));
            log.setSeverity(PainLog.Severity.findByValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_SEVERITY))));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    private static Set<MedicationLog> getUpdatedMedLogs(Context context, String id) {
        Set<MedicationLog> logs = new HashSet<MedicationLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.MedLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            MedicationLog log = new MedicationLog();
            log.setMed(new Medication());
            log.getMed().setId(cursor.getString(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_MED_ID)));
            log.getMed().setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_MED_NAME)));
            log.setTaken(cursor.getLong(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_TAKEN)));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    private static synchronized Set<StatusLog> getUpdatedStatusLogs(Context context, String id) {
        Set<StatusLog> logs = new HashSet<StatusLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.StatusLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            StatusLog log = new StatusLog();
            log.setNote(cursor.getString(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_NOTE)));
            log.setImage_location(cursor.getString(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_IMAGE)));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    public static synchronized Collection<Reminder> loadReminderList(Context context) {
        Collection<Reminder> reminders = new HashSet<Reminder>();
        // search the local storage for the item id
        String mPatientId = LoginUtility.getLoginId(context);
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            Reminder item = new Reminder();
            item.setDbId(cursor.getLong(cursor.getColumnIndex(PatientCPContract.ReminderEntry._ID)));
            item.setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_NAME)));
            item.setHour(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_HOUR)));
            item.setMinutes(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_MINUTES)));
            item.setOn((cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_ON)) == 0 ? false : true));
            reminders.add(item);
        }
        cursor.close();
        return reminders;
    }
}
