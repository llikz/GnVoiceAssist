package com.gionee.voiceassist.directiveListener.applauncher;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.baidu.duer.dcs.devicemodule.custominteraction.CustomUserInteractionDeviceModule;
import com.baidu.duer.dcs.devicemodule.custominteraction.message.CustomClicentContextMachineState;
import com.baidu.duer.dcs.devicemodule.custominteraction.message.CustomClientContextHyperUtterace;
import com.baidu.duer.dcs.devicemodule.custominteraction.message.CustomClientContextPayload;
import com.baidu.duer.dcs.framework.message.Directive;
import com.baidu.duer.dcs.framework.message.Payload;
import com.baidu.duer.dcs.common.util.CommonUtil;
import com.gionee.voiceassist.util.LogUtil;
import com.gionee.voiceassist.basefunction.IBaseFunction;
import com.gionee.voiceassist.basefunction.MaxUpriseCounter;
import com.gionee.voiceassist.basefunction.applaunch.AppLaunchPresenter;
import com.gionee.voiceassist.customlink.CustomLinkSchema;
import com.gionee.voiceassist.directiveListener.BaseDirectiveListener;
import com.gionee.voiceassist.directiveListener.customuserinteraction.CustomUserInteractionManager;
import com.gionee.voiceassist.coreservice.sdk.module.applauncher.AppLauncherDeviceModule;
import com.gionee.voiceassist.util.SharedData;

import java.util.ArrayList;
import java.util.List;

import static com.gionee.voiceassist.util.Utils.doUserActivity;

/**
 * Created by twf on 2017/8/16.
 */

public class AppLauncherListener extends BaseDirectiveListener implements AppLauncherDeviceModule.IAppLauncherDirectiveListener {
    public static final String TAG = AppLauncherListener.class.getSimpleName();
    public static final String DOWNLOAD_CONFIRM = "download_confirm";
    public static final String DOWNLOAD_CANCEL = "download_cancel";
    public static final String UTTER_SHOW_CONFIRM_DIALOG = "utter_show_confirm_dialog";
    public static final String UTTER_ENTER_DOWNLOAD_CONFIRM_CUI = "utter_enter_download_confirm_cui";
    public static final String CUI_QUERY_DOWNLOAD_APP = "cui_query_download_app";


    private AppLaunchPresenter mAppLaunchPresenter;

    public AppLauncherListener(IBaseFunction iBaseFunction) {
        super(iBaseFunction);
        mAppLaunchPresenter = iBaseFunction.getAppLaunchPresenter();
    }

    @Override
    public void appLauncherDirectiveReceived(String appName, String packageName, Directive directive) {

        LogUtil.d("DCSF", TAG + " appName= " + appName + " packageName= " + packageName);
        boolean isLaunchSuccess = false;
        if(!TextUtils.isEmpty(packageName)) {
            isLaunchSuccess = mAppLaunchPresenter.launchAppByPackageName(packageName);
        } else if(!TextUtils.isEmpty(appName)) {
            isLaunchSuccess = mAppLaunchPresenter.launchAppByName(appName);
        }

        if(isLaunchSuccess) {
            playTTS("正在为您打开" + appName, true);
        } else {
            confirmDownloadOrNot(appName);
        }
    }

    @Override
    public void appDeepLinkDirectiveReceived(String s, String s1, String deeplink, Directive directive) {
        // 地图导航
        LogUtil.d("DCSF", TAG + " s= " + s + " s1= " + s1 + " deeplink= " + deeplink);

//        DcsSDK.getInstance().getAppLauncher().
//                launchAppByDeepLink(iBaseFunction.getMainActivity(), deeplink);
        mAppLaunchPresenter.launchAppByDeepLink(deeplink);
    }

    @Override
    public void handleCUInteractionTargetUrl(String id, String url) {
        super.handleCUInteractionTargetUrl(id, url);
        if(TextUtils.equals(id, CUI_QUERY_DOWNLOAD_APP)) {
            if(url.startsWith(CustomLinkSchema.LINK_APP_DOWNLOAD)){
                int beginIdx = url.indexOf(":");
                String realContent = url.substring(beginIdx + 3);
                LogUtil.d(TAG, "customUserInteractionDirectiveReceived realContent = " + realContent);
                if(TextUtils.equals(realContent, DOWNLOAD_CANCEL)) {
                    mAppLaunchPresenter.cancelDownload();
                } else if(TextUtils.equals(realContent, DOWNLOAD_CONFIRM)) {
                    mAppLaunchPresenter.confirmDownload();
                }
            }
        }
    }

