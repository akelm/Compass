package com.example.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView sensorsTV;
    SensorManager sm;
    float[] gravityMatrix = new float[9];
    float[] inclinationMatrix = new float[9];

    float[] geomagnetic = new float[3];
    // dummy array for getRoationMatrix
    float[] gravity = {1f, 1f, 1f};

    double inclination = 0f;
    double meanInclination = 0f;
    int inclinationSize = 5;
    Queue<Double> inclinationValues = new LinkedList<>();
    double radiansToDeg = 180.0 / Math.PI;
    private Sensor geoSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < inclinationSize; i++) {
            inclinationValues.add(.0);
        }
        sensorsTV = findViewById(R.id.sensorsTV);
        initSensors();
    }

    private void initSensors() {
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        geoSensor = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (geoSensor != null) { // register this Activity for events
            sm.registerListener(this, geoSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            sensorsTV.setText(R.string.geomagError);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            geomagnetic = Arrays.copyOf(se.values, 3);
            updateOrientation();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void updateOrientation() {
        SensorManager.getRotationMatrix(gravityMatrix, inclinationMatrix, gravity, geomagnetic);
        inclination = SensorManager.getInclination(inclinationMatrix);
        inclinationValues.remove();
        inclinationValues.add(inclination);
        meanInclination = inclinationValues.stream().mapToDouble(d -> d).average().orElse(.0);
        sensorsTV.setText(String.format(Locale.US, "%d\u00B0", Math.round(360.0 - meanInclination * radiansToDeg) % 360));
    }


}