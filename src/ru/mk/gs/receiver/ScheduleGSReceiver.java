package ru.mk.gs.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ru.mk.gs.GSService;

import static ru.mk.gs.GSApplication.isEmulation;

/**
 * @author mkasumov
 * For emulation: adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
 */
public class ScheduleGSReceiver extends BroadcastReceiver {

    private static final int PERIOD = isEmulation() ? 10*1000 : 60*1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent service = new Intent(context, GSService.class);
        final PendingIntent pintent = PendingIntent.getService(context, 0, service, 0);
        final AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        final long firstTime = System.currentTimeMillis();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, PERIOD, pintent);
    }
}
