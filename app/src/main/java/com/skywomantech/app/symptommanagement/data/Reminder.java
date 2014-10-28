package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;

public class Reminder {
	
	private BigInteger Id;
    private String name;


    private int dayOfWeek;
	private int hour;
	private int minutes;
	private String alarm;
	private boolean isOn;

    public enum ReminderType {
        PAIN(1), MED(2), GENERIC(3);

        private final int value;

        private ReminderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };

    private ReminderType reminderType;
	
	public Reminder() {
		super();
        this.hour = -1;
        this.minutes = -1;
        this.isOn = false;
        this.reminderType = ReminderType.GENERIC;
	}

    public Reminder(String name) {
        super();
        this.name = name;
        this.hour = -1;
        this.minutes = -1;
        this.setOn(false);
        this.reminderType = ReminderType.GENERIC;
    }
	
	public Reminder(String name, int dayOfWeek, int hour, int minutes, String alarm) {
		super();
		this.dayOfWeek = dayOfWeek;
		this.hour = hour;
		this.minutes = minutes;
		this.alarm = alarm;
        this.reminderType = ReminderType.GENERIC;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reminder reminder = (Reminder) o;

        if (dayOfWeek != reminder.dayOfWeek) return false;
        if (hour != reminder.hour) return false;
        if (isOn != reminder.isOn) return false;
        if (minutes != reminder.minutes) return false;
        if (Id != null ? !Id.equals(reminder.Id) : reminder.Id != null) return false;
        if (alarm != null ? !alarm.equals(reminder.alarm) : reminder.alarm != null) return false;
        if (name != null ? !name.equals(reminder.name) : reminder.name != null) return false;
        if (reminderType != reminder.reminderType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Id != null ? Id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + dayOfWeek;
        result = 31 * result + hour;
        result = 31 * result + minutes;
        result = 31 * result + (alarm != null ? alarm.hashCode() : 0);
        result = 31 * result + (isOn ? 1 : 0);
        result = 31 * result + (reminderType != null ? reminderType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "Id=" + Id +
                ", name='" + name + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", hour=" + hour +
                ", minutes=" + minutes +
                ", alarm='" + alarm + '\'' +
                ", isOn=" + isOn +
                ", reminderType=" + reminderType +
                '}';
    }


}
