/**
 * 
 */
package ioio.retroid;

import ioio.api.DigitalInput;
import ioio.api.DigitalOutput;
import ioio.api.IOIOLib;
import ioio.lib.Constants;
import ioio.lib.IOIOImpl;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author erez
 * 
 */
public class RetroidService extends Service {
	private ioio.retroid.RetroidService.IOIOThread thread;
	protected boolean loop;
	protected boolean ledStatus = true;
	private Object action;
	protected static final String LOG_TAG = "RetroidService";

	public RetroidService() {
	}
	
	@Override
	public  void onCreate() {
		super.onCreate();
		thread = new IOIOThread();
		loop = true;
		action = null;
		thread.start();
		
	}
	
	@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.i(LOG_TAG,"Start Command " + intent.getAction());
			if ("retroid.intent.action.BLAH_INTENT".equals(intent.getAction())){
				action = "blink";
			} 
			else if ("retroid.intent.action.ALARM_INTENT".equals(intent.getAction())) {
				action = "alarm";
			}
			else if ("retroid.intent.action.PHONE_RING_START".equals(intent.getAction())) {
				action = "ring_start";
			}
			else if ("retroid.intent.action.PHONE_RING_STOP".equals(intent.getAction())) {
				action = "ring_stop";
			}
			else {
				action = null;
			}
			return super.onStartCommand(intent, flags, startId);

		}
	
	@Override
		public void onDestroy() {
			loop = false;
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			super.onDestroy();
		}
	
	
	private class IOIOThread extends Thread {
		@Override
		public void run() {
			Log.i(LOG_TAG,"IOIO Thread Started");
			IOIOLib ioio = new IOIOImpl();
			boolean ledStatus=  false;
			while(loop){
				try {
					ioio.waitForConnect();
					Log.i(LOG_TAG, "Connected to IOIO");
					DigitalOutput led = ioio.openDigitalOutput(Constants.LED_PIN,true);
					DigitalInput in = ioio.openDigitalInput(17);
					ioio.openDigitalOutput(12,true).write(false);
					led.write(false);
					Thread.sleep(300);
					led.write(true);
					
					while(loop){
						if ("alarm".equals(action)) {
							boolean input = in.read();
							if (!input) action = "snooze";
							led.write(ledStatus);
							ledStatus = false;
						}
						else if ("snooze".equals(action)) {
							Intent broadcastIntent = new Intent();
							broadcastIntent.setAction("com.android.deskclock.ALARM_SNOOZE");
							sendBroadcast(broadcastIntent);
							action = null;
						}
						else if ("ring_start".equals(action)) {
							led.write(ledStatus);
							ledStatus ^= true;
						}
						else if ("ring_stop".equals(action)) {
							action = null;
						}
						else if ("blink".equals(action)){
							boolean input = in.read();
							if (!input) action = null;
							led.write(ledStatus);
							ledStatus ^= true;
							
						} else {
							boolean input = in.read();
							if (!input) action = "blink";
							led.write(true);
						}
						Thread.sleep(100);
						Log.i(LOG_TAG,"loop action = " + action);
					}
				} catch (Exception e) {
					Log.e(LOG_TAG,"Exception in IOIO Thread " + e.getMessage());
				} finally{
					Log.i(LOG_TAG,"IOIO Thread : finally");
					ioio.disconnect();
				}
			}
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
//	@Override
//	protected void onHandleIntent(Intent intent) {
////		 Log.i(LOG_TAG, "Received alarm broadcast");
//		// ledStatus = false;
//		if (intent != null && intent.getAction() != null){
//			Log.i(LOG_TAG,"intent" + intent.getAction());
//		}
//		else {
//			return;
//		}
//		String action = intent.getAction();
//		if (action.equals("retroid.intent.action.BLAH_INTENT")) {
//			loop = true;
//			try {
//				ioio.waitForConnect();
//				DigitalOutput led = ioio.openDigitalOutput(37,
//						true);
//				DigitalOutput led2 = ioio.openDigitalOutput(38,
//						true);
//				DigitalOutput curLed = led;
//				Log.i(LOG_TAG, "before loop"+loop);
//				while (loop) {
//					if (curLed == led)
//						curLed = led2;
//					else
//						curLed = led;
//					Log.i(LOG_TAG, "in loop");
//					curLed.write(false);
//					Thread.sleep(100);
//					curLed.write(true);
//					Thread.sleep(100);
//				}
//			} catch (final Exception e) {
//				Log.i(LOG_TAG, e.getMessage());
//			} finally {
//				Log.i(LOG_TAG, "finally");
//				ioio.disconnect();
//			}
//		}
//		else if (action.equals("retroid.intent.action.ALARM_INTENT")) {
//			loop = true;
//			try {
//				Log.i(LOG_TAG,"in Alarm intent");
//				ioio.waitForConnect();
//
//				DigitalOutput led = ioio.openDigitalOutput(Constants.LED_PIN,
//						true);
//				DigitalOutput curLed = led;
//				Log.i(LOG_TAG, "before loop"+loop);
//				while (loop) {
//					Log.i(LOG_TAG, "in loop");
//					curLed.write(false);
//					Thread.sleep(500);
//					curLed.write(true);
//					Thread.sleep(500);
//				}
//			} catch (final Exception e) {
//				Log.i(LOG_TAG, e.getMessage());
//			} finally {
//				Log.i(LOG_TAG, "finally");
//				ioio.disconnect();
//			}
//		}
//		else if (action.equals("retroid.intent.action.PHONE_START")) {
//			Log.i(LOG_TAG, "Service: phone call now");
//			loop = true;
//			try {
//				ioio.waitForConnect();
//
//				DigitalOutput led = ioio.openDigitalOutput(Constants.LED_PIN,
//						true);
//				DigitalOutput curLed = led;
//				Log.i(LOG_TAG, "before loop"+loop);
//				while (loop) {
//					Log.i(LOG_TAG, "in loop");
//					curLed.write(false);
//					Thread.sleep(500);
//					curLed.write(true);
//					Thread.sleep(500);
//				}
//			} catch (final Exception e) {
//				Log.i(LOG_TAG, e.getMessage());
//			} finally {
//				Log.i(LOG_TAG, "finally");
//				ioio.disconnect();
//			}
//		}
//		
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
