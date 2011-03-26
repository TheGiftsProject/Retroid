/**
 * 
 */
package ioio.retroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author erez
 *
 */
public class AlarmClockReceiver extends BroadcastReceiver {
	public static boolean status = true;
	public static final String ALARM_INTENT = "retroid.intent.action.ALARM_INTENT";
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
    	Log.i("RetroidService", "Received alarm event");
//    	Intent retroidIntent = new Intent(context, RetroidService.class);
//    	retroidIntent.setAction(ALARM_INTENT);
//    	context.startService(retroidIntent);
////    	context.sendBroadcast(retroidIntent);
	}
}
