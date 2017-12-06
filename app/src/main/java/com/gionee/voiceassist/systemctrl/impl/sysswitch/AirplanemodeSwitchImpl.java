package com.gionee.voiceassist.systemctrl.impl.sysswitch;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.gionee.voiceassist.systemctrl.iface.ISwitchCtrl;
import com.gionee.voiceassist.systemctrl.impl.BaseCtrlImpl;

/**
 * Created by liyingheng on 12/6/17.
 */

public class AirplanemodeSwitchImpl extends BaseCtrlImpl implements ISwitchCtrl {

    @Override
    public boolean getState() {
        ContentResolver cr = mAppCtx.getContentResolver();
        return Settings.System.getInt(cr,Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }

    @Override
    public void toggle(boolean enabled, Callback callback) {
        ContentResolver cr = mAppCtx.getContentResolver();
        if (enabled == getState()) {
            Toast.makeText(mAppCtx,"飞行模式已" + (enabled ? "打开":"关闭"), Toast.LENGTH_SHORT).show();
            return;
        }
        Settings.System.putInt(cr,Settings.System.AIRPLANE_MODE_ON, enabled ? 1:0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabled);
        mAppCtx.sendBroadcast(intent);
    }
}
