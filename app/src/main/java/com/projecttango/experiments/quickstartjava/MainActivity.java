/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.experiments.quickstartjava;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.quickstartjava.R;

import java.util.ArrayList;


/**
 * Main Activity for the Tango Java Quickstart. Demonstrates establishing a
 * connection to the {@link Tango} service and printing the {@link}
 * data to the LogCat. Also demonstrates Tango lifecycle management through
 * {@link TangoConfig}.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String sTranslationFormat = "Translation: %f, %f, %f";
    private static final String sRotationFormat = "Rotation: %f, %f, %f, %f";

    private TextView mTranslationTextView;
    private TextView mRotationTextView;
    private TextView mLength;
    private TextView mArea;
    private TextView mVolume;
    private TextView mRecorded;
    private double area;
    private double volume;
    private String PointsChosen="";
    private double a1=0;
    private double a2=0;
    private double a3=0;
    private double sums;

    private Tango mTango;
    private TangoConfig mConfig;
    private TangoPoseData mTangoPoseData;
    private boolean mIsTangoServiceConnected;
    private boolean mIsProcessing = false;

    ArrayList<vector> vectors = new ArrayList<vector>();
    ArrayList<Double> magnitudes = new ArrayList<Double>();
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //Setting textviews
        mTranslationTextView = (TextView) findViewById(R.id.translation_text_view);
        mRotationTextView = (TextView) findViewById(R.id.rotation_text_view);
        mRecorded = (TextView) findViewById(R.id.recorded_points);
        mLength = (TextView) findViewById(R.id.total_length);
        mArea = (TextView) findViewById(R.id.total_area);
        mVolume = (TextView) findViewById(R.id.total_volume);
        //buttons!
        findViewById(R.id.Finish_Record).setOnClickListener(this);
        findViewById(R.id.total_length).setOnClickListener(this);
        findViewById(R.id.total_area).setOnClickListener(this);
        findViewById(R.id.total_volume).setOnClickListener(this);
        findViewById(R.id.Record).setOnClickListener(this);

        // Instantiate Tango client
        mTango = new Tango(this);

        // Set up Tango configuration for motion tracking
        // If you want to use other APIs, add more appropriate to the config
        // like: mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true)
        mConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT);
        mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lock the Tango configuration and reconnect to the service each time
        // the app
        // is brought to the foreground.
        super.onResume();
        if (!mIsTangoServiceConnected) {
            startActivityForResult(
                    Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_MOTION_TRACKING),
                    Tango.TANGO_INTENT_ACTIVITYCODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Tango.TANGO_INTENT_ACTIVITYCODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,
                        "This app requires Motion Tracking permission!",
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            try {
                setTangoListeners();
            } catch (TangoErrorException e) {
                Toast.makeText(this, "Tango Error! Restart the app!",
                        Toast.LENGTH_SHORT).show();
            }
            try {
                mTango.connect(mConfig);
                mIsTangoServiceConnected = true;
            } catch (TangoOutOfDateException e) {
                Toast.makeText(getApplicationContext(),
                        "Tango Service out of date!", Toast.LENGTH_SHORT)
                        .show();
            } catch (TangoErrorException e) {
                Toast.makeText(getApplicationContext(),
                        "Tango Error! Restart the app!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // When the app is pushed to the background, unlock the Tango
        // configuration and disconnect
        // from the service so that other apps will behave properly.
        try {
            mTango.disconnect();
            mIsTangoServiceConnected = false;
        } catch (TangoErrorException e) {
            Toast.makeText(getApplicationContext(), "Tango Error!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View v){
        switch(v.getId()) {
            //adds a point when the button is pressed
            case R.id.Record:
                index++;
                vectors.add(new vector(a1, a2, a3));
                PointsChosen = PointsChosen + "Vector" + index + ": " + vectors.get(index - 1).getX() + ", " + vectors.get(index - 1).getY() + ", " + vectors.get(index - 1).getZ() + "\n";
                break;
            //finishes the recording - Gets the length travelled
            case R.id.Finish_Record:
                for (int x = 0; x < vectors.size(); x++) {
                    for (int y = x + 1; y < vectors.size(); y++) {
                        magnitudes.add(Math.abs(vectors.get(x).magnitude() - vectors.get(y).magnitude()));
                    }
                }
                sums = sumVectors(magnitudes);
                break;
            //Calculates the area
            case R.id.total_area:
                area = CalcArea(vectors, magnitudes);
                break;
            //Calculates the volume
            case R.id.total_volume:
                volume = CalcVolume(vectors, magnitudes);
                break;
            case R.id.Reset:
                area = 0;
                volume = 0;
                sums = 0;
                magnitudes = new ArrayList<Double>();
                vectors = new ArrayList<vector>();
                PointsChosen = "";
                break;

        }
    }

    public double CalcArea (ArrayList<vector> vectors, ArrayList<Double> mag){
        return 0;
        //calculate area
    }

    public double CalcVolume (ArrayList<vector> vectors, ArrayList<Double> mag){
        return 0;
        //calculate volume
    }

    public double sumVectors(ArrayList<Double> mag){
        double sum = 0;
        for(int x = 0; x<mag.size(); x++){
            sum = sum+mag.get(x);
        }
        return sum;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setTangoListeners() {
        // Select coordinate frame pairs
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Add a listener for Tango pose data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            @SuppressLint("DefaultLocale")
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                if (mIsProcessing) {
                    Log.i(TAG, "Processing UI");
                    return;
                }
                mIsProcessing = true;
                
                // Format Translation and Rotation data
                final String translationMsg = String.format(sTranslationFormat,
                        pose.translation[0], pose.translation[1],
                        pose.translation[2]);
                final String rotationMsg = String.format(sRotationFormat,
                        pose.rotation[0], pose.rotation[1], pose.rotation[2],
                        pose.rotation[3]);

                a1 = pose.translation[0];
                a2 = pose.translation[1];
                a3 = pose.translation[2];

                // Output to LogCat
                String logMsg = translationMsg + " | " + rotationMsg;
                Log.i(TAG, logMsg);

                // Display data in TextViews. This must be done inside a
                // runOnUiThread call because
                // it affects the UI, which will cause an error if performed
                // from the Tango
                // service thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTranslationTextView.setText(translationMsg);
                        mRotationTextView.setText(rotationMsg);
                        mRecorded.setText(PointsChosen);
                        mLength.setText("Total Length Traveled: "+sums);
                        mArea.setText("Total Area: "+area);
                        mVolume.setText("Total Volume: "+volume);
                        mIsProcessing = false;
                    }
                });
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData arg0) {
                // Ignoring XyzIj data
            }

            @Override
            public void onTangoEvent(TangoEvent arg0) {
                // Ignoring TangoEvents
            }

			@Override
			public void onFrameAvailable(int arg0) {
				// Ignoring onFrameAvailable Events
				
			}

        });
    }

}