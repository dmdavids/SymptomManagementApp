package com.skywomantech.app.symptommanagement.physician;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.androidplot.Plot;
import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlotZoomPan;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
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
import java.util.Arrays;
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
        NO_CHART(0), LINE_PLOT(100), FUZZY_CHART(200), SCATTER_CHART(300),
        PIE_CHART(400), BAR_CHART(500);

        private final int value;

        PatientGraph(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PatientGraph findByValue(int val) {
            for (PatientGraph s : values()) {
                if (s.getValue() == val) {
                    return s;
                }
            }
            return NO_CHART;
        }
    }

    private Patient mPatient;
    private String mPatientId = null;
    private XYPlotZoomPan simplePatientXYPlot;
    private SimpleXYSeries eatingSeries = null;  // by time
    private SimpleXYSeries eatingSeriesByHour = null;
    private SimpleXYSeries eatingSeriesByDay = null;
    int notEatingCount = 0;
    int eatingSomeCount = 0;
    int eatingOkCount = 0;
    private SimpleXYSeries severitySeries = null; // by time
    private SimpleXYSeries severitySeriesByHour = null;
    private SimpleXYSeries severitySeriesByDay = null;
    int severeCount = 0;
    int moderateCount = 0;
    int controlledCount = 0;
    private SimpleXYSeries medicationSeries = null;

    private SimpleXYSeries testPoints = null;
    private PointF minXY;
    private PointF maxXY;

    private PieChart pie;
    SegmentFormatter sf1;
    SegmentFormatter sf2;
    SegmentFormatter sf3;
    SegmentFormatter sf4;

    private Segment painSevere;
    private Segment painModerate;
    private Segment painControlled;
    private Segment eatingOK;
    private Segment eatingSome;
    private Segment eatingNone;

    private PatientGraph graph = PatientGraph.NO_CHART;

    private List<SeverityPlotPoint> severityPoints = null;
    private List<EatingPlotPoint> eatingPoints = null;
    private List<MedicationPlotPoint> medicationPoints = null;

    LinearLayout xyChartLayout;
    LinearLayout pieChartLayout;


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
        // ButterKnife doesn't want to inject these so we do it manually
        xyChartLayout = (LinearLayout) rootView.findViewById(R.id.xy_chart_layout);
        simplePatientXYPlot = (XYPlotZoomPan) rootView.findViewById(R.id.patientGraphicsPlot);

        pieChartLayout = (LinearLayout) rootView.findViewById(R.id.pie_chart_layout);
        pie = (PieChart) rootView.findViewById(R.id.PatientPieChart);
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
            createBarChart();
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
            createScatterPlot();
        }
    }

    @OnClick(R.id.pie_plot)
    public void onClickPiePlot(View view) {
        Log.d(LOG_TAG, "Pie Plot Clicked");
        if (patientReadyForGraphing()) {
            createPieChart();
        }
    }

    // if the reset button is pressed then go back to initial view of graph
    @OnClick(R.id.resetButton)
    public void onClickResetButton(View view) {
        // The x-axis is our date range! so both eating and severity have exactly
        // find the min and max X date range for this plot and reset them
        if (testPoints != null) {
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
            if (mPatientId == null) mPatientId = mPatient.getId();
            if (!mPatient.getId().contentEquals(mPatientId) ||
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
        resetCounts();
        Collection<PainLog> painLogs = patient.getPainLog();

        if (painLogs != null && painLogs.size() > 0) {
            severityPoints = new ArrayList<SeverityPlotPoint>();
            eatingPoints = new ArrayList<EatingPlotPoint>();
            for (PainLog p : painLogs) {
                Log.d(LOG_TAG, "Adding Pain Log to Graph Data " + p.toString());
                severityPoints.add(new SeverityPlotPoint(p.getCreated(), p.getSeverity().getValue()));
                updateSeverityCounts(p.getSeverity().getValue());
                eatingPoints.add((new EatingPlotPoint(p.getCreated(), p.getEating().getValue())));
                updateEatingCounts(p.getEating().getValue());

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

    private void createBarChart() {

        setLayout(PatientGraph.BAR_CHART);
        simplePatientXYPlot.clear();

        int count = 0;
        if (severityPoints != null) count++;
        if (eatingPoints != null) count++;
        if (count == 0) {
            Log.d(LOG_TAG, "NO DATA to work with on the graphing!");
            simplePatientXYPlot.redraw();
            return;
        }

        simplePatientXYPlot.setTitle("HTTP Server State (15  Sec)");
        simplePatientXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
//        simplePatientXYPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
//        simplePatientXYPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        simplePatientXYPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        simplePatientXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        simplePatientXYPlot.getGraphWidget().setMarginRight(5);

        simplePatientXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        simplePatientXYPlot.getBorderPaint().setStrokeWidth(1);
        simplePatientXYPlot.getBorderPaint().setAntiAlias(false);
        simplePatientXYPlot.getBorderPaint().setColor(Color.WHITE);

        Log.d(LOG_TAG, "Creating data to plot");
        if (severityPoints != null) {
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


//        // y-vals to plot:
//        Number[] series1Numbers = {1, 2, 3, 4, 2, 3, 4, 2, 2, 2, 3, 4, 2, 3, 2, 2};
//
//        // create our series from our array
//        XYSeries series2 = new SimpleXYSeries(
//                Arrays.asList(series1Numbers),
//                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
//                "Thread #1");
//
        severitySeries.setTitle("Severity");

        simplePatientXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        simplePatientXYPlot.getBorderPaint().setStrokeWidth(1);
        simplePatientXYPlot.getBorderPaint().setAntiAlias(false);
        simplePatientXYPlot.getBorderPaint().setColor(Color.WHITE);

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 100, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                Color.rgb(100, 200, 0), null);          // fill color

        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.BLUE, Shader.TileMode.MIRROR));

        StepFormatter stepFormatter  = new StepFormatter(Color.rgb(0, 0,0), Color.BLUE);
        stepFormatter.getLinePaint().setStrokeWidth(1);

        stepFormatter.getLinePaint().setAntiAlias(false);
        stepFormatter.setFillPaint(lineFill);
        simplePatientXYPlot.addSeries(severitySeries, stepFormatter);

        // adjust the domain/range ticks to make more sense; label per tick for range and label per 5 ticks domain:
        simplePatientXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
        simplePatientXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
        simplePatientXYPlot.setTicksPerRangeLabel(1);
        simplePatientXYPlot.setTicksPerDomainLabel(5);

        // customize our domain/range labels
        simplePatientXYPlot.setDomainLabel("Time (Secs)");
        simplePatientXYPlot.setRangeLabel("Server State");

        // get rid of decimal points in our domain labels:
        simplePatientXYPlot.setDomainValueFormat(new DecimalFormat("0"));

        // create a custom formatter to draw our state names as range tick labels:
        simplePatientXYPlot.setRangeValueFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                Number num = (Number) obj;
                switch(num.intValue()) {
                    case 1:
                        toAppendTo.append("Init");
                        break;
                    case 2:
                        toAppendTo.append("Idle");
                        break;
                    case 3:
                        toAppendTo.append("Recv");
                        break;
                    case 4:
                        toAppendTo.append("Send");
                        break;
                    default:
                        toAppendTo.append("Unknown");
                        break;
                }
                return toAppendTo;
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        simplePatientXYPlot.redraw();
    }

    private void createScatterPlot() {

        setLayout(PatientGraph.SCATTER_CHART);
        simplePatientXYPlot.clear();

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
            simplePatientXYPlot.redraw();
            return;
        }

        Log.d(LOG_TAG, "Creating data to plot");
        if (severityPoints != null) {
            severitySeriesByHour = new SimpleXYSeries("Severity By Hour");
            severitySeriesByDay = new SimpleXYSeries("Severity by Day of Week");
            for (SeverityPlotPoint s : severityPoints) {
                severitySeriesByHour.addLast(s.getHour(), s.getSeverityValue());
                severitySeriesByDay.addLast(s.getDay_of_week(), s.getSeverityValue());
            }
        }
        if (eatingPoints != null) {
            eatingSeriesByHour = new SimpleXYSeries("Eating Ability By Hour");
            eatingSeriesByDay = new SimpleXYSeries("Eating Ability By Day of Week");
            for (EatingPlotPoint eat : eatingPoints) {
                eatingSeriesByHour.addLast(eat.getHour(), eat.getEatingValue());
                eatingSeriesByDay.addLast(eat.getDay_of_week(), eat.getEatingValue());
            }
        }
        Log.d(LOG_TAG, "setting up the drawing information");
        if (eatingSeriesByHour.size() > 0) {
            LineAndPointFormatter eatingPointFormat = new LineAndPointFormatter(
                    null,                   //line color
                    Color.rgb(240, 0, 0),  // point color
                    null,                  // fill color Color.rgb(0, 100, 0)
                    null);                 // pointLabelFormatter
            simplePatientXYPlot.addSeries(eatingSeriesByHour, eatingPointFormat);
        }

        if (severitySeriesByHour.size() > 0) {
            LineAndPointFormatter severityPointFormat = new LineAndPointFormatter(
                    null,                 // line color
                    Color.rgb(0, 50, 0),  // point color
                    null,                 // fill color Color.rgb(0, 0,150)
                    null);                // pointLabelFormatter
            simplePatientXYPlot.addSeries(severitySeriesByHour, severityPointFormat);
        }
        // setup the format of the Y-axis (domain) to show only month & day
//        simplePatientXYPlot.setDomainValueFormat(new Format() {
//            private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd\nhh:mm a");
//
//            @Override
//            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                long timestamp = ((Number) obj).longValue();
//                Date date = new Date(timestamp);
//                return dateFormat.format(date, toAppendTo, pos);
//            }
//
//            @Override
//            public Object parseObject(String source, ParsePosition pos) {
//                return null;
//            }
//        });

        simplePatientXYPlot.setMarkupEnabled(false);
        Log.d(LOG_TAG, "Redrawing the Graph");
        simplePatientXYPlot.redraw();

        // determine the plots minXY and maxXY
        simplePatientXYPlot.calculateMinMaxVals();
        minXY = new PointF(simplePatientXYPlot.getCalculatedMinX().floatValue(),
                simplePatientXYPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(simplePatientXYPlot.getCalculatedMaxX().floatValue(),
                simplePatientXYPlot.getCalculatedMaxY().floatValue());
    }

    private void createPieChart() {

        setLayout(PatientGraph.PIE_CHART);
        pie.clear();
        pie.setTitle("Percentage Pain Severity/Eating Ability");
        painSevere = new Segment("Severe", severeCount);
        painModerate = new Segment("Moderate", moderateCount);
        painControlled = new Segment("Well-Controlled", controlledCount);

        eatingNone = new Segment("Not Eating", notEatingCount);
        eatingSome = new Segment("Some", eatingSomeCount);
        eatingOK = new Segment("Eating OK", eatingOkCount);

        sf1 = new SegmentFormatter();
        sf1.configure(getActivity(), R.xml.pie_segment_formatter1);
        sf2 = new SegmentFormatter();
        sf2.configure(getActivity(), R.xml.pie_segment_formatter2);

        sf3 = new SegmentFormatter();
        sf3.configure(getActivity(), R.xml.pie_segment_formatter3);

        sf4 = new SegmentFormatter();
        sf4.configure(getActivity(), R.xml.pie_segment_formatter4);
        // default to severity level pie chart
        onPieChartGroup(getView().findViewById(R.id.pie_chart_severity));
        ((RadioGroup) getView().findViewById(R.id.pie_chart_radio_group))
                .check(R.id.pie_chart_severity);

        pie.getBorderPaint().setColor(Color.TRANSPARENT);
        pie.getBackgroundPaint().setColor(Color.TRANSPARENT);
    }

    @OnClick({R.id.pie_chart_severity, R.id.pie_chart_eating,})
    public void onPieChartGroup(View v) {
        switch (v.getId()) {
            case R.id.pie_chart_severity:
                pie.removeSeries(eatingOK);
                pie.removeSeries(eatingSome);
                pie.removeSeries(eatingNone);
                pie.addSeries(painSevere, sf1);
                pie.addSeries(painModerate, sf2);
                pie.addSeries(painControlled, sf3);
                pie.redraw();
                break;
            case R.id.pie_chart_eating:
                pie.removeSeries(painSevere);
                pie.removeSeries(painModerate);
                pie.removeSeries(painControlled);
                pie.addSeries(eatingNone, sf1);
                pie.addSeries(eatingSome, sf2);
                pie.addSeries(eatingOK, sf3);
                pie.redraw();
                break;
        }
    }

    private void setLayout(PatientGraph graph) {
        switch (graph) {
            case PIE_CHART:
                xyChartLayout.setVisibility(View.GONE);
                pieChartLayout.setVisibility(View.VISIBLE);
                break;
            default:
                pieChartLayout.setVisibility(View.GONE);
                xyChartLayout.setVisibility(View.VISIBLE);
        }
        simplePatientXYPlot.clear();
        pie.clear();
    }

    private void createLinePlot() {
        setLayout(PatientGraph.LINE_PLOT);
        simplePatientXYPlot.clear();

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
            if (severityPoints != null) {
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
                    : (eatingSeries != null && eatingSeries.size() > 0) ? eatingSeries : null;
            Log.d(LOG_TAG, "setting up the drawing information");
            if (eatingSeries.size() > 0) {
                LineAndPointFormatter eatingLineFormat = new LineAndPointFormatter(
                        Color.rgb(240, 0, 0),  //line color
                        null,                  // point color
                        null,                  // fill color Color.rgb(0, 100, 0)
                        null);                 // pointLabelFormatter
                simplePatientXYPlot.addSeries(eatingSeries, eatingLineFormat);
            }

            if (severitySeries.size() > 0) {
                LineAndPointFormatter severityLineFormat = new LineAndPointFormatter(
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
            return x.getName().compareTo(y.getName());
        }
    }


    private void resetCounts() {
        notEatingCount = 0;
        eatingSomeCount = 0;
        eatingOkCount = 0;
        severeCount = 0;
        moderateCount = 0;
        controlledCount = 0;
    }

    private void updateSeverityCounts(int value) {
        if (PainLog.Severity.SEVERE.getValue() == value) {
            severeCount++;
        } else if (PainLog.Severity.MODERATE.getValue() == value) {
            moderateCount++;
        } else if (PainLog.Severity.WELL_CONTROLLED.getValue() == value) {
            controlledCount++;
        }
    }

    private void updateEatingCounts(int value) {
        if (PainLog.Eating.NOT_EATING.getValue() == value) {
            notEatingCount++;
        } else if (PainLog.Eating.SOME_EATING.getValue() == value) {
            eatingSomeCount++;
        } else if (PainLog.Eating.EATING.getValue() == value) {
            eatingOkCount++;
        }
    }

}
