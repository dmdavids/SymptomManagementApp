package com.skywomantech.app.symptommanagement.patient;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.StatusLogEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PatientStatusLogFragment extends Fragment {

    public final static String LOG_TAG = PatientStatusLogFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "patient_status_Log_fragment";

    private StatusLog mLog;

    public interface Callbacks {
        public boolean onStatusLogComplete();
    }

    @InjectView(R.id.symptom_note)
    EditText note;

    @InjectView(R.id.image_status)
    TextView imageLocation;

    @InjectView(R.id.status_imageButton)
    ImageButton imageButton;

    @InjectView(R.id.camera_picture_view) ImageView imageView;

    Uri imagePath;
    Uri imageFile = null;
    public static final int CAMERA_PIC_REQUEST = 99;
    static boolean showCameraButton = true;

    public PatientStatusLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status_log_entry, container, false);
        ButterKnife.inject(this, rootView);
        this.setRetainInstance(true);  // save the fragment state with rotations
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( mLog == null) {
            mLog = new StatusLog();
        } else {
            imageLocation.setVisibility(View.GONE);
            imageButton.setVisibility(showCameraButton ? View.VISIBLE : View.GONE);
            Log.d(LOG_TAG, "file path being opened in view : " + mLog.getImage_location());
            if (mLog != null && mLog.getImage_location() != null && !mLog.getImage_location().isEmpty()) {
                Picasso.with(getActivity())
                        .load(mLog.getImage_location())
                        .resize(800, 800)
                        .centerInside()
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.GONE);
                showCameraButton = false;
            }

        }
    }

    @OnClick(R.id.status_imageButton)
    public void processImage() {
        if (mLog.getImage_location() == null) {
            addImage();
        }
    }

    public void addImage() {
        // See: http://developer.android.com/reference/android/provider/MediaStore.html
        Intent launchCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = getOutputMediaFileUri();
        if (imageFile != null) {
            launchCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);
            startActivityForResult(launchCameraIntent, CAMERA_PIC_REQUEST);
        }
        else {
            Toast.makeText(getActivity(), "Unable to Store Images on this device.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    Uri imagePathFinal = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult called. requestCode: "
                + requestCode + " resultCode:" + resultCode + "data:" + data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            if (resultCode == PatientMainActivity.RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                if (imagePath == null) {
                    imagePath = getImagePath();
                }
                imagePathFinal = imagePath;
                Log.d(LOG_TAG, "File path being saved is: " + imagePathFinal.toString());
                mLog.setImage_location(imagePathFinal.toString());
                imageLocation.setVisibility(View.GONE);
                imageButton.setVisibility(View.GONE);
                showCameraButton = false;
                Log.d(LOG_TAG, "file path being opened in view : " + mLog.getImage_location());
                Picasso.with(getActivity())
                        .load(mLog.getImage_location())
                        .resize(500, 500)
                        .centerInside()
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else if (resultCode == PatientMainActivity.RESULT_CANCELED) {
                // User cancelled the image capture
                Log.e(LOG_TAG, "Image Capture Was Cancelled by User.");
                mLog.setImage_location(null);
                imageButton.setImageResource(android.R.drawable.ic_menu_camera);
                imageButton.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                showCameraButton = true;
            } else {
                // Image capture failed, advise user
                Log.e(LOG_TAG, "Image Capture Failed.");
                mLog.setImage_location(null);
                imageButton.setImageResource(android.R.drawable.ic_menu_camera);
                showCameraButton = true;
                imageButton.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.status_save_button)
    public void saveStatusLog() {
        mLog.setNote(note.getText().toString()); // save the text from the note edit text
        String mPatientId = LoginUtility.getLoginId(getActivity());
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, mLog);
        Log.d(LOG_TAG, "Saving this status : " + mLog.toString());
        Uri uri = getActivity().getContentResolver().insert(StatusLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Status Log Insert Failed.");
        } else {
            ((Callbacks) getActivity()).onStatusLogComplete();
        }
        SymptomManagementSyncAdapter.syncImmediately(getActivity());
        getActivity().onBackPressed();
    }

    private  Uri getOutputMediaFileUri() {
        File newFile = getOutputMediaFile();
        if (newFile != null ) {
            return Uri.fromFile(newFile);
        }
        else {
            return null;
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!isExternalStorageWritable()) {
            return null;
        }
        // For future implementation: store videos in a separate directory
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                "SymptomManagement");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("SymptomManagement", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
    }

    public Uri getImagePath() {
        return imageFile;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
