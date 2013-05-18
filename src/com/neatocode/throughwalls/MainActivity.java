package com.neatocode.throughwalls;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class MainActivity extends Activity implements SensorEventListener,
		LocationListener {
	
	private static final String LOG_TAG = "ThroughWalls";

	private SensorManager mSensorManager;

	private Sensor mOrientation;

	private LocationManager mLocationManager;
	
	private Display mDisplay;
	
	private List<Target> mTargets;
	
	private int mCurrentTargetIndex;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// TODO supposed to be more accurate to compose compass and accelerometer yourself
		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mDisplay = new Display(this);

		mTargets = new LinkedList<Target>();
		// Use NYC, Beijing as test targets for now.
		// TODO add cameras, shelters, etc..
		// TODO sort nearest first
		mTargets.add(Target.BEIJING);
		mTargets.add(Target.NYC);
		mDisplay.setTarget(mTargets.get(mCurrentTargetIndex));
		
		// TODO use default location as Palo Alto for now.
		mDisplay.setLocation(Target.PALO_ALTO.asLocation());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			mCurrentTargetIndex++;
			if ( mCurrentTargetIndex >= mTargets.size() ) {
				mCurrentTargetIndex = 0;
			}
			mDisplay.setTarget(mTargets.get(mCurrentTargetIndex));
			return true;
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		// Do nothing.
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mOrientation,
				SensorManager.SENSOR_DELAY_NORMAL);
		final List<String> providers = mLocationManager.getAllProviders();
		for(String provider : providers ) {
			// Set last known location if we have it.
			// TODO indicate to user if very out of date?
			// Time label in corner? some sort of scanner ping lines moving outward?		
			final Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
			mDisplay.setLocation(lastKnownLocation);			
			
			final boolean enabled = mLocationManager.isProviderEnabled(provider);
			Log.i(LOG_TAG, "Found provider: " + provider + ", enabled = " + enabled);
			mLocationManager.requestLocationUpdates(provider, 0, 0, this);
		}		
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		float azimuth_angle = event.values[0];
		float pitch_angle = event.values[1];
		float roll_angle = event.values[2];
		mDisplay.setOrientation(azimuth_angle, pitch_angle, roll_angle);
	}
	
	@Override
	public void onLocationChanged(final Location location) {
		mDisplay.setLocation(location);
	}

	@Override
	public void onProviderDisabled(final String provider) {
		// TODO warn user if no providers enabled
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}

}
