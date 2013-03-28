package edu.neu.madcourse.michaelallen.project;

import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class AccelerometerListenerService extends Service implements SensorEventListener{
	private static final String TAG = "AccelerometerListener";
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	float x;
	float y;
	float z;
	long lastEventTime = 0;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	@Override
	public void onCreate (){
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		
	}

	@Override
	public void onSensorChanged(SensorEvent ev) {
		x = ev.values[0];
		y = ev.values[1];
		z = ev.values[2];
		
		
		
		Log.d(TAG, Calendar.getInstance().getTime().getTime() - lastEventTime + " at:: " + x + " " + y + " " + z);
		
	}
	
	
}