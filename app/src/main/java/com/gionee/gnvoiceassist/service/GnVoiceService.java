package com.gionee.gnvoiceassist.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gionee.gnvoiceassist.basefunction.contact.ContactObserver;
import com.gionee.gnvoiceassist.message.model.CUIEntity;
import com.gionee.gnvoiceassist.message.model.DirectiveResponseEntity;
import com.gionee.gnvoiceassist.message.model.UsecaseResponseEntity;
import com.gionee.gnvoiceassist.message.model.render.RenderEntity;
import com.gionee.gnvoiceassist.usecase.AlarmUseCase;
import com.gionee.gnvoiceassist.usecase.AppLaunchUseCase;
import com.gionee.gnvoiceassist.usecase.ContactsUseCase;
import com.gionee.gnvoiceassist.usecase.DeviceControlUseCase;
import com.gionee.gnvoiceassist.usecase.GnMusicUseCase;
import com.gionee.gnvoiceassist.usecase.GnRemoteUseCase;
import com.gionee.gnvoiceassist.usecase.PhonecallUseCase;
import com.gionee.gnvoiceassist.usecase.SmsSendUseCase;
import com.gionee.gnvoiceassist.usecase.TimeQueryUseCase;
import com.gionee.gnvoiceassist.usecase.UseCase;
import com.gionee.gnvoiceassist.util.Constants;
import com.gionee.gnvoiceassist.util.LogUtil;
import com.gionee.gnvoiceassist.util.kookong.KookongCustomDataHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.gionee.gnvoiceassist.util.Preconditions.checkNotNull;

public class GnVoiceService extends Service implements IDirectiveListenerCallback, UseCase.UsecaseCallback{

    private static final String TAG = GnVoiceService.class.getSimpleName();
    private static final int MSG_CONTACT_UPDATE = Constants.MSG_UPDATE_CONTACTS;

    private RecognizeManager mRecognizeManager;
    private ContactObserver mContactObserver;
    private ContentResolver mContentResolver;
    private TelephonyManager mTelephonyManager;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeCallback;
    private GnVoiceServiceBinder mBinder;
    private UsecaseHandler mUsecaseHandler;

    //消息传递
    private List<IVoiceServiceListener> mExportCallbacks;   //对外回调接口
    private IRecognizeManagerCallback mRmCallback = new IRecognizeManagerCallback() {
        @Override
        public void onEngineState(Constants.EngineState state) {

        }

        @Override
        public void onRecordStart() {

        }

        @Override
        public void onRecordStop() {

        }

        @Override
        public void onRecordError() {

        }

        @Override
        public void onRecordState(Constants.RecognitionState state) {

        }

        @Override
        public void onTtsStart() {

        }

        @Override
        public void onTtsEnd() {

        }

        @Override
        public void onTtsState(boolean speaking) {

        }

        @Override
        public void onVolume(int volumeLevel) {

        }

        @Override
        public void onResult() {

        }
    };

    private InnerHandler mLocalHandler;
    private InnerPhoneStateListener mPhoneStateListener;

    public GnVoiceService() {
        mBinder = new GnVoiceServiceBinder();
        mExportCallbacks = new ArrayList<>();
        mLocalHandler = new InnerHandler(this);
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return checkNotNull(mBinder, "GnVoiceService中的Binder不能为空");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initEssentialComponent();
        initAdditionalComponent();
        initRecognizeManager();
        requestAudioFocus();    //TODO 处理焦点获取失败的情况
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyEssentialComponent();
        destroyAdditionalComponent();
        destroyRecognizeManager();
        abandonAudioFocus();
    }

    /**
     * 开始录音
     */
    public void fireRecord() {
        mRecognizeManager.startRecord();
    }

    /**
     * 打断录音
     */
    public void abortRecord() {
        mRecognizeManager.abortRecord(true);
        //TODO 恢复音频焦点

    }

    public void stopRecord() {
        mRecognizeManager.abortRecord(false);
        //TODO 恢复音频焦点

    }

    /**
     * 使用TTS朗读文字
     * @param text
     */
    public void playTts(String text) {
        mRecognizeManager.startTts(text);
    }

    /**
     * 停止所有TTS朗读
     */
    public void stopTts() {
        mRecognizeManager.abortRecord(true);
    }

    /**
     * 处理RecognizeManager返回的语音识别结果
     * 选择调用HandlerDispatcher分发到具体的Usecase，或显示到View
     */
    public void dispatchRecognizeResult(DirectiveResponseEntity directiveResult) {
        try {
            mUsecaseHandler.sendMessage(directiveResult);
        } catch (Exception e) {
            LogUtil.e(TAG,"dispatchRecognizeResult() 分发消息时出现错误。" + e);
            e.printStackTrace();
        }
    }

    /**
     * 处理Usecase返回的消息
     * @param usecaseResult UseCase返回的实体类
     */
    public void dispatchUsecaseResult(UsecaseResponseEntity usecaseResult) {
        if (usecaseResult.isShouldSpeak()) {
            //取出SpeakText
            if (!TextUtils.isEmpty(usecaseResult.getSpeakText()))
                playTts(usecaseResult.getSpeakText());
        }

        if (usecaseResult.isShouldRender() && usecaseResult.getRenderContent() != null) {
            //取出RenderEntity
            //分发到界面进行渲染
            renderOnActivity(usecaseResult.getRenderContent());
        }

        if (usecaseResult.isInCustomInteractive() && usecaseResult.getCustomInteract() != null) {
            //取出CUIEntity，信息分发到RecognizeManager，让其主持发起多轮交互
            startCustomInteraction(usecaseResult.getCustomInteract());
        }
    }

