package com.gionee.voiceassist.controller.ttscontrol;


import com.baidu.duer.dcs.devicemodule.voiceoutput.VoiceOutputDeviceModule;

/**
 * Created by twf on 2017/8/26.
 */

public class TtsEventListener implements VoiceOutputDeviceModule.IVoiceOutputListener {
    public static final String TAG = TtsEventListener.class.getSimpleName();

    @Override
    public void onVoiceOutputStarted() {
        TtsController.getInstance().setPlayingState(true);
    }

    @Override
    public void onVoiceOutputFinished() {
        TtsController.getInstance().setPlayingState(false);
        TtsCallback listener = TtsController.getInstance().getSpeakTxtCallbackListener();
        if(listener != null) {
            listener.onSpeakFinish(TtsController.getInstance().getCurrUtterId());
        } else {
            // TODO:
        }
    }

    //TODO Deprecated. Offline TTS should migrate to newer SDK API
    @Deprecated
    public void onTtsOutputStarted() {
        TtsController.getInstance().setPlayingState(true);
    }

    //TODO Deprecated. Offline TTS should migrate to newer SDK API
    @Deprecated
    public void onTtsOutputFinished() {
        TtsController.getInstance().setPlayingState(false);
        TtsCallback listener = TtsController.getInstance().getSpeakTxtCallbackListener();
        if(listener != null) {
            listener.onSpeakFinish(TtsController.getInstance().getCurrUtterId());
        } else {
            // TODO:
        }
    }
}
