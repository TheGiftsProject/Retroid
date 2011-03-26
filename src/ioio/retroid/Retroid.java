package ioio.retroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Retroid extends Activity {
	private static final String LOG_TAG = "RetroidService";
	private static String lastKnownPhoneState = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG,"Let's go");
		setContentView(R.layout.main);

		final Intent retroidIntent = new Intent(this, RetroidService.class);
		startService(retroidIntent);
		
		registerReceivers(retroidIntent);
		
		Toast.makeText(Retroid.this, "After startService", Toast.LENGTH_SHORT).show();

		Button button = (Button) findViewById(R.id.ledbutton);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "Clicked on Button");
		    	retroidIntent.setAction("retroid.intent.action.BLAH_INTENT");
		    	startService(retroidIntent);
//				ledStatus ^= true;
				Toast.makeText(Retroid.this, "Click", Toast.LENGTH_SHORT).show();
			}
		});
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("com.android.deskclock.ALARM_ALERT");
		sendBroadcast(broadcastIntent);
		
//		context = this;
	}
	
	protected void registerReceivers(final Intent retroidIntent) {
		// Alarm clock
		registerReceiver(new BroadcastReceiver(){ 
			@Override 
			public void onReceive(Context context, Intent intent) { 
				Log.i(LOG_TAG,"Broadcasting message Received");
				retroidIntent.setAction("retroid.intent.action.ALARM_INTENT");
				startService(retroidIntent);
			} 
			}, 
		new IntentFilter("com.android.deskclock.ALARM_ALERT"));
		
		// Phone calls
		registerReceiver(new BroadcastReceiver(){ 
			@Override 
			public void onReceive(Context context, Intent intent) { 
				Log.i(LOG_TAG,"Phone state changed");
				if(intent != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
					
		            //State has changed
		            String newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;

		            //See if the new state is 'ringing'
		            if(newPhoneState != null && newPhoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
		            	Log.i(LOG_TAG,"Phone ringing");
		            	retroidIntent.setAction("retroid.intent.action.PHONE_RING_START");
						startService(retroidIntent);
		            }
		            else {
		            	Log.i(LOG_TAG,"Phone not ringing");
		            	retroidIntent.setAction("retroid.intent.action.PHONE_RING_STOP");
						startService(retroidIntent);
		            }
		            lastKnownPhoneState = newPhoneState;
		        }
				

			} 
			}, 
		new IntentFilter("android.intent.action.PHONE_STATE"));
	}
}