    @Override
    public void handleCUInteractionUnknownUtterance(String id) {
        super.handleCUInteractionUnknownUtterance(id);
        if(TextUtils.equals(id, CUI_QUERY_DOWNLOAD_APP)) {
            String alert = "您要下载还是取消？";
            MaxUpriseCounter.increaseUpriseCount();
            if(MaxUpriseCounter.isMaxCount()) {
                alert = "太累了,我先休息一下";
                mAppLaunchPresenter.disappearSelectButton();
                CustomUserInteractionManager.getInstance().setStopCurrentInteraction(true);
                playTTS(alert, true);
                return;
            }
            playTTS(alert, UTTER_ENTER_DOWNLOAD_CONFIRM_CUI, this, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSpeakFinish(String utterId) {
        // show view
        if(TextUtils.equals(utterId, UTTER_SHOW_CONFIRM_DIALOG)) {
//            mAppLaunchPresenter.tipInstallOrCancel();
        } else if(TextUtils.equals(utterId, UTTER_ENTER_DOWNLOAD_CONFIRM_CUI)) {

        }

        if(CustomUserInteractionManager.getInstance().isCustomUserInteractionProcessing()) {
            // custom user interaction action
            if(CommonUtil.isFastDoubleClick()) {
                return;
            }
            LogUtil.d(TAG, "onSpeakFinish startRecordOnline");
            iBaseFunction.getRecordController().startRecordOnline();
            doUserActivity();
        }
    }

    @Override
    public void onDestroy() {
//        iBaseFunction.onDestroy();
//        iBaseFunction = null;
    }

    public void confirmDownloadOrNot(String appName) {
        CustomUserInteractionDeviceModule.PayLoadGenerator generator = new CustomUserInteractionDeviceModule.PayLoadGenerator() {
            @Override
            public Payload generateContextPayloadByInteractionState(CustomClicentContextMachineState customClicentContextMachineState) {
                LogUtil.d(TAG, "generateContextPayloadByInteractionState");
                if (CustomUserInteractionManager.getInstance().shouldStopCurrentInteraction()) {
                    // 达到最大多轮交互次数，跳出自定义多轮交互状态
                    LogUtil.d(TAG, "confirmDownloadOrNot ******************************ShouldStopCurrentInteraction");
                    return new CustomClientContextPayload(null);
                }
                Payload payload;
                ArrayList<CustomClientContextHyperUtterace> hyperUtterances = new ArrayList<>();

                // Yes
                List<String> confrimUtterances = new ArrayList<>();
                confrimUtterances.add("确定");
                confrimUtterances.add("下载");
                confrimUtterances.add("好的");
                String confirmUrl = CustomLinkSchema.LINK_APP_DOWNLOAD + DOWNLOAD_CONFIRM;
                CustomClientContextHyperUtterace confirmHyperUtterace = new CustomClientContextHyperUtterace(confrimUtterances, confirmUrl);
                // No
                List<String> cancelUtterances = new ArrayList<>();
                cancelUtterances.add("取消");
                cancelUtterances.add("取消下载");
                cancelUtterances.add("不下载");
                String cancelUrl = CustomLinkSchema.LINK_APP_DOWNLOAD + DOWNLOAD_CANCEL;
                CustomClientContextHyperUtterace cancelHyperUtterance = new CustomClientContextHyperUtterace(cancelUtterances, cancelUrl);

                hyperUtterances.add(confirmHyperUtterace);
                hyperUtterances.add(cancelHyperUtterance);
                payload = new CustomClientContextPayload(false, hyperUtterances);
                return payload;
            }
        };

        CustomUserInteractionManager.getInstance()
                .startCustomUserInteraction(generator, CUI_QUERY_DOWNLOAD_APP, this);

        playTTS("没有安装" + appName + ",下载还是取消？", UTTER_SHOW_CONFIRM_DIALOG, this, false);
        mAppLaunchPresenter.tipInstallOrCancel();
    }
}
