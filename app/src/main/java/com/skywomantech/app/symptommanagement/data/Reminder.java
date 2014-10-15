package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;

public class Reminder {
	
	BigInteger Id;
	private int dayOfWeek;
	private int hour;
	private int minutes;
	private String alarm;
	private boolean isOn;
	
	public enum ReminderType { PAIN, MED, GENERIC };
	private ReminderType reminderType; 

	
	public Reminder() {
		super();
	}
	
	public Reminder(int dayOfWeek, int hour, int minutes, String alarm) {
		super();
		this.dayOfWeek = dayOfWeek;
		this.hour = hour;
		this.minutes = minutes;
		this.alarm = alarm;
	}
	
	public BigInteger getId() {
		return Id;
	}
	public void setId(BigInteger id) {
		Id = id;
	}
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	public String getAlarm() {
		return alarm;
	}
	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}

	public boolean isOn() {
		return isOn;
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}

	public ReminderType getReminderType() {
		return reminderType;
	}

	public void setReminderType(ReminderType reminderType) {
		this.reminderType = reminderType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		result = prime * result + ((alarm == null) ? 0 : alarm.hashCode());
		result = prime * result + dayOfWeek;
		result = prime * result + hour;
		result = prime * result + (isOn ? 1231 : 1237);
		result = prime * result + minutes;
		result = prime * result
				+ ((reminderType == null) ? 0 : reminderType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Reminder))
			return false;
		Reminder other = (Reminder) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		if (alarm == null) {
			if (other.alarm != null)
				return false;
		} else if (!alarm.equals(other.alarm))
			return false;
		if (dayOfWeek != other.dayOfWeek)
			return false;
		if (hour != other.hour)
			return false;
		if (isOn != other.isOn)
			return false;
		if (minutes != other.minutes)
			return false;
		if (reminderType != other.reminderType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Reminder [Id=" + Id + ", dayOfWeek=" + dayOfWeek + ", hour="
				+ hour + ", minutes=" + minutes + ", alarm=" + alarm
				+ ", isOn=" + isOn + ", reminderType=" + reminderType + "]";
	}


	
}