    /**
     * 处理从View层传递的请求
     */
    public void dispatchViewRequest(RenderEntity renderData) {

    }

    private void startCustomInteraction(CUIEntity cuiData) {
        mRecognizeManager.startCustomInteraction(cuiData);
    }

    private synchronized void renderOnActivity(RenderEntity renderData) {
        for (IVoiceServiceListener listener:mExportCallbacks) {
            listener.onRenderRequest(renderData);
        }
    }

    private void initRecognizeManager() {
        if (mRecognizeManager == null) {
            mRecognizeManager = RecognizeManager.getInstance();
        }
        mRecognizeManager.addCallback(mRmCallback);
        mRecognizeManager.setDirectiveCallback(this);
        mRecognizeManager.init();
    }

    private void initEssentialComponent() {
        //TODO 初始化UsecaseHandler
        mUsecaseHandler = new UsecaseHandler();
        mUsecaseHandler.registerUsecase("applaunch",new AppLaunchUseCase(),this);
        mUsecaseHandler.registerUsecase("phonecall",new PhonecallUseCase(),this);
        mUsecaseHandler.registerUsecase("smssend",new SmsSendUseCase(),this);
        mUsecaseHandler.registerUsecase("alarm",new AlarmUseCase(),this);
        mUsecaseHandler.registerUsecase("contacts",new ContactsUseCase(),this);
        mUsecaseHandler.registerUsecase("device_control", new DeviceControlUseCase(),this);
        mUsecaseHandler.registerUsecase("gn_remote",new GnRemoteUseCase(),this);
        mUsecaseHandler.registerUsecase("gn_music",new GnMusicUseCase(),this);
        mUsecaseHandler.registerUsecase("time_query",new TimeQueryUseCase(),this);

        // 初始化ContentResolver
        mContentResolver = getContentResolver();    //是否要判断ContentResolver为空的情况？是否要使用ApplicationContext？
        // 初始化ContactObserver
        mContactObserver = new ContactObserver(mLocalHandler,GnVoiceService.this.getApplicationContext());
        mContentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,mContactObserver);
        // TODO 初始化Telephony监听器
        mTelephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
        //初始化音频焦点监听
        mAudioFocusChangeCallback = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                LogUtil.i(TAG, "onAudioFocusChange() focusChange = " + focusChange);
            }
        };

    }

    private void initAdditionalComponent() {
        // 初始化酷控服务
        KookongCustomDataHelper.bindDataRetriveService();
    }

    private void registerBroadcast() {
        //初始化广播接收器
        //需要接收以下广播：开机启动完成


    }

    private void destroyRecognizeManager() {
        if (mRecognizeManager != null) {
            mRecognizeManager.release();
        }
        mRecognizeManager.setDirectiveCallback(null);
        mRecognizeManager.removeCallback(mRmCallback);
    }

    private void destroyEssentialComponent() {
        //TODO 注销UsecaseDispatcher

        // 注销ContentResolver（不需要）

        //TODO 注销ContactObserver
        mContentResolver.unregisterContentObserver(mContactObserver);
        //TODO 注销Telephony监听器
        mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
        mPhoneStateListener = null;
        mTelephonyManager = null;
        //TODO 注销音频焦点监听

    }

    private void destroyAdditionalComponent() {
        //注销酷控服务（不需要）
    }

    private void unregisterBroadcast() {
        //注销广播接收器

    }

    private void startUpdateContacts() {
        Intent intent = new Intent(GnVoiceService.this,ContactsUploadService.class);
        startService(intent);
    }

    private boolean requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus
                (mAudioFocusChangeCallback,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private boolean abandonAudioFocus() {
        int result = mAudioManager.abandonAudioFocus(mAudioFocusChangeCallback);
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }


    @Override
    public void onDirectiveResponse(DirectiveResponseEntity response) {
        dispatchRecognizeResult(response);
    }

    @Override
    public void onVoiceInputVolume(int level) {

    }

    @Override
    public void onRenderResponse(RenderEntity response) {

    }

    @Override
    public void onUsecaseResponse(UsecaseResponseEntity response) {
        dispatchUsecaseResult(response);
    }

    /**
     * VoiceService的通信接口
     * 用于与建立连接的客户端进行通信
     */
    public class GnVoiceServiceBinder extends Binder {

        public void addCallback(IVoiceServiceListener listener) {
            if (!mExportCallbacks.contains(checkNotNull(listener))) {
                mExportCallbacks.add(checkNotNull(listener));
            }
        }

        public void removeCallback(IVoiceServiceListener listener) {
            if (!mExportCallbacks.contains(checkNotNull(listener))) {
                mExportCallbacks.remove(listener);
            }
        }

        public void startRecord() {
            fireRecord();
        }

        public void abortRecord() {
            GnVoiceService.this.abortRecord();
        }

        public void stopRecord() {
            GnVoiceService.this.stopRecord();
        }

        public void sendRequestMessage() {

        }
    }

    private static class InnerHandler extends Handler{

        private WeakReference<GnVoiceService> mVoiceService;

        InnerHandler(GnVoiceService service) {
            mVoiceService = new WeakReference<GnVoiceService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            GnVoiceService service = mVoiceService.get();
            switch (msg.what) {
                case MSG_CONTACT_UPDATE:
                    service.startUpdateContacts();
                    break;
            }
        }
    }

    private class InnerPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //TODO 当电话来电时，打断语音识别
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //打断语音识别
                    abortRecord();
                    // 是否需要退出界面？
                    break;
                default:
                    break;

            }
        }

    }

}
