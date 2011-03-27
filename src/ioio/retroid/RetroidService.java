/**
 * 
 */
package ioio.retroid;

import ioio.api.DigitalOutput;
import ioio.api.IOIOLib;
import ioio.api.PwmOutput;
import ioio.lib.Constants;
import ioio.lib.IOIOImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

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
			else if ("retroid.intent.action.SMS_INTENT".equals(intent.getAction())) {
				action = "sms";
			}
			else if ("retroid.intent.action.SNOOZE".equals(intent.getAction())) {
				action = "snooze";
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
		public int[] led_pins = {13,28,14,11,10,12,6,5,3,4,30,29};
		
		public int engine_pin = 31;
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
					List<DigitalOutput> leds = new ArrayList<DigitalOutput>();
					for (int pin : led_pins) {
						leds.add(ioio.openDigitalOutput(pin, true));
					}
					PwmOutput engine = ioio.openPwmOutput(engine_pin, 100);
					
					float engineStrength = 0.4f;
					float engineModifier = 0.01f;
					
					int smsLoopsDefault = 20;
					int smsLoops = smsLoopsDefault;
					
					int current_led = 0;
					
					int fill_led = 0;
					int fill_led_offset = 0;
					boolean fill_command = true;
					
					led.write(false);
					Thread.sleep(200);
					led.write(true);
					Thread.sleep(200);
					led.write(false);
					Thread.sleep(200);
					led.write(true);
					
					
					try{
					while(loop){
						if ("alarm".equals(action)) {
							engine.setDutyCycle(engineStrength);
							engineStrength += engineModifier;
							if (engineStrength >= 1 || (engineStrength <= 0.3f)){
								engineModifier *= -1;
							}
							for (DigitalOutput digitalOutput : leds) {
								digitalOutput.write(leds.get(current_led) == digitalOutput);
							}
							current_led = (current_led+1) % 12;
							
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
							for (DigitalOutput digitalOutput : leds) {
								digitalOutput.write(true);
							}
						}
						else if ("ring_stop".equals(action)) {
							action = null;
						}
						else if ("sms".equals(action)) {
							if (smsLoops <= 0){
								action = null;
								smsLoops = smsLoopsDefault;
							} else {
								smsLoops -= 1;
							}
							led.write(ledStatus);
							ledStatus ^= true;
							for (DigitalOutput digitalOutput : leds) {
								digitalOutput.write(ledStatus);
							}
							engine.setDutyCycle(0.3f);
						}
						else if ("blink".equals(action)){
							engine.setDutyCycle((float) 0.2);
							led.write(ledStatus);
							ledStatus ^= true;
							
							
							leds.get((fill_led_offset + fill_led)%12).write(fill_command);
							leds.get((12-fill_led+fill_led_offset)%12).write(fill_command);
							if (fill_led == 6) {
								fill_command ^= true;
								if (fill_command==true) fill_led_offset= new Random().nextInt(12);
							}
							fill_led = (fill_led+1)%7;

						} else {
							led.write(true);
							for (DigitalOutput digitalOutput : leds) {
								digitalOutput.write(leds.get(Calendar.getInstance().get(Calendar.HOUR)) == digitalOutput);
							}
							engine.setDutyCycle(0);
						}
						Thread.sleep(100);
//						Log.i(LOG_TAG,"loop action = " + action);
					} } finally {
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
////	}
//	
//	DigitalOutput snoozeOut;
//	DigitalInput snoozeIn;
//	snoozeOut = ioio.openDigitalOutput(11,true);
//	
//	while (true) {
//		int cycle_len = 0;
//		
//		snoozeOut.write(false);
//		Thread.sleep(200);
//
//		snoozeOut.close();
//		snoozeIn = ioio.openDigitalInput(11,DigitalInputMode.FLOATING);
//		
//		while(!snoozeIn.read()) {
//			cycle_len++;
//			if (cycle_len % 100 == 0) {
//				led.write(ledStatus);
//				ledStatus ^= true;
//			}
//		} 
//		
//		snoozeIn.close();
//		snoozeOut = ioio.openDigitalOutput(11,true);
//		
//		Log.i(LOG_TAG, "Input: " + cycle_len);
//		cycle_len = 0;
//	}

}
