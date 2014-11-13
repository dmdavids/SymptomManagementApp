package com.skywomantech.app.symptommanagement.data.graph;

public class MedicationPlotPoint extends TimePoint {
    String medId;
    String name;

    public MedicationPlotPoint() {
    }

    public MedicationPlotPoint(long timeValue, String medId, String name) {
        super(timeValue);
        this.medId = medId;
        this.name = name;
    }

    public String getMedId() {
        return medId;
    }
    public void setMedId(String medId) {
        this.medId = medId;
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
        if (!(o instanceof MedicationPlotPoint)) return false;
        if (!super.equals(o)) return false;

        MedicationPlotPoint that = (MedicationPlotPoint) o;

        if (medId != null ? !medId.equals(that.medId) : that.medId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (medId != null ? medId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MedicationPlotPoint{" +
                "medId='" + medId + '\'' +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
