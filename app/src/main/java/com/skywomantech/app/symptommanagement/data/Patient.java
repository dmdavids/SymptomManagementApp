package com.skywomantech.app.symptommanagement.data;

import java.util.Set;


public class Patient {


	private String id;
	private String name;
	private long lastLogin;
	private Boolean isActive;
	
	private Set<Medication> prescriptions;
	private Set<Physician> physicians;
	
	private Set<PainLog> painLog;
	private Set<MedicationLog> medLog;
	private Set<StatusLog> statusLog;
	
	private PatientPrefs prefs;
	
	public Patient() {
		super();
	}
	
	public Patient( String name ){
		super();
		this.name = name;
		this.isActive = true;
		this.lastLogin = 0L;
	}

    public Patient( Patient patient ){
        super();
        this.id = patient.getId();
        this.name = patient.getName();
        this.lastLogin = patient.getLastLogin();
        this.isActive = patient.getIsActive();
    }
	
	public Patient( String name, long lastLogin,
			Boolean isActive, Set<Medication> prescriptions,
			Set<Physician> physicians, Set<PainLog> painLog,
			Set<MedicationLog> medLog, Set<StatusLog> statusLog, PatientPrefs prefs) {
		super();
		this.name = name;
		this.lastLogin = lastLogin;
		this.isActive = isActive;
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


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public long getLastLogin() {
		return lastLogin;
	}


	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}


	public Boolean getIsActive() {
		return isActive;
	}


	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((isActive == null) ? 0 : isActive.hashCode());
		result = prime * result + (int) (lastLogin ^ (lastLogin >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isActive == null) {
			if (other.isActive != null)
				return false;
		} else if (!isActive.equals(other.isActive))
			return false;
		if (lastLogin != other.lastLogin)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

    public String toDebugString() {
        return "Patient [id=" + id + ", name=" + name + ", lastLogin="
                + lastLogin + ", isActive=" + isActive + ", prescriptions="
                + prescriptions + ", physicians=" + physicians + ", painLog="
                + painLog + ", medLog=" + medLog + ", statusLog=" + statusLog
                + ", prefs=" + prefs + "]";
    }
	
}
