package com.skywomantech.app.symptommanagement.data;

public class CheckInLog {
    private transient long id; // CP id
    private long checkinId;
    private long created; // use this for an id to connect to pain and medication logs

    public long getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(long checkinId) {
        this.checkinId = checkinId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "CheckInLog [created=" + created + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (created ^ (created >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CheckInLog))
            return false;
        CheckInLog other = (CheckInLog) obj;
        if (created != other.created)
            return false;
        return true;
    }
}
