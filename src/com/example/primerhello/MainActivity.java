package com.example.primerhello;

import java.io.FileOutputStream;
import java.math.BigDecimal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements SensorEventListener {
    
	private SensorManager mSensorManager = null;
	private Sensor mAccelerometer = null;
	private float accel_max = 0;
	private float accel_min = 100;
	private float last_timestamp = 0;
	private float timestamp_delta_min = 1000;
	private String data_button_state = "Start"; // Button initial state "Start"
	private FileOutputStream fos;
	private String file_name;
	private float start_timestamp;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        setContentView(R.layout.main);
        
        final Button button_reset = (Button) findViewById(R.id.reset);
        
        button_reset.setVisibility(View.VISIBLE);
        
        button_reset.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.v(getPackageName(), "onClickListener Button Reset");
                accel_max = 0;
                accel_min = 100;
                timestamp_delta_min = 1000;
            }
        });
        
        final Button button_data = (Button) findViewById(R.id.data);
        
        button_data.setVisibility(View.VISIBLE);
        
        button_data.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.v(getPackageName(), "onClickListener Button Data");
                if (data_button_state == "Start") {
                	data_button_state = "Stop";
                	start_timestamp = 0;
                	button_data.setText(data_button_state);
                	try {
                	    file_name = getResources().getString(R.string.data_file_name);
            			fos = openFileOutput(file_name, MODE_PRIVATE);
            			fos.write(" Hello, I love you!\n".getBytes());
            			Log.i(getPackageName(), "Open and wrote to File: " + file_name);
            		} catch (Exception e) {
            			Log.i(getPackageName(), "openFileOutput (new file) threw exception: "
            					+ e.getMessage());
            		}
                } else {
                	data_button_state = "Start";
                	button_data.setText(data_button_state);
                	try {
                		fos.close();
                		Log.i(getPackageName(), "Close: " + file_name);
            		} catch (Exception e) {
            			Log.i(getPackageName(), "fos.close threw exception: "
            					+ e.getMessage());
            		}
                }
            }
        });
        
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	boolean isAvailable = mSensorManager.registerListener(MainActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    	// Nokia X2, Sampling rate: 
    	//     SENSOR_DELAY_FASTEST, 10ms;
    	//     SENSOR_DELAY_GAME, 20ms;
    	//     SENSOR_DELAY_NORMAL, 200ms;
    	//     SENSOR_DELAY_UI, 70ms

    	if (!isAvailable) {
    		Log.e(getPackageName(), " ACCELEROMETER is not available!");
    	}
    	
        TextView text_disp = (TextView)findViewById(R.id.discription);
        text_disp.setText("Discription:"+mAccelerometer.toString());
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
        	mSensorManager.unregisterListener(this);
        }
    	super.onPause();
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.
	    float x = e.getX();
	    float y = e.getY();
	    switch (e.getAction()) {
	        case MotionEvent.ACTION_MOVE:
	            Log.d(getPackageName(), " MOVE -- X >>"+x);
	            Log.d(getPackageName(), " MOVE -- Y >>"+y);
	             
	            TextView text_x = (TextView)findViewById(R.id.panel_x);
	            text_x.setText("Penal_X:"+x);
	            TextView text_y = (TextView)findViewById(R.id.panel_y);
	            text_y.setText("Penal_Y:"+y);
	            
	        	if (mAccelerometer != null) {
		            long min_delay = mAccelerometer.getMinDelay();
		        	Log.d(getPackageName(), " Accelerometer's MinDelay:"+min_delay);
	        	}
	        	     
	    }
	    return true;
	 
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float accel_x = event.values[0];
		float accel_y = event.values[1];
		float accel_z = event.values[2];
		float current_timestamp = event.timestamp / 1000000;
		float timestamp_delta = current_timestamp - last_timestamp;
		last_timestamp = current_timestamp;
		
		if (timestamp_delta < timestamp_delta_min) {
			timestamp_delta_min = timestamp_delta;
		}
		
		BigDecimal b = new BigDecimal((float)Math.sqrt(Math.pow(accel_x, 2)+Math.pow(accel_y, 2)+Math.pow(accel_z, 2)));
		float accel = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		
		if (accel < accel_min) {
			accel_min = accel;
		}
		
		if (accel > accel_max) {
			accel_max = accel;
		}
		
		TextView text_acc_x = (TextView)findViewById(R.id.acc_x);
		text_acc_x.setText("Accel_X:"+accel_x);
		TextView text_acc_y = (TextView)findViewById(R.id.acc_y);
		text_acc_y.setText("Accel_Y:"+accel_y);
		TextView text_acc_z = (TextView)findViewById(R.id.acc_z);
		text_acc_z.setText("Accel_Z:"+accel_z);
		
		TextView text_acc = (TextView)findViewById(R.id.acc);
		text_acc.setText("Accel_Final:"+accel);
		TextView text_acc_max = (TextView)findViewById(R.id.acc_max);
		text_acc_max.setText("Accel_Max:"+accel_max);
		TextView text_acc_min = (TextView)findViewById(R.id.acc_min);
		text_acc_min.setText("Accel_Min:"+accel_min);
		
		TextView text_timestamp_delta = (TextView)findViewById(R.id.timestamp_delta);
		text_timestamp_delta.setText("TimeStamp_D:"+timestamp_delta+"ms");
		TextView text_timestamp_delta_min = (TextView)findViewById(R.id.timestamp_delta_min);
		text_timestamp_delta_min.setText("TimeStamp_D_Min:"+timestamp_delta_min+"ms");
		
		if (data_button_state == "Stop") {
			if (start_timestamp == 0) {
				start_timestamp = current_timestamp;
			}
			
			float recode_timestamp = current_timestamp - start_timestamp;
			String strFileContents = recode_timestamp+","+accel_x+","+accel_y+","+accel_z+","+accel+"\n";
			
			try {
        	    fos.write(strFileContents.getBytes());
    		} catch (Exception e) {
    			Log.i(getPackageName(), "Try to write file threw exception: "
    					+ e.getMessage());
    		}
		}
	}
    
}
