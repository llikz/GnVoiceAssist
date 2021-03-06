package com.gionee.voiceassist.basefunction.alarm;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

import com.gionee.voiceassist.GnVoiceAssistApplication;
import com.gionee.voiceassist.basefunction.BasePresenter;
import com.gionee.voiceassist.basefunction.IBaseFunction;

import java.util.ArrayList;

/**
 * Created by liyingheng on 10/17/17.
 */

public class AlarmPresenter extends BasePresenter implements IAlarmPresenter {

    public static final String TAG = AlarmPresenter.class.getSimpleName();
    public Context mAppCtx;

    public AlarmPresenter(IBaseFunction baseFunction) {
        super(baseFunction);
        mAppCtx = GnVoiceAssistApplication.getInstance().getApplicationContext();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void setAlarm(int hour, int minute, ArrayList<Integer> triggerDays, String message) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR,hour)
                .putExtra(AlarmClock.EXTRA_MINUTES,minute)
                .putExtra(AlarmClock.EXTRA_DAYS,triggerDays)
                .putExtra(AlarmClock.EXTRA_MESSAGE,message);

        startIntent(intent);
    }

    @Override
    public void setTimer(long length) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .putExtra(AlarmClock.EXTRA_LENGTH,length)
                .putExtra(AlarmClock.EXTRA_SKIP_UI,false);

        startIntent(intent);
    }

    private void startIntent(Intent intent) {
        if (intent.resolveActivity(mAppCtx.getPackageManager()) != null) {
            mAppCtx.startActivity(intent);
        }
    }

}
