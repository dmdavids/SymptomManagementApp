package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;
import java.util.Collection;
import java.util.TimeZone;

public class PatientPrefs {
	
	BigInteger id;
	private TimeZone timezone;
	private Collection<Reminder> alerts;
	private boolean isNotificationOn;
	
	public PatientPrefs() {
		super();
	}
	public PatientPrefs(TimeZone timezone, Collection<Reminder> alerts,
			boolean isNotificationOn) {
		super();
		this.timezone = timezone;
		this.alerts = alerts;
		this.isNotificationOn = isNotificationOn;
	}
	public TimeZone getTimezone() {
		return timezone;
	}
	public void setTimezone(TimeZone timezone) {
		this.timezone = timezone;
	}
	public Collection<Reminder> getAlerts() {
		return alerts;
	}
	public void setAlerts(Collection<Reminder> alerts) {
		this.alerts = alerts;
	}
	public boolean isNotificationOn() {
		return isNotificationOn;
	}
	public void setNotificationOn(boolean isNotificationOn) {
		this.isNotificationOn = isNotificationOn;
	}
	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PatientPrefs))
			return false;
		PatientPrefs other = (PatientPrefs) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "PatientPrefs [id=" + id + ", timezone=" + timezone
				+ ", alerts=" + alerts + ", isNotificationOn="
				+ isNotificationOn + "]";
	}
	
}
