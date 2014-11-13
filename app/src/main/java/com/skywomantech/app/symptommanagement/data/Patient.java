package com.skywomantech.app.symptommanagement.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;


public class Patient {

    private String id;
    private transient long dbId;  // local CP database id!!
    private int severityLevel = 0; // boolean indicating if an alert exists for this patient

    private String firstName;
    private String lastName;
    private String birthdate = "";
    private long lastLogin = 0L;
    private Boolean active = true;
    private PatientPrefs prefs;
    private Set<Medication> prescriptions;
    private Set<Physician> physicians;

    private Set<PainLog> painLog;
    private Set<MedicationLog> medLog;
    private Set<StatusLog> statusLog;

    public Patient() {
        super();
    }

    public Patient( String firstName, String lastName ){
        super();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.birthdate = "";
    }

    public Patient(Patient patient) {
        super();
        this.firstName = patient.getFirstName().trim();
        this.lastName = patient.getLastName().trim();
        this.birthdate = "";
        this.id = patient.getId();
    }

    public Patient( String firstName, String lastName, String birthdate, int severityLevel,
                    long lastLogin,
                    Boolean active, Set<Medication> prescriptions,
                    Set<Physician> physicians, Set<PainLog> painLog,
                    Set<MedicationLog> medLog, Set<StatusLog> statusLog, PatientPrefs prefs) {
        super();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.birthdate = birthdate;
        this.severityLevel = 0;
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

    public String getUserName() {
        String name = "";
        if (firstName != null && !firstName.isEmpty()) name += firstName;
        if (!name.isEmpty()) name += ".";
        if (lastName != null  && !lastName.isEmpty()) name+= lastName;
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
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


    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        if (birthdate != null ? !birthdate.equals(patient.birthdate) : patient.birthdate != null)
            return false;
        if (firstName != null ? !firstName.equals(patient.firstName) : patient.firstName != null)
            return false;
        if (lastName != null ? !lastName.equals(patient.lastName) : patient.lastName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (birthdate != null ? birthdate.hashCode() : 0);
        return result;
    }

    public String toDebugString() {
        return "Patient [id=" + id + ", " +  ", dbId=" + dbId +
                "firstName=" + firstName + ", lastName="
                + lastName + ", birthdate=" + birthdate + ", severityLevel=" + severityLevel
                + ", lastLogin=" + lastLogin + ", active=" + active + ", prescriptions="
                + prescriptions + ", physicians=" + physicians + ", painLog="
                + painLog + ", medLog=" + medLog + ", statusLog=" + statusLog
                + ", prefs=" + prefs + "]";
    }

	@Override
	public String toString() {
		return getName();
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
