package ru.mk.gs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ru.mk.gs.activity.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mkasumov
 */
public class OutgoingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null == bundle) {
            return;
        }

        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (getCorrectCode().equals(phoneNumber)) {
            startMainActivity(context);
            setResultData(null);
        }
    }

    private String getCorrectCode() {
        return inverse(new SimpleDateFormat("HHmm").format(new Date()));
    }

    private String inverse(String s) {
        int len = s.length();
        char chars[] = new char[len];
        s.getChars(0, len, chars, 0);
        for (int i = 0; i < len / 2; i++) {
            char tmp = chars[i];
            chars[i] = chars[len-i-1];
            chars[len-i-1] = tmp;
        }
        return new String(chars);
    }

    private void startMainActivity(Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
