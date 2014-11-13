package com.skywomantech.app.symptommanagement.physician;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.data.graph.EatingPlotPoint;
import com.skywomantech.app.symptommanagement.data.graph.MedicationPlotPoint;
import com.skywomantech.app.symptommanagement.data.graph.SeverityPlotPoint;
import com.skywomantech.app.symptommanagement.data.graph.TimePoint;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

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
import java.util.Random;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PhysicianPatientDetailFragment extends Fragment
        implements View.OnTouchListener {

    public final static String LOG_TAG = PhysicianPatientDetailFragment.class.getSimpleName();

    public static final String PATIENT_ID_KEY = "patient_id";
    public static final String PHYSICIAN_ID_KEY = "physician_id";

    private String mPatientId;
    private Patient mPatient;

    @InjectView(R.id.physician_patient_detail_name)
    TextView mNameView;

    @InjectView(R.id.physician_patient_detail_birthdate)
    TextView mBDView;

    @InjectView(R.id.patient_medical_id)
    TextView mRecordId;

    private static final int SERIES_SIZE = 200;
    private XYPlot mySimpleXYPlot;
    private SimpleXYSeries[] series = null;  // store the plotting points
    private PointF minXY;
    private PointF maxXY;

    // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;

    PointF firstFinger;
    float distBetweenFingers;
    boolean stopThread = false;

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

    private PatientGraph graph = PatientGraph.FUZZY_CHART;

    private List<SeverityPlotPoint> severityPoints = null;
    private List<EatingPlotPoint> eatingPoints = null;
    private List<MedicationPlotPoint> medicationPoints = null;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicianPatientDetailFragment() {
    }

    public interface Callbacks {
        public void onPatientContacted(String patientId, StatusLog statusLog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_physician_patient_detail, container, false);
        ButterKnife.inject(this, rootView);
        // ButterKnife doesn't want to inject this one
        mySimpleXYPlot = (XYPlot) rootView.findViewById(R.id.mySimpleXYPlot);
        // find the patient and the details then generate plotting data
        mPatient = getPatientFromCloud(this);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.physician_patient_contact_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_status) {
            if (mPatient != null) {
                Log.d(LOG_TAG, "Adding a Physician Status Log");
                addPhysicianStatusLog(mPatientId);
                SymptomManagementSyncAdapter.syncImmediately(getActivity());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPhysicianStatusLog(final String patientId) {
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Patient Contact")
                .setMessage("Did you contact Patient?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addPatientContactStatus(patientId);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                }).create();
        alert.show();
    }

    private void addPatientContactStatus(final String patientId) {
        if (!LoginUtility.isLoggedIn(getActivity())
                || LoginUtility.getUserRole(getActivity()) != UserCredential.UserRole.PHYSICIAN) {
            Log.d(LOG_TAG, "This user isn't a physician why are they here?");
            return;
        }
        String contactNote = "Patient Contacted by Physician";
        StatusLog statusLog = new StatusLog();
        statusLog.setNote(contactNote);
        statusLog.setCreated(System.currentTimeMillis());
        // have the activity save the physician data
        ((Callbacks) getActivity()).onPatientContacted(mPatientId, statusLog);

    }

    private Patient getPatientFromCloud(final PhysicianPatientDetailFragment saveThis) {

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting Patient ID : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    mPatient = result;
                    Log.d(LOG_TAG, "got the Patient!" + mPatient.toDebugString());
                    if (mPatient != null) {
                        // set the views with the patient data
                        mNameView.setText(mPatient.getName());
                        mBDView.setText(mPatient.getBirthdate());
                        mRecordId.setText(mPatient.getId());
                        // use the patient data to generate graphic views
                        createGraph(saveThis, mPatient);
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getActivity(),
                            "Unable to fetch the Patient data. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }

    private void createGraph(PhysicianPatientDetailFragment frag, Patient patient) {

        // make this fragment a listener for events in this plot
        mySimpleXYPlot.setOnTouchListener(frag);
        // set up the graph that isn't already done in the xml
        mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
        mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().setRangeValueFormat(new DecimalFormat("#####"));
        mySimpleXYPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("#####.#"));
        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
        mySimpleXYPlot.setRangeLabel("");
        mySimpleXYPlot.setDomainLabel("");
        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);

        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        mySimpleXYPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        mySimpleXYPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        // This creates four series of plot data
        generatePatientDataLists(patient);

        int count = 0;
        if (severityPoints != null) count++;
        if (eatingPoints != null) count++;
        if (count == 0) {
            Log.d(LOG_TAG, "NO DATA to work with on the graphing!");
        } else {
            Log.d(LOG_TAG, "Creating data to plot");
            series = new SimpleXYSeries[count];
            int counter = 0;
            if ( severityPoints != null ) {
                series[counter] = new SimpleXYSeries("Severity Level");
                Collections.sort(severityPoints, new TimePointSorter());
                for (SeverityPlotPoint s : severityPoints) {
                    series[counter].addLast(s.getTimeValue(), s.getSeverityValue());
                }
                counter++;
            }
            if (eatingPoints != null) {
                series[counter] = new SimpleXYSeries("Eating Ability");
                Collections.sort(eatingPoints, new TimePointSorter());
                for (EatingPlotPoint eat : eatingPoints) {
                    series[counter].addLast(eat.getTimeValue(), eat.getEatingValue());
                }
            }

            Log.d(LOG_TAG, "setting up the drawing information");
                mySimpleXYPlot.addSeries(series[1],
                        new LineAndPointFormatter(Color.rgb(0, 50, 0), null,
                                Color.rgb(0, 100, 0), null));

                mySimpleXYPlot.addSeries(series[0],
                        new LineAndPointFormatter(Color.rgb(0, 0, 0), null,
                                Color.rgb(0, 0, 150), null));

            mySimpleXYPlot.setDomainValueFormat(new Format() {

                // create a simple date format that draws on the year portion of our timestamp.
                // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
                // for a full description of SimpleDateFormat.
                private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                    // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                    // we multiply our timestamp by 1000:
                    long timestamp = ((Number) obj).longValue();
                    Date date = new Date(timestamp);
                    return dateFormat.format(date, toAppendTo, pos);
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;

                }
            });

                // redraw the graph with the new data
            Log.d(LOG_TAG, "Redrawing the Graph");
                mySimpleXYPlot.redraw();
            }

        // determine min and max values for what?
        mySimpleXYPlot.calculateMinMaxVals();
        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),
                mySimpleXYPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),
                mySimpleXYPlot.getCalculatedMaxY().floatValue());
    }

    // if the reset button is pressed then go back to initial view of graph
    @OnClick(R.id.resetButton)
    public void onClick(View view) {
        // not sure how these work for the reset?  uses only series first and last
        minXY.x = series[0].getX(0).floatValue();
        maxXY.x = series[1].getX(series[1].size() - 1).floatValue();
        mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
        mySimpleXYPlot.redraw();
    }

    // when we have a touch event then need to figure out what kind and how it affects
    // the current graph that is displaying
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();

                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();
                }
                break;
        }
        return true;
    }

    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

        minXY.x = Math.min(minXY.x, series[1].getX(series[1].size() - 3)
                .floatValue());
        maxXY.x = Math.max(maxXY.x, series[0].getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }

    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / mySimpleXYPlot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }

    private void clampToDomainBounds(float domainSpan) {
        float leftBoundary = series[0].getX(0).floatValue();
        float rightBoundary = series[1].getX(series[1].size() - 1).floatValue();
        // enforce left scroll boundary:
        if (minXY.x < leftBoundary) {
            minXY.x = leftBoundary;
            maxXY.x = leftBoundary + domainSpan;
        } else if (maxXY.x > series[1].getX(series[1].size() - 1).floatValue()) {
            maxXY.x = rightBoundary;
            minXY.x = rightBoundary - domainSpan;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    // this can be replaced with real data
    private void populateSeries(SimpleXYSeries series, int max) {
        Random r = new Random();
        for (int i = 0; i < SERIES_SIZE; i++) {
            series.addLast(i, r.nextInt(max));
        }
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
