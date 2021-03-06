package com.gionee.voiceassist.directiveListener.voiceinput;

import com.baidu.duer.dcs.api.IDialogStateListener;
import com.gionee.voiceassist.basefunction.IBaseFunction;
import com.gionee.voiceassist.controller.recordcontrol.RecordController;
import com.gionee.voiceassist.directiveListener.BaseDirectiveListener;
import com.gionee.voiceassist.util.LogUtil;

/**
 * Created by twf on 2017/8/25.
 */

public class AsrVoiceInputListener extends BaseDirectiveListener implements IDialogStateListener {
    public static final String TAG = AsrVoiceInputListener.class.getSimpleName();
    private IVoiceInputEventListener voiceInputEventListener;
    private DialogState mPreviousState = DialogState.IDLE;

    public AsrVoiceInputListener(IBaseFunction baseFunction) {
        super(baseFunction);
        this.voiceInputEventListener = baseFunction.getVoiceInputEventListener();
    }
//
//    @Override
//    public void onStartRecord() {
//        LogUtil.d(TAG, "onStartRecord");
//        if(voiceInputEventListener != null) {
//            voiceInputEventListener.onVoiceInputStart();
//        }
//    }
//
//    @Override
//    public void onFinishRecord() {
//        LogUtil.d(TAG, "onFinishRecord");
//        if(voiceInputEventListener != null) {
//            voiceInputEventListener.onVoiceInputStop();
//        }
//    }
//
//    @Override
//    public void onSucceed(int i) {
//        // TODO:
//        LogUtil.d(TAG, "onSucceed   statusCode: " + i);
//        if(i != 200) {
//            if(voiceInputEventListener != null) {
//                voiceInputEventListener.onVoiceInputStop();
//            }
//            String msg = GnVoiceAssistApplication.getInstance().
//                    getResources().getString(R.string.voice_err_msg) + " By APP onSucceed   statusCode: " + i;
//            T.showShort(msg);
//        }
//    }
//
//    @Override
//    public void onFailed(String s) {
//        LogUtil.d(TAG, "onFailed    errMsg：" + s);
////        stopRecording();
//        if(voiceInputEventListener != null) {
//            voiceInputEventListener.onVoiceInputStop();
//        }
//        String errorMsg = GnVoiceAssistApplication.getInstance().
//                getResources().getString(R.string.voice_err_msg) + " By APP onFailed    errMsg：" + s;
//        T.showShort(errorMsg);
//    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {
        voiceInputEventListener = null;
    }

    @Override
    public void onDialogStateChanged(DialogState dialogState) {
        LogUtil.i(TAG,"onDialogStateChanged: " + dialogState);
        if (dialogState != mPreviousState) {
            if (dialogState == DialogState.LISTENING) {
                voiceInputEventListener.onVoiceInputStart();
                RecordController.getInstance().setSDKRecording(true);
            } else if (mPreviousState == DialogState.LISTENING){
                RecordController.getInstance().setSDKRecording(false);
                voiceInputEventListener.onVoiceInputStop();
            }
            mPreviousState = dialogState;
        }
    }

}
