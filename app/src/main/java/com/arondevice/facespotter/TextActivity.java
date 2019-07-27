package com.arondevice.facespotter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arondevice.facespotter.ui.camera.CameraSource;
import com.arondevice.facespotter.ui.camera.CameraSourcePreview;
import com.arondevice.facespotter.ui.camera.GraphicOverlay;
import com.arondevice.facespotter.ui.camera.TextRecognitionProcessor;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tourdyiev Roman on 4/13/19.
 */
public class TextActivity extends AppCompatActivity {
    private static final String TAG = "TextActivity";
    private CameraSourcePreview preview; // To handle the camera
    private GraphicOverlay graphicOverlay; // To draw over the camera screen
    private CameraSource cameraSource = null; //To handle the camera

    private static final int PERMISSION_REQUESTS = 1; // to handle the runtime permissions
    //    private List<String> displayList; // to manage the adapter of the results recieved
    private List<String> markers;
    private List<String> masks;

    private TextView resultNumberTv;// to display the number of results


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);


        // getting views from the xml
        resultNumberTv = findViewById(R.id.resultsMessageTv);
        preview = findViewById(R.id.Preview);
        graphicOverlay = findViewById(R.id.Overlay);


        // intializing views
//        displayList = new ArrayList<>();
        markers = new ArrayList<>();
        masks = new ArrayList<>();
        markers.addAll(Arrays.asList(getResources().getStringArray(R.array.markers)));
        masks.addAll(Arrays.asList(getResources().getStringArray(R.array.masks)));

//        resultNumberTv.setText(getString(R.string.x_results_found, displayList.size()));

        if (preview == null) {
            Log.d(TAG, " Preview is null ");
        }

        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null ");
        }
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        Log.d("onPause", "here");
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    // Actual code to start the camera
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "startCameraSource resume: Preview is null ");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "startCameraSource resume: graphOverlay is null ");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.d(TAG, "startCameraSource : Unable to start camera source." + e.getMessage());
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    // Function to check if all permissions given by the user
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    // List of permissions required by the application to run.
    private String[] getRequiredPermissions() {
        return new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    // Checking a Runtime permission value
    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "isPermissionGranted Permission granted : " + permission);
            return true;
        }
        Log.d(TAG, "isPermissionGranted: Permission NOT granted -->" + permission);
        return false;
    }

    // getting runtime permissions
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    // Function to create a camera source and retain it.
    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(this, false));//here we can add activity and populate
        } catch (Exception e) {
            Log.d(TAG, "createCameraSource can not create camera source: " + e.getCause());
            e.printStackTrace();
        }
    }


    //  updating and displaying the results recieved from Firebase Text Processor Api
    public void returnResults(FirebaseVisionText textresults) {
        List<FirebaseVisionText.TextBlock> blocks = textresults.getTextBlocks();
        for (FirebaseVisionText.TextBlock eachBlock : blocks) {
            for (FirebaseVisionText.Line eachLine : eachBlock.getLines()) {
                for (final FirebaseVisionText.Element eachElement : eachLine.getElements()) {

                    Log.d("printResults", eachElement.getText());

                    if (markers.contains(eachElement.getText().toLowerCase())) {
//                            TextActivity.this.finish();
                        int index = markers.indexOf(eachElement.getText().toLowerCase());

                        Log.d("resultScanned", eachElement.getText());
                        Toast.makeText(this, "I've found " + masks.get(index), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(TextActivity.this, FaceActivity.class);
                        intent.putExtra("mask", masks.get(markers.indexOf(eachElement.getText().toLowerCase())));
                        startActivity(intent);
                    }
                }
            }
        }
//        resultNumberTv.setText(getString(R.string.x_results_found, displayList.size()));
    }
}
