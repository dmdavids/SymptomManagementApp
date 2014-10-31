package com.skywomantech.app.symptommanagement.data;


import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryLog {

    private long id;  // if from CP store the id else ignore?
    private LogType type;
    private String info;
    private long created;

    public enum LogType {
        GENERIC(0), PAIN_LOG(10), MED_LOG(20), STATUS_LOG(30);

        private final int value;

        private LogType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static LogType findByValue(int val){
            for(LogType l : values()){
                if( l.getValue() == val ){
                    return l;
                }
            }
            return GENERIC;
        }
    };

    public HistoryLog() {
    }

    public HistoryLog(LogType type, long id, String info, long created) {
        this.type = type;
        this.id = id;
        this.info = info;
        this.created = created;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryLog that = (HistoryLog) o;

        if (created != that.created) return false;
        if (id != that.id) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (created ^ (created >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HistoryLog{" +
                "id=" + id +
                ", type=" + type +
                ", info='" + info + '\'' +
                ", created=" + created +
                '}';
    }

    public String getFormattedDate(long dt, String fmt) {
        if (dt <= 0L) return "";
        Date date = new Date(dt);
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        return format.format(date);
    }

    // TODO: put the guts in this one
    public String getFormattedCreatedDate() {
        return getFormattedDate(this.created, "E, MMM d yyyy 'at' hh:mm a" );
    }
}
