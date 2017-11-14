package com.gionee.gnvoiceassist.sdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.duer.dcs.androidsystemimpl.AudioRecordImpl;
import com.baidu.duer.dcs.androidsystemimpl.player.MediaPlayerImpl;
import com.baidu.duer.dcs.androidsystemimpl.wakeup.kitt.KittWakeUpImpl;
import com.baidu.duer.dcs.api.DcsSdkBuilder;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.devicemodule.audioplayer.AudioPlayerDeviceModule;
import com.baidu.duer.dcs.devicemodule.custominteraction.CustomUserInteractionDeviceModule;
import com.baidu.duer.dcs.devicemodule.system.SystemDeviceModule;
import com.baidu.duer.dcs.framework.DcsSdkImpl;
import com.baidu.duer.dcs.framework.DialogRequestIdHandler;
import com.baidu.duer.dcs.framework.ILoginListener;
import com.baidu.duer.dcs.framework.IMessageSender;
import com.baidu.duer.dcs.framework.InternalApi;
import com.baidu.duer.dcs.framework.internalapi.DcsConfig;
import com.baidu.duer.dcs.framework.internalapi.IASROffLineConfigProvider;
import com.baidu.duer.dcs.framework.internalapi.IWakeupAgent;
import com.baidu.duer.dcs.framework.internalapi.IWakeupProvider;
import com.baidu.duer.dcs.framework.location.Location;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.oauth.api.credentials.BaiduOauthClientCredentialsImpl;
import com.baidu.duer.dcs.offline.asr.bean.ASROffLineConfig;
import com.baidu.duer.dcs.systeminterface.BaseAudioRecorder;
import com.baidu.duer.dcs.systeminterface.BaseWakeup;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.gionee.gnvoiceassist.GnVoiceAssistApplication;
import com.gionee.gnvoiceassist.sdk.module.alarms.AlarmsDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.applauncher.AppLauncherDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.applauncher.IAppLauncher;
import com.gionee.gnvoiceassist.sdk.module.applauncher.IAppLauncherImpl;
import com.gionee.gnvoiceassist.sdk.module.contacts.ContactsDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.devicecontrol.DeviceControlDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.localaudioplayer.LocalAudioPlayerDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.offlineasr.OffLineDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.phonecall.PhoneCallDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.screen.ScreenDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.screen.extend.card.ScreenExtendDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.sms.SmsDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.telecontroller.TeleControllerDeviceModule;
import com.gionee.gnvoiceassist.sdk.module.webbrowser.WebBrowserDeviceModule;
import com.gionee.gnvoiceassist.util.Constants;
import com.gionee.gnvoiceassist.util.LogUtil;
import com.gionee.gnvoiceassist.util.T;
import com.gionee.gnvoiceassist.util.Utils;
import com.gionee.gnvoiceassist.util.kookong.KookongCustomDataHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by liyingheng on 10/15/17.
 */

public class SdkManagerImpl implements ISdkManager {

    //Config SDK
    private static final String WAKEUP_WORD = "";
    private static final boolean ENABLE_WAKEUP = false;

    private static final String TAG = SdkManagerImpl.class.getSimpleName();
    private static SdkManagerImpl sInstance;

    private IDcsSdk mDcsSdk;
    private Context mAppCtx;
    private IWakeupAgent.IWakeupAgentListener wakeupAgentListener;

    private SdkManagerImpl() {
        mAppCtx = GnVoiceAssistApplication.getInstance().getApplicationContext();
    }

    public static synchronized SdkManagerImpl getInstance() {
        if (sInstance == null) {
            sInstance = new SdkManagerImpl();
        }
        return sInstance;
    }

    @Override
    public void init() {
        initSdk(mAppCtx);
    }

    @Override
    public void destroy() {
        mDcsSdk.release();
    }

