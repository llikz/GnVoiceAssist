package com.gionee.voiceassist.directiveListener;

import com.gionee.voiceassist.basefunction.IBaseFunction;
import com.gionee.voiceassist.directiveListener.customuserinteraction.ICUIDirectiveReceivedInterface;
import com.gionee.voiceassist.controller.ttscontrol.TtsCallback;
import com.gionee.voiceassist.controller.ttscontrol.TtsController;

/**
 * Created by twf on 2017/8/14.
 */

public abstract class BaseDirectiveListener
        implements TtsCallback, ICUIDirectiveReceivedInterface{
    protected IBaseFunction iBaseFunction;

    public BaseDirectiveListener(IBaseFunction iBaseFunction) {
        if(iBaseFunction == null) {
            throw new IllegalArgumentException("iBaseFunction cannot be null!");
        }
        this.iBaseFunction = iBaseFunction;
    }

    @Override
    public void onSpeakStart() {

    }

    @Override
    public void onSpeakFinish(String utterId) {

    }

    @Override
    public void onSpeakError(TtsController.TtsResultCode ttsResultCode, String s) {

    }

    public void handleCUInteractionUnknownUtterance(String id) {

    }

    public void handleCUInteractionTargetUrl(String id, String url) {

    }

    /*public abstract void procOtherDirectiveCalling();

    public abstract boolean procRecoError(int error);

    public abstract void stopFocus();*/

    protected void playTTS(String ttsText, String utterId, TtsCallback listener) {
        TtsController.getInstance().playTTS(ttsText, utterId, listener);
    }

    protected void playTTS(String ttsText, String utterId, TtsCallback listener, boolean showPlayTextInScreen) {
        TtsController.getInstance().playTTS(ttsText, utterId, listener);
        if(showPlayTextInScreen) {
            iBaseFunction.getScreenRender().renderAnswerInScreen(ttsText);
        }
    }

    protected void playTTS(String ttsText) {
        TtsController.getInstance().playTTS(ttsText);
    }

    protected void playTTS(String ttsText, boolean showPlayTextInScreen) {
        TtsController.getInstance().playTTS(ttsText);
        if(showPlayTextInScreen) {
            iBaseFunction.getScreenRender().renderAnswerInScreen(ttsText);
        }
    }

    /**
     * 释放资源
     */
    public abstract void onDestroy();
}
