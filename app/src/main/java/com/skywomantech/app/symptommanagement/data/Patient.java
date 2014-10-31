package com.skywomantech.app.symptommanagement.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;


public class Patient {

    private String id;
    private transient long dbId;  // local database id

    private String firstName;
    private String lastName;
    private long birthdate = 0L;
    private long lastLogin = 0L;
    private Boolean active = true;

    private Set<Medication> prescriptions;
    private Set<Physician> physicians;

    private Set<PainLog> painLog;
    private Set<MedicationLog> medLog;
    private Set<StatusLog> statusLog;

    private PatientPrefs prefs;

    public Patient() {
        super();
    }

    public Patient( String firstName, String lastName ){
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = System.currentTimeMillis();
    }

    public Patient(Patient patient) {
        super();
        this.firstName = patient.getFirstName();
        this.lastName = patient.getLastName();
        this.birthdate = patient.getBirthdate();
        this.id = patient.getId();
    }

    public Patient( String firstName, String lastName, long birthdate, long lastLogin,
                    Boolean active, Set<Medication> prescriptions,
                    Set<Physician> physicians, Set<PainLog> painLog,
                    Set<MedicationLog> medLog, Set<StatusLog> statusLog, PatientPrefs prefs) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.lastLogin = lastLogin;
        this.active = active;
        this.prescriptions = prescriptions;
        this.physicians = physicians;
        this.painLog = painLog;
        this.medLog = medLog;
        this.statusLog = statusLog;
        this.prefs = prefs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getName() {
        String name = "";
        if (firstName != null && !firstName.isEmpty()) name += firstName;
        if (!name.isEmpty()) name += " ";
        if (lastName != null  && !lastName.isEmpty()) name+= lastName;
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Medication> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(Set<Medication> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public Set<Physician> getPhysicians() {
        return physicians;
    }

    public void setPhysicians(Set<Physician> physicians) {
        this.physicians = physicians;
    }

    public Set<PainLog> getPainLog() {
        return painLog;
    }

    public void setPainLog(Set<PainLog> painLog) {
        this.painLog = painLog;
    }

    public Set<MedicationLog> getMedLog() {
        return medLog;
    }

    public void setMedLog(Set<MedicationLog> medLog) {
        this.medLog = medLog;
    }

    public Set<StatusLog> getStatusLog() {
        return statusLog;
    }

    public void setStatusLog(Set<StatusLog> statusLog) {
        this.statusLog = statusLog;
    }

    public PatientPrefs getPrefs() {
        return prefs;
    }

    public void setPrefs(PatientPrefs prefs) {
        this.prefs = prefs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (birthdate ^ (birthdate >>> 32));
        result = prime * result
                + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((lastName == null) ? 0 : lastName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Patient))
            return false;
        Patient other = (Patient) obj;
        if (birthdate != other.birthdate)
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        return true;
    }

    public String toDebugString() {
        return "Patient [id=" + id + ", " +  ", dbId=" + dbId +
                "firstName=" + firstName + ", lastName="
                + lastName + ", birthdate=" + birthdate + ", lastLogin="
                + lastLogin + ", active=" + active + ", prescriptions="
                + prescriptions + ", physicians=" + physicians + ", painLog="
                + painLog + ", medLog=" + medLog + ", statusLog=" + statusLog
                + ", prefs=" + prefs + "]";
    }

	@Override
	public String toString() {
		return getName();
	}

    public String getFormattedBirthdate() {
        return getFormattedDate(this.birthdate, "MM/dd/yyyy");
    }

    // returns 0 if the string is invalid
    public static long formatBirthdate(String s) {
        if (s == null || s.isEmpty()) return -1L;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dt = null;
        try {
            dt = dateFormat.parse(s);
        } catch (ParseException e) {
            return -1L;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.getTimeInMillis();
    }

    public String getFormattedDate(long dt, String fmt) {
        if (dt <= 0L) return "";
        Date date = new Date(dt);
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        return format.format(date);
    }

    // TODO: put the guts in this one
    public String getFormattedLastLogged() {
        return getFormattedDate(this.lastLogin,"E, MMM d yyyy 'at' hh:mm a" );
    }


}
