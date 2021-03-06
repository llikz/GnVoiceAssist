package com.gionee.voiceassist.directiveListener.voiceinput;

import com.baidu.dcs.acl.AsrEventStatus;
import com.baidu.duer.dcs.devicemodule.voiceinput.VoiceInputDeviceModule;
import com.gionee.voiceassist.util.LogUtil;
import com.gionee.voiceassist.GnVoiceAssistApplication;
import com.gionee.voiceassist.R;
import com.gionee.voiceassist.directiveListener.BaseDirectiveListener;
import com.gionee.voiceassist.util.T;

/**
 * Created by twf on 2017/8/17.
 */

public class VoiceInputListener extends BaseDirectiveListener implements VoiceInputDeviceModule.IVoiceInputListener {
    public static final String TAG = VoiceInputListener.class.getSimpleName();
    private IVoiceInputEventListener voiceInputEvent;

    public VoiceInputListener(IVoiceInputEventListener voiceInputEvent) {
        super(null);
        this.voiceInputEvent = voiceInputEvent;
    }

    @Override
    public void onStart() {
        voiceInputEvent.onVoiceInputStart();
    }

    @Override
    public void onFinish() {
        voiceInputEvent.onVoiceInputStop();
    }

    @Override
    public void onCancel() {
        voiceInputEvent.onVoiceInputStop();
    }

    @Override
    public void onSucceed(int i) {
        // TODO:
        LogUtil.d(TAG, "onSucceed   statusCode: " + i);
        if(i != 200) {
            voiceInputEvent.onVoiceInputStop();
            String msg = GnVoiceAssistApplication.getInstance().
                    getResources().getString(R.string.voice_err_msg) + "onSucceed   statusCode: " + i;
            T.showShort(msg);
        }
    }

    @Override
    public void onFailed(String s) {
        // TODO:
        LogUtil.d(TAG, "onFailed    errMsg：" + s);
//        stopRecording();
        voiceInputEvent.onVoiceInputStop();
        String errorMsg = GnVoiceAssistApplication.getInstance().
                getResources().getString(R.string.voice_err_msg) + "onFailed    errMsg：" + s;
        T.showShort(errorMsg);
    }

    @Override
    public void onAsrEventStatus(AsrEventStatus asrEventStatus) {
        LogUtil.d(TAG, "onAsrEventStatus():" + asrEventStatus);
    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {

    }
}
