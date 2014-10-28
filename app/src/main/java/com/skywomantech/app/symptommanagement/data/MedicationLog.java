package com.skywomantech.app.symptommanagement.data;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MedicationLog  {

	private BigInteger id;
	private long created;
	private Medication med;
	private long taken;

	public MedicationLog() {
		super();
	}

	public MedicationLog(Medication med, long taken) {
		this.med = med;
		this.taken = taken;
		this.created = 0L;
	}

	public MedicationLog(long created, Medication med, long taken) {
		super();
		this.created = created;
		this.med = med;
		this.taken = taken;
	}

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public Medication getMed() {
		return med;
	}

	public void setMed(Medication med) {
		this.med = med;
	}

	public long getTaken() {
		return taken;
	}

	public void setTaken(long taken) {
		this.taken = taken;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((med == null) ? 0 : med.hashCode());
		result = prime * result + (int) (taken ^ (taken >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MedicationLog))
			return false;
		MedicationLog other = (MedicationLog) obj;
		if (created != other.created)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (med == null) {
			if (other.med != null)
				return false;
		} else if (!med.equals(other.med))
			return false;
		if (taken != other.taken)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MedicationLog [id=" + id + ", created=" + created + ", med="
				+ med + ", taken=" + taken + "]";
	}

    public String getTakenDateFormattedString(String dateFormat) {
            Date date = new Date(taken);
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            return format.format(date);
        }
    }

