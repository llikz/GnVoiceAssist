package com.gionee.gnvoiceassist.basefunction.devicecontrol.impl;

import android.content.Context;

import com.gionee.gnvoiceassist.basefunction.devicecontrol.sysinterface.INoDisturb;

/**
 * Created by liyingheng on 10/24/17.
 */

public class NoDisturbController extends BaseController implements INoDisturb {
    public NoDisturbController(Context ctx) {
        super(ctx);
    }

    @Override
    public void setNoDisturbEnabled(boolean enabled) {

    }
}
