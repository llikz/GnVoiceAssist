package com.gionee.voiceassist.usecase.devicecontrol.impl;

import android.content.Context;

import com.gionee.voiceassist.usecase.devicecontrol.sysinterface.INfc;

/**
 * Created by liyingheng on 10/24/17.
 */

public class NfcController extends BaseController implements INfc {

    public NfcController(Context ctx) {
        super(ctx);
    }

    @Override
    public void setNfcEnabled(boolean enabled) {

    }
}
