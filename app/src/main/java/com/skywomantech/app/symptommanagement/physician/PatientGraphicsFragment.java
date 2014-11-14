package com.skywomantech.app.symptommanagement.physician;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlotZoomPan;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.graphics.EatingPlotPoint;
import com.skywomantech.app.symptommanagement.data.graphics.MedicationPlotPoint;
import com.skywomantech.app.symptommanagement.data.graphics.SeverityPlotPoint;
import com.skywomantech.app.symptommanagement.data.graphics.TimePoint;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PatientGraphicsFragment extends Fragment {

    public final static String LOG_TAG = PatientGraphicsFragment.class.getSimpleName();

    public interface Callbacks {
        public Patient getPatientDataForGraphing();
    }

    public static final String PATIENT_ID_KEY = "patient_id";
    public static final String PHYSICIAN_ID_KEY = "physician_id";

    public enum PatientGraph {
        NO_CHART(0), LINE_PLOT(100), FUZZY_CHART(200), SCATTER_CHART_DAY(300),
        SCATTER_CHART_WEEK(400);

        private final int value;

        PatientGraph(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PatientGraph findByValue(int val){
            for(PatientGraph s : values()){
                if( s.getValue() == val ){
                    return s;
                }
            }
            return NO_CHART;
        }
    }

    private Patient mPatient;
    private String mPatientId = null;
    private XYPlotZoomPan simplePatientXYPlot;
    private SimpleXYSeries eatingSeries = null;
    private SimpleXYSeries severitySeries = null;
    private SimpleXYSeries medicationSeries = null;
    private SimpleXYSeries testPoints = null;
    private PointF minXY;
    private PointF maxXY;

    private PatientGraph graph = PatientGraph.NO_CHART;

    private List<SeverityPlotPoint> severityPoints = null;
    private List<EatingPlotPoint> eatingPoints = null;
    private List<MedicationPlotPoint> medicationPoints = null;

    public PatientGraphicsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
        setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_graphics, container, false);
        ButterKnife.inject(this, rootView);
        // ButterKnife doesn't want to inject this one so we do it manually
        simplePatientXYPlot = (XYPlotZoomPan) rootView.findViewById(R.id.patientGraphicsPlot);
        return rootView;
    }

    @OnClick(R.id.line_plot)
    public void onClickLinePlot(View view) {
        Log.d(LOG_TAG, "Line Plot Clicked");
        if (patientReadyForGraphing()) {
            createLinePlot();
        }
    }


    @OnClick(R.id.bar_plot)
    public void onClickBarPlot(View view) {
        Log.d(LOG_TAG, "Bar Chart Clicked");
        if (patientReadyForGraphing()) {
            createLinePlot();
        }
    }

    @OnClick(R.id.fuzzy_plot)
    public void onClickFuzzyPlot(View view) {
        Log.d(LOG_TAG, "Fuzzy Plot Clicked");
        if (patientReadyForGraphing()) {
            createLinePlot();
        }
    }

    @OnClick(R.id.scatter_plot)
    public void onClickScatterPlot(View view) {
        Log.d(LOG_TAG, "Scatter Plot Clicked");
        if (patientReadyForGraphing()) {
            createLinePlot();
        }
    }

    @OnClick(R.id.pie_plot)
    public void onClickPiePlot(View view) {
        Log.d(LOG_TAG, "Pie Plot Clicked");
        if (patientReadyForGraphing()) {
            createLinePlot();
        }
    }

    // if the reset button is pressed then go back to initial view of graph
    @OnClick(R.id.resetButton)
    public void onClickResetButton(View view) {
        // The x-axis is our date range! so both eating and severity have exactly
        // find the min and max X date range for this plot and reset them
        if( testPoints != null ) {
            minXY.x = testPoints.getX(0).floatValue();
            maxXY.x = testPoints.getX(testPoints.size() - 1).floatValue();
            simplePatientXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
            simplePatientXYPlot.redraw();
        }
    }

    private boolean patientReadyForGraphing() {
        if (mPatient == null || mPatientId == null) {
            mPatient = ((Callbacks) getActivity()).getPatientDataForGraphing();

            if (mPatient == null || mPatient.getId() == null) {
                Log.d(LOG_TAG, "NO Patient Data for Graphing!");
                return false;
            }
            if(mPatientId == null) mPatientId = mPatient.getId();
            if(!mPatient.getId().contentEquals(mPatientId) ||
                    severityPoints == null || eatingPoints == null || medicationPoints == null) {
                Log.d(LOG_TAG, "This is a new patient so we need to recalculate the series.");
                generatePatientDataLists(mPatient);
            }
            Log.d(LOG_TAG, "Current Patient to be Graphed : " + mPatient);
        }
        return true;
    }

    private void generatePatientDataLists(Patient patient) {
        if (patient == null) return;
        Collection<PainLog> painLogs = patient.getPainLog();

        if (painLogs != null && painLogs.size() > 0) {
            severityPoints = new ArrayList<SeverityPlotPoint>();
            eatingPoints = new ArrayList<EatingPlotPoint>();
            for (PainLog p : painLogs) {
                Log.d(LOG_TAG, "Adding Pain Log to Graph Data " + p.toString());
                severityPoints.add(new SeverityPlotPoint(p.getCreated(), p.getSeverity().getValue()));
                eatingPoints.add((new EatingPlotPoint(p.getCreated(), p.getEating().getValue())));
            }
        }
        Collection<MedicationLog> medicationLogs = patient.getMedLog();
        if (medicationLogs != null && medicationLogs.size() > 0) {
            medicationPoints = new ArrayList<MedicationPlotPoint>();
            for (MedicationLog m : medicationLogs) {
                Log.d(LOG_TAG, "Adding Medication Log to Graph Data " + m.toString());
                medicationPoints.add(new MedicationPlotPoint(m.getTaken(),
                        m.getMed().getId(), m.getMed().getName()));
            }
        }
        Log.d(LOG_TAG, "Severity Points : " + severityPoints.toString());
        Log.d(LOG_TAG, "Eating Points : " + eatingPoints.toString());
        Log.d(LOG_TAG, "Med Log Points : " + medicationPoints.toString());
    }

    private void createLinePlot() {

        simplePatientXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
        simplePatientXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
        simplePatientXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        simplePatientXYPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("#####"));
        simplePatientXYPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("#####.#"));
        simplePatientXYPlot.getGraphWidget().setRangeLabelWidth(25);
        simplePatientXYPlot.setRangeLabel("");
        simplePatientXYPlot.setDomainLabel("");
        simplePatientXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        simplePatientXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        simplePatientXYPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        simplePatientXYPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        simplePatientXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.setRangeBoundaries(0, 400, BoundaryMode.FIXED);

        int count = 0;
        if (severityPoints != null) count++;
        if (eatingPoints != null) count++;
        if (count == 0) {
            Log.d(LOG_TAG, "NO DATA to work with on the graphing!");
        } else {
            Log.d(LOG_TAG, "Creating data to plot");
            if ( severityPoints != null ) {
                severitySeries = new SimpleXYSeries("Severity Level");
                Collections.sort(severityPoints, new TimePointSorter());
                for (SeverityPlotPoint s : severityPoints) {
                    severitySeries.addLast(s.getTimeValue(), s.getSeverityValue());
                }
            }
            if (eatingPoints != null) {
                eatingSeries = new SimpleXYSeries("Eating Ability");
                Collections.sort(eatingPoints, new TimePointSorter());
                for (EatingPlotPoint eat : eatingPoints) {
                    eatingSeries.addLast(eat.getTimeValue(), eat.getEatingValue());
                }
            }
            testPoints = (severitySeries != null && severitySeries.size() > 0) ? severitySeries
                       : (eatingSeries != null && eatingSeries.size() > 0) ?  eatingSeries : null;
            Log.d(LOG_TAG, "setting up the drawing information");
            if ( eatingSeries.size() > 0 ) {
                LineAndPointFormatter eatingLineFormat = new LineAndPointFormatter(
                        Color.rgb(240, 0, 0),  //line color
                        null,                  // point color
                        null,                  // fill color Color.rgb(0, 100, 0)
                        null);                 // pointLabelFormatter
                simplePatientXYPlot.addSeries(eatingSeries, eatingLineFormat);
            }

            if (severitySeries.size() > 0) {
                LineAndPointFormatter severityLineFormat =new LineAndPointFormatter(
                        Color.rgb(0, 50, 0),  // line color
                        null,                 // point color
                        null,                 // fill color Color.rgb(0, 0,150)
                        null);                // pointLabelFormatter
                simplePatientXYPlot.addSeries(severitySeries, severityLineFormat);
            }
            // setup the format of the Y-axis (domain) to show only month & day
            simplePatientXYPlot.setDomainValueFormat(new Format() {
                private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd\nhh:mm a");
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    long timestamp = ((Number) obj).longValue();
                    Date date = new Date(timestamp);
                    return dateFormat.format(date, toAppendTo, pos);
                }
                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });

            simplePatientXYPlot.setMarkupEnabled(false);
            Log.d(LOG_TAG, "Redrawing the Graph");
                simplePatientXYPlot.redraw();
            }

        // determine the plots minXY and maxXY
        simplePatientXYPlot.calculateMinMaxVals();
        minXY = new PointF(simplePatientXYPlot.getCalculatedMinX().floatValue(),
                simplePatientXYPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(simplePatientXYPlot.getCalculatedMaxX().floatValue(),
                simplePatientXYPlot.getCalculatedMaxY().floatValue());
    }

    private class TimePointSorter implements Comparator<TimePoint> {

        public int compare(TimePoint x, TimePoint y) {
            return Long.compare(x.getTimeValue(), y.getTimeValue());
        }
    }

    private class SeveritySorter implements Comparator<SeverityPlotPoint> {

        public int compare(SeverityPlotPoint x, SeverityPlotPoint y) {
            return Long.compare(x.getSeverityValue(), y.getSeverityValue());
        }
    }

    private class EatingSorter implements Comparator<EatingPlotPoint> {

        public int compare(EatingPlotPoint x, EatingPlotPoint y) {
            return Long.compare(x.getEatingValue(), y.getEatingValue());
        }
    }

    private class MedicationNameSorter implements Comparator<MedicationPlotPoint> {

        public int compare(MedicationPlotPoint x, MedicationPlotPoint y) {
            return  x.getName().compareTo(y.getName());
        }
    }

}
