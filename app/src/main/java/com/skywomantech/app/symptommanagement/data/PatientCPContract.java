package com.skywomantech.app.symptommanagement.data;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class PatientCPContract {

    public static final String CONTENT_AUTHORITY = "com.skywomantech.app.symptommanagement";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public final static String PATIENT_PATH = "patient";
    public final static String PRESCRIPTION_PATH = "prescription";
    public final static String PHYSICIAN_PATH = "physician";
    public final static String PAIN_LOG_PATH = "painlog";
    public final static String MED_LOG_PATH = "medlog";
    public final static String STATUS_LOG_PATH = "statuslog";
    public final static String REMINDER_PATH = "reminder";
    public final static String PREFS_PATH = "pref";
    public final static String CREDENTIAL_PATH = "credential";
    public final static String CHECK_IN_LOG_PATH = "checkinlog";


    public static final class PatientEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATIENT_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATIENT_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATIENT_PATH;

        public static final String TABLE_NAME = "patient";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_LAST_LOGIN = "last_login";
        public static final String COLUMN_BIRTHDATE = "birthdate";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_PROCESSED = "processed";
        public static final String COLUMN_PROCESS_STATUS = "process_status";

        public static Uri buildPatientEntryUriWithPatientId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CredentialEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(CREDENTIAL_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + CREDENTIAL_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + CREDENTIAL_PATH;

        public static final String TABLE_NAME = "credential";
        public static final String COLUMN_USER_ID = "user_id"; // patient or physician id
        public static final String COLUMN_LAST_LOGIN = "last_login";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_USER_TYPE_VALUE = "user_type_value";

        public static Uri buildCredentialEntryUriWithDBId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PrescriptionEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PRESCRIPTION_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PRESCRIPTION_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PRESCRIPTION_PATH;

        public static final String TABLE_NAME = "prescriptions";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_MEDICATION_ID = "medication_id"; // server id
        public static final String COLUMN_NAME = "name";

        public static Uri buildPrescriptionEntryUriWithPrescriptionId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PhysicianEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PHYSICIAN_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PHYSICIAN_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PHYSICIAN_PATH;

        public static final String TABLE_NAME = "physicians";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_PHYSICIAN_ID = "physician_id"; // server id
        public static final String COLUMN_NAME = "name";

        public static Uri buildPhysicianEntryUriWithPhysicianId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CheckInLogEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(CHECK_IN_LOG_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + CHECK_IN_LOG_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + CHECK_IN_LOG_PATH;

        public static final String TABLE_NAME = "checkinlogs";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_CHECKIN_ID = "checkinId"; // connects to pain & med logs
        public static final String COLUMN_CREATED = "created";

        public static Uri buildCheckinLogEntryUriWithLogId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PainLogEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PAIN_LOG_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PAIN_LOG_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PAIN_LOG_PATH;

        public static final String TABLE_NAME = "painlogs";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_PAIN_LOG_ID = "log_id"; // server id
        public static final String COLUMN_SEVERITY = "severity";
        public static final String COLUMN_EATING = "eating";
        public static final String COLUMN_CHECKIN_ID = "checkinId"; // connects to checkin & med logs
        public static final String COLUMN_CREATED = "created";

        public static Uri buildPainLogEntryUriWithLogId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

        public static final class MedLogEntry implements BaseColumns {

            public static final Uri CONTENT_URI =
                    BASE_CONTENT_URI.buildUpon().appendPath(MED_LOG_PATH).build();

            public static final String CONTENT_TYPE =
                    "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + MED_LOG_PATH;
            public static final String CONTENT_ITEM_TYPE =
                    "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + MED_LOG_PATH;

            public static final String TABLE_NAME = "medlogs";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
            public static final String COLUMN_MED_ID = "med_id"; // server id
            public static final String COLUMN_MED_NAME = "med_name";
            public static final String COLUMN_TAKEN = "taken";
            public static final String COLUMN_CHECKIN_ID = "checkinId"; // connects to pain & checkin logs
            public static final String COLUMN_CREATED = "created";

            public static Uri buildMedLogEntryUriWithLogId(long id) {
                return ContentUris.withAppendedId(CONTENT_URI, id);
            }
        }

    public static final class StatusLogEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(STATUS_LOG_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + STATUS_LOG_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + STATUS_LOG_PATH;

        public static final String TABLE_NAME = "statuslogs";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_STATUS_LOG_ID = "log_id"; // server id
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_CREATED = "created";

        public static Uri buildStatusLogEntryUriWithLogId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReminderEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(REMINDER_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + REMINDER_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + REMINDER_PATH;

        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_REMINDER_ID = "reminder_id"; // server id
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_HOUR = "hour";
        public static final String COLUMN_MINUTES = "minutes";
        public static final String COLUMN_ALARM = "alarm";
        public static final String COLUMN_ON = "isOn";
        public static final String COLUMN_CREATED = "created";

        public static Uri buildReminderEntryUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PrefsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PREFS_PATH).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PREFS_PATH;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PREFS_PATH;

        public static final String TABLE_NAME = "prefs";
        public static final String COLUMN_PATIENT_ID = "patient_id"; // server id
        public static final String COLUMN_PREF_ID = "pref_id"; // server id
        public static final String COLUMN_NOTIFICATION = "notification";
        public static final String COLUMN_TIMEZONE = "timezone";
        public static final String COLUMN_CREATED = "created";

        public static Uri buildPrefsEntryUriWithPrefId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}


