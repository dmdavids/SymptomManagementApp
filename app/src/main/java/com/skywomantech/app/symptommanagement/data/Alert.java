package com.skywomantech.app.symptommanagement.data;


public class Alert {

    String id;
	String physicianId;
	String patientId;
	String patientName;
	long created;
    int severityLevel;
    long physicianContacted = 0L;

    public static final int PAIN_SEVERITY_LEVEL_0 = 0;
    public static final int PAIN_SEVERITY_LEVEL_1 = 10;
    public static final int PAIN_SEVERITY_LEVEL_2 = 30;
    public static final int PAIN_SEVERITY_LEVEL_3 = 90;
    public static final int PAIN_SEVERITY_LEVEL_4 = 100;


    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhysicianId() {
		return physicianId;
	}
	public void setPhysicianId(String physicianId) {
		this.physicianId = physicianId;
	}
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
    public int getSeverityLevel() {
        return severityLevel;
    }
    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }
    public String getFormattedMessage() {
		return patientName + " has severe symptoms.";
	}
    public long getPhysicianContacted() {
        return physicianContacted;
    }
    public void setPhysicianContacted(long physicianContacted) {
        this.physicianContacted = physicianContacted;
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((patientId == null) ? 0 : patientId.hashCode());
		result = prime * result
				+ ((patientName == null) ? 0 : patientName.hashCode());
		result = prime * result
				+ ((physicianId == null) ? 0 : physicianId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Alert))
			return false;
		Alert other = (Alert) obj;
		if (created != other.created)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (patientId == null) {
			if (other.patientId != null)
				return false;
		} else if (!patientId.equals(other.patientId))
			return false;
		if (patientName == null) {
			if (other.patientName != null)
				return false;
		} else if (!patientName.equals(other.patientName))
			return false;
		if (physicianId == null) {
			if (other.physicianId != null)
				return false;
		} else if (!physicianId.equals(other.physicianId))
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "Alert{" +
                "id='" + id + '\'' +
                ", physicianId='" + physicianId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", patientName='" + patientName + '\'' +
                ", created=" + created +
                ", severityLevel=" + severityLevel +
                ", physicianContacted=" + physicianContacted +
                '}';
    }
}