    private void initSdk(final Context context) {
        // 第一步初始化sdk
        BaseAudioRecorder audioRecorder = new AudioRecordImpl();

        final BaseWakeup wakeup = new KittWakeUpImpl();
        IWakeupProvider wakeupProvider = new IWakeupProvider() {
            @Override
            public String wakeupWords() {
                return "你好小金";
            }

            @Override
            public boolean enableWarning() {
                return false;
            }

            @Override
            public String warningSource() {
                return "assets://alarm.mp3";
            }

            @Override
            public boolean wakeAlways() {
                return false;
            }

            @Override
            public BaseWakeup wakeupImpl() {
                return wakeup;
            }
        };

        String clientId = "83kW99iEz0jpGp9hrX981ezGcTaxNzk0";
        String clientSecret = "UTjgedIE5CRZM3CWj2cApLKajeZWotvf";
        String appId = "10290022";
        String apiKey = "bw40xRdDGFclaSIGzgXgNFdG";
        String secretKey = "AkP1XOuGVlrrML7dTs4WqW6bqj8lvv6C";
        IOauth oauth = new BaiduOauthClientCredentialsImpl(clientId, clientSecret);
        final ASROffLineConfig asrOffLineConfig = new ASROffLineConfig();
        asrOffLineConfig.offlineAsrSlots = getOfflineAsrSlots();
        asrOffLineConfig.asrAppId = appId;
        asrOffLineConfig.asrAppKey = apiKey;
        asrOffLineConfig.asrSecretKey = secretKey;

        IASROffLineConfigProvider asrOffLineConfigProvider = new IASROffLineConfigProvider() {
            @Override
            public ASROffLineConfig getOfflineConfig() {
                return asrOffLineConfig;
            }
        };

        mDcsSdk = new DcsSdkBuilder()
                .withClientId(clientId)
                .withOauth(oauth)
                .withAudioRecorder(audioRecorder)
                .build();

        getInternalApi().setDebug(true);
        getInternalApi().setWakeupProvider(wakeupProvider);
        getInternalApi().setAsrMode(GnVoiceAssistApplication.ASR_MODE);
        getInternalApi().setAsrOffLineConfigProvider(asrOffLineConfigProvider);

        //配置自定义端能力
        initDeviceModule(context);

        // 第三步，将sdk跑起来
        getInternalApi().login(new ILoginListener() {
            @Override
            public void onSucceed(String s) {
                mDcsSdk.run();
            }

            @Override
            public void onFailed(String s) {
                LogUtil.e(TAG,"SDK Login failed! Message = " + s);
            }

            @Override
            public void onCancel() {
                LogUtil.w(TAG,"SDK login cancel!");
            }
        });

        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgentListener = new IWakeupAgent.IWakeupAgentListener() {
                @Override
                public void onWakeupSucceed() {
                    Toast.makeText(context, "唤醒成功",
                            Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onWarningCompleted() {

                }
            };
            wakeupAgent.addWakeupAgentListener(wakeupAgentListener);
        }

    }

    private void initDeviceModule(Context context) {
        //初始化应用启动模块AppLauncher
        IAppLauncher appLauncher = new IAppLauncherImpl(context);
        IMessageSender messageSender = ((DcsSdkImpl) mDcsSdk).getInternalApi().getMessageSender();
        AppLauncherDeviceModule appLauncherDeviceModule = new AppLauncherDeviceModule(appLauncher, messageSender);
        mDcsSdk.putDeviceModule(appLauncherDeviceModule);

        //初始化电话模块PhonecallDeviceModule
        PhoneCallDeviceModule phoneCallDeviceModule = new PhoneCallDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(phoneCallDeviceModule);

        //初始化短信模块SmsDeviceModule
        SmsDeviceModule smsDeviceModule = new SmsDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(smsDeviceModule);

        //初始化联系人模块ContactsDeviceModule
        ContactsDeviceModule contactsDeviceModule = new ContactsDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(contactsDeviceModule);

        //初始化浏览器模块WebBrowserDeviceModule
        WebBrowserDeviceModule webBrowserDeviceModule = new WebBrowserDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(webBrowserDeviceModule);

        //初始化闹铃模块AlarmsDeviceModule
        AlarmsDeviceModule alarmsDeviceModule = new AlarmsDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(alarmsDeviceModule);

        //初始化手机控制模块DeviceControlDeviceModule
        DeviceControlDeviceModule deviceControlDeviceModule = new DeviceControlDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(deviceControlDeviceModule);

        //初始化上屏模块ScreenDeviceModule
        ScreenDeviceModule screenDeviceModule = new ScreenDeviceModule(messageSender);
        ScreenExtendDeviceModule screenExtendDeviceModule = new ScreenExtendDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(screenDeviceModule);
        mDcsSdk.putDeviceModule(screenExtendDeviceModule);

        //初始化telecontrollerDeviceModule
        TeleControllerDeviceModule teleControllerDeviceModule = new TeleControllerDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(teleControllerDeviceModule);

        //初始化localAudioPlayerDeviceModule
        LocalAudioPlayerDeviceModule localAudioPlayerDeviceModule = new LocalAudioPlayerDeviceModule(messageSender);
        mDcsSdk.putDeviceModule(localAudioPlayerDeviceModule);

        //初始化CustomInteraction多轮交互模块
        CustomUserInteractionDeviceModule customUserInteractionDeviceModule = new CustomUserInteractionDeviceModule(messageSender,new DialogRequestIdHandler());
        mDcsSdk.putDeviceModule(customUserInteractionDeviceModule);

        //初始化离线识别模块
        OffLineDeviceModule offLineDeviceModule = new OffLineDeviceModule();
        mDcsSdk.putDeviceModule(offLineDeviceModule);

        getInternalApi().getDeviceModule(com.baidu.duer.dcs.devicemodule.custominteraction.ApiConstants.NAMESPACE);
    }

    private JSONObject getOfflineAsrSlots() {
        long startTimemills = System.currentTimeMillis();
        long endTimemills = 0;
        JSONObject slotJson = new JSONObject();
        try {
            {
                Map<String, String[]> slotMap = KookongCustomDataHelper.getKookongOfflineAsrSlotMap();

                String[] deviceList = slotMap.get(Constants.SLOT_DEVICELIST);
                if(deviceList != null) {
                    JSONArray slotdataArray1 = new JSONArray(deviceList);
                    slotJson.put(Constants.SLOT_DEVICELIST, slotdataArray1);
                }

                String[] customACStateList = slotMap.get(Constants.SLOT_CUSTOMACSTATELIST);
                if(customACStateList != null) {
                    JSONArray slotDataArray2 = new JSONArray(customACStateList);
                    slotJson.put(Constants.SLOT_CUSTOMACSTATELIST, slotDataArray2);
                }

                JSONArray slotdataArray = new JSONArray();
                slotdataArray.put("相机");
                slotdataArray.put("设置");
                slotdataArray.put("相册");
                slotdataArray.put("联系人");
                // 通用识别槽位
                slotJson.put(Constants.SLOT_APPNAME, slotdataArray);
            }
            {
                JSONArray slotdataArray = new JSONArray();
                ArrayList<String> contactNameList = Utils.getAllContacts(GnVoiceAssistApplication.getInstance());
                for(String name : contactNameList) {
                    slotdataArray.put(name);
                }
                // 通用识别槽位
                slotJson.put(Constants.SLOT_CONTACTNAME, slotdataArray);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endTimemills = System.currentTimeMillis();
            Log.i("liyh","getOfflineAsrSlots() duration = " + (endTimemills - startTimemills));
            return slotJson;
        }
        // 离线识别

//        JSONObject slotJson = new JSONObject();
//        try {
//            JSONArray slotdataArray = new JSONArray();
//            slotdataArray.put("打开空调");
//            slotdataArray.put("打开电视");
//            slotdataArray.put("关闭空调");
//            slotdataArray.put("今天天气怎么样");
//            // 通用识别槽位
//            slotJson.put("generalslot", slotdataArray);
//            JSONArray slotdPhoneNameArray = new JSONArray();
//            slotdPhoneNameArray.put("张三");
//            slotdPhoneNameArray.put("李四");
//            // 通用识别槽位
//            slotJson.put("phonename", slotdPhoneNameArray);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } finally {
//            return slotJson;
//        }
    }

    public IDcsSdk getDcsSdk() {
        if (mDcsSdk == null) {
            init();
        }
        return mDcsSdk;
    }


    public InternalApi getInternalApi() {
        return ((DcsSdkImpl) mDcsSdk).getInternalApi();
    }

}
