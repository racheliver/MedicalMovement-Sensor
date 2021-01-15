package com.chenp_racheliv.finalProject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class IndicesActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private SensorManager sensorManager;
    private Sensor acceleratorSensor;

    private TextView txvTimer;
    private String timeString = "00:00";
    private long timeLeft = 11;

    private TextView txvAvg;
    private TextView txvAvgTitle;
    private String avgString;

    private LineChart lineChart;
    private Thread chartThread;
    private CountDownTimer timerThread;
    private boolean plotData = true;

    public static float average = 0;
    public static float sum = 0;
    public static float count = 0;

    private float lastRestAverage;
    private float lastHalfRestAverage;
    private float lastMovementAverage;

    private Button btnBack;
    private Button btnStart;
    private boolean isPausedInCall;
    private boolean isPausedInExit;

    private ProgressBar prg;
    private TextView veryLow;
    private TextView low;
    private TextView high;
    private TextView veryHigh;

    private TextView txvLastMeas;
    private String lastAvgRestString;
    private String lastAvgHalfRestString;
    private String lastAvgMovementString;

    public static final int REST = 0;
    public static final int HALF_REST = 1;
    public static final int MOVEMENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indices);

        // Hide the Activity Action Bar
        getSupportActionBar().hide();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get all refrences from XML
        txvTimer = findViewById(R.id.txvTimerID);
        txvAvg = findViewById(R.id.txvAvgID);
        txvAvgTitle = findViewById(R.id.txvAvgTitleID);
        lineChart = (LineChart) findViewById(R.id.chartID);
        btnBack = findViewById(R.id.btnBackID);
        btnStart = findViewById(R.id.btnStartID);
        prg = (ProgressBar) findViewById(R.id.prgID);
        veryLow = findViewById(R.id.veryLowID);
        low = findViewById(R.id.lowID);
        high = findViewById(R.id.highID);
        veryHigh = findViewById(R.id.veryHighID);
        txvLastMeas = findViewById(R.id.txvLastMeasID);

        // event listeners
        btnBack.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        // 1. get SensorManager from the System SENSOR_SERVICE
        // ---------------------------------------------------
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 2. get the needed Sensors. (if null returns - sensor not exist on device!)
        // --------------------------------------------------------------------------
        acceleratorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // 3. check if one of the sensors not exist in the device?
        // --------------------------------------------------------
        if (acceleratorSensor == null) {
            String sensorErrMsg = "";
            if (acceleratorSensor == null)
                sensorErrMsg += "\nOrientaion Sensor NOT exists!";

            sensorErrMsg += "\nThe app will exit!";

            Toast.makeText(this, sensorErrMsg, Toast.LENGTH_LONG).show();
            finish();
        }

        // check if call is entering
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    isPausedInCall = true;
                }
                else if(state == TelephonyManager.CALL_STATE_IDLE)
                {
                    isPausedInCall = false;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        // chart settings
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setBackgroundColor(getResources().getColor(R.color.colorBackground));

        LineData data = new LineData();
        data.setValueTextColor(getResources().getColor(R.color.transparentWhite));
        lineChart.setData(data);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(getResources().getColor(R.color.transparentWhite));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.transparentWhite));
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(2.2f);
        leftAxis.setAxisMinimum(-2.2f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(true);
    }

    // thread that responsible to broadcast the chart continuously
    private void feedMultiple() {
        if (chartThread != null){
            chartThread.interrupt();
        }

        chartThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(timeLeft > 0){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        chartThread.start();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        isPausedInExit = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sensorManager.unregisterListener(IndicesActivity.this);
        chartThread.interrupt();
        timerThread.cancel();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("Previous measurements", MODE_PRIVATE);
        lastRestAverage = sharedPreferences.getFloat("last rest measurement", 0);
        lastHalfRestAverage = sharedPreferences.getFloat("last half-rest measurement", 0);
        lastMovementAverage = sharedPreferences.getFloat("last movement measurement", 0);

        // start listen to sensor
        sensorManager.registerListener(this, acceleratorSensor, SensorManager.SENSOR_DELAY_GAME);

        if(isPausedInCall || isPausedInExit)
            countDown();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("Previous measurements", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(InstructionsActivity.currentPage == REST)
            editor.putFloat("last rest measurement", average);
        else if(InstructionsActivity.currentPage == HALF_REST)
            editor.putFloat("last half-rest measurement", average);
        else if(InstructionsActivity.currentPage == MOVEMENT)
            editor.putFloat("last movement measurement", average);
        editor.commit();

        isPausedInExit=true;

        //stop the thread
        if (chartThread!=null)
            chartThread.interrupt();

        if (timerThread!=null)
            timerThread.cancel();

        // disable sensor
        sensorManager.unregisterListener(this);
    }

    // thread that responsible for timer and display indices data
    private void countDown()
    {
        timerThread = new CountDownTimer(timeLeft*1000, 1000)
        {
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished / 1000;
                timeString = String.format("00:%02d", timeLeft);
                txvTimer.setText(timeString);
            }

            public void onFinish() {
                timeLeft = 0;
                txvTimer.setVisibility(View.INVISIBLE);

                average = sum/count;
                txvAvg.setVisibility(View.VISIBLE);
                avgString = String.format("%.5f",average);
                txvAvg.setText(avgString);
                txvAvgTitle.setVisibility(View.VISIBLE);

                if(average <= 0.09)
                {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarGreen), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(25);
                    veryLow.setVisibility(View.VISIBLE);
                }

                else if(average > 0.09 && average <= 0.28) {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarYellow), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(50);
                    low.setVisibility(View.VISIBLE);
                }

                else if(average > 0.28 && average < 0.5) {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarOrange), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(75);
                    high.setVisibility(View.VISIBLE);
                }

                else {
                    prg.setVisibility(View.VISIBLE);
                    prg.getProgressDrawable().setColorFilter(getResources().getColor(R.color.progressBarRed), android.graphics.PorterDuff.Mode.SRC_IN);
                    prg.setProgress(100);
                    veryHigh.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    // add entry to chart - all entries are movements in x-axis
    private void addEntry(SensorEvent event)
    {
        LineData data = lineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), event.values[0]), 0);
            data.notifyDataChanged();

            sum+=Math.abs(event.values[0]);
            count=set.getEntryCount();

            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(150);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
        }

        // display last measurement
        if(InstructionsActivity.currentPage == REST){
            lastAvgRestString = String.format("%.5f",lastRestAverage);
            txvLastMeas.setText("Last Rest Measurment: " + lastAvgRestString);
        }

        else if(InstructionsActivity.currentPage == HALF_REST){
            lastAvgHalfRestString = String.format("%.5f",lastHalfRestAverage);
            txvLastMeas.setText("Last Half-Rest Measurment: " + lastAvgHalfRestString);
        }

        else if(InstructionsActivity.currentPage == MOVEMENT){
            lastAvgMovementString = String.format("%.5f",lastMovementAverage);
            txvLastMeas.setText("Last Movement Measurment: " + lastAvgMovementString);
        }
    }

    // create the chart
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Intensity of Tremor");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(getResources().getColor(R.color.orange));
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(plotData){
            addEntry(event);
            plotData = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnStartID:
                btnStart.setVisibility(View.INVISIBLE);
                countDown();
                feedMultiple();
                break;

            case R.id.btnBackID:
                Intent intent = new Intent(view.getContext(), InstructionsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
