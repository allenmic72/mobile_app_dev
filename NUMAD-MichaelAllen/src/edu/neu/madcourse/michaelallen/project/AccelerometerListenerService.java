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
	
	float runningAverage;
	int eventCount;
	float[] priorX;
	float[] priorY;
	float[] priorZ;
	
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
		
		priorX = new float[50];
		priorY = new float[50];
		priorZ = new float[50];
		eventCount = 0;
		
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
		highPassFilter(ev.values[0], ev.values[1], ev.values[2]);
		
		
		
		
	}
	
	private static final boolean ADAPTIVE_ACCEL_FILTER = true;
	float lastAccel[] = new float[3];
	float accelFilter[] = new float[3];

	/**
	 * Adaptation of Apple's high pass filter
	 * Gotta be good since it's an apple product
	 * @param accelX
	 * @param accelY
	 * @param accelZ
	 */
	public void highPassFilter(float accelX, float accelY, float accelZ) {
	    // high pass filter
	    float updateFreq = 30; // match this to your update speed
	    float cutOffFreq = 1.0f;
	    float RC = 1.0f / cutOffFreq;
	    float dt = 1.0f / updateFreq;
	    float filterConstant = RC / (dt + RC);
	    float alpha = filterConstant; 
	    float kAccelerometerMinStep = 0.033f;
	    float kAccelerometerNoiseAttenuation = 3.0f;

	    if(ADAPTIVE_ACCEL_FILTER)
	    {
	        float d = clamp(Math.abs(norm(accelFilter[0], accelFilter[1], accelFilter[2]) - norm(accelX, accelY, accelZ)) / kAccelerometerMinStep - 1.0f, 0.0f, 1.0f);
	        alpha = d * filterConstant / kAccelerometerNoiseAttenuation + (1.0f - d) * filterConstant;
	    }

	    accelFilter[0] = (float) (alpha * (accelFilter[0] + accelX - lastAccel[0]));
	    accelFilter[1] = (float) (alpha * (accelFilter[1] + accelY - lastAccel[1]));
	    accelFilter[2] = (float) (alpha * (accelFilter[2] + accelZ - lastAccel[2]));

	    lastAccel[0] = accelX;
	    lastAccel[1] = accelY;
	    lastAccel[2] = accelZ;
	    
	    Log.d(TAG, Calendar.getInstance().getTime().getTime() - lastEventTime + " at:: " +  
	    		accelFilter[0] + " " + accelFilter[1] + " " + accelFilter[2]);
	}
	
	private float norm(float x, float y, float z){
		 return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	private float clamp(float v, float min, float max){
		if(v > max)
	        return max;
	    else if(v < min)
	        return min;
	    else
	        return v;
	}
	
	
}