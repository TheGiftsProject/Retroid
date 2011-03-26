/**
 * 
 */
package ioio.retroid;

import ioio.api.DigitalOutput;
import ioio.api.IOIOLib;
import ioio.lib.Constants;
import ioio.lib.IOIOImpl;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author erez
 * 
 */
public class RetroidService extends IntentService {
//	private ioio.retroid.RetroidService.IOIOThread thread;
	protected boolean loop;
	protected boolean ledStatus = true;
	private IOIOLib ioio;
	protected static final String LOG_TAG = "RetroidService";

	public RetroidService() {
		super("RetroidService");
	}
	
	@Override
	public  void onCreate() {
		super.onCreate();
		 ioio = new IOIOImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
//		 Log.i(LOG_TAG, "Received alarm broadcast");
		// ledStatus = false;
		if (intent != null && intent.getAction() != null){
			Log.i(LOG_TAG,"intent" + intent.getAction());
		}
		else {
			return;
		}
		String action = intent.getAction();
		if (action.equals("retroid.intent.action.BLAH_INTENT")) {
			loop = true;
			try {
				ioio.waitForConnect();

				DigitalOutput led = ioio.openDigitalOutput(37,
						true);
				DigitalOutput led2 = ioio.openDigitalOutput(38,
						true);
				DigitalOutput curLed = led;
				Log.i(LOG_TAG, "before loop"+loop);
				while (loop) {
					if (curLed == led)
						curLed = led2;
					else
						curLed = led;
					Log.i(LOG_TAG, "in loop");
					curLed.write(false);
					Thread.sleep(100);
					curLed.write(true);
					Thread.sleep(100);
				}
			} catch (final Exception e) {
				Log.i(LOG_TAG, e.getMessage());
			} finally {
				Log.i(LOG_TAG, "finally");
				ioio.disconnect();
			}
		}
		else if (action.equals("retroid.intent.action.ALARM_INTENT")) {
			loop = true;
			try {
				ioio.waitForConnect();

				DigitalOutput led = ioio.openDigitalOutput(Constants.LED_PIN,
						true);
				DigitalOutput curLed = led;
				Log.i(LOG_TAG, "before loop"+loop);
				while (loop) {
					Log.i(LOG_TAG, "in loop");
					curLed.write(false);
					Thread.sleep(500);
					curLed.write(true);
					Thread.sleep(500);
				}
			} catch (final Exception e) {
				Log.i(LOG_TAG, e.getMessage());
			} finally {
				Log.i(LOG_TAG, "finally");
				ioio.disconnect();
			}
		}
		else if (action.equals("retroid.intent.action.PHONE_START")) {
			Log.i(LOG_TAG, "Service: phone call now");
			loop = true;
			try {
				ioio.waitForConnect();

				DigitalOutput led = ioio.openDigitalOutput(Constants.LED_PIN,
						true);
				DigitalOutput curLed = led;
				Log.i(LOG_TAG, "before loop"+loop);
				while (loop) {
					Log.i(LOG_TAG, "in loop");
					curLed.write(false);
					Thread.sleep(500);
					curLed.write(true);
					Thread.sleep(500);
				}
			} catch (final Exception e) {
				Log.i(LOG_TAG, e.getMessage());
			} finally {
				Log.i(LOG_TAG, "finally");
				ioio.disconnect();
			}
		}
		
	}
}
