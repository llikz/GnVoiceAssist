package com.gionee.voiceassist.usecase.devicecontrol.sysinterface;

/**
 * Created by liyingheng on 10/24/17.
 */

public interface IFlashlight {

    /**
     * 控制手电筒开关
     * @param state
     */
    void setFlashlightEnabled(boolean state);

}
