package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;


public class PainLog {

	private BigInteger id;
	private long created;
	private int severity;
	private int eating;

	public PainLog() {
		super();
	}
	
	public PainLog(int severity, int eating) {
		this.severity = severity;
		this.eating = eating;
		this.created = 0L;
	}
	
	
	public PainLog(long created, int severity, int eating) {
		super();
		this.created = created;
		this.severity = severity;
		this.eating = eating;
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

	public int getSeverity() {
		return severity;
	}
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	public int getEating() {
		return eating;
	}
	public void setEating(int eating) {
		this.eating = eating;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + eating;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + severity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PainLog))
			return false;
		PainLog other = (PainLog) obj;
		if (created != other.created)
			return false;
		if (eating != other.eating)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (severity != other.severity)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PainLog [id=" + id + ", created=" + created + ", severity="
				+ severity + ", eating=" + eating + "]";
	}

}
