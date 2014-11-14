package com.skywomantech.app.symptommanagement.data.graphics;

public class EatingPlotPoint extends TimePoint {
    int eatingValue;

    public EatingPlotPoint() {
    }

    public EatingPlotPoint(long timeValue, int eatingValue) {
        super(timeValue);
        this.eatingValue = eatingValue;
    }

    public int getEatingValue() {
        return eatingValue;
    }
    public void setEatingValue(int eatingValue) {
        this.eatingValue = eatingValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EatingPlotPoint)) return false;
        if (!super.equals(o)) return false;

        EatingPlotPoint that = (EatingPlotPoint) o;

        if (eatingValue != that.eatingValue) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eatingValue;
        return result;
    }

    @Override
    public String toString() {
        return "EatingPlotPoint{" +
                "eatingValue=" + eatingValue +
                "} " + super.toString();
    }
}
