package com.skywomantech.app.symptommanagement.data.graph;

public class SeverityPlotPoint extends TimePoint {
    int severityValue;

    public SeverityPlotPoint() {
        super();
    }

    public SeverityPlotPoint(long timeValue, int severityValue) {
        super(timeValue);
        this.severityValue = severityValue;
    }

    public int getSeverityValue() {
        return severityValue;
    }
    public void setSeverityValue(int severityValue) {
        this.severityValue = severityValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeverityPlotPoint)) return false;
        if (!super.equals(o)) return false;

        SeverityPlotPoint that = (SeverityPlotPoint) o;

        if (severityValue != that.severityValue) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + severityValue;
        return result;
    }

    @Override
    public String toString() {
        return "SeverityPlotPoint{" +
                "severityValue=" + severityValue +
                "} " + super.toString();
    }
}
