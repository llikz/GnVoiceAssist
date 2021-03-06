package com.gionee.voiceassist.directiveListener.webbrowser;

import android.content.Intent;
import android.net.Uri;

import com.baidu.duer.dcs.framework.message.Directive;
import com.gionee.voiceassist.util.LogUtil;
import com.gionee.voiceassist.GnVoiceAssistApplication;
import com.gionee.voiceassist.basefunction.IBaseFunction;
import com.gionee.voiceassist.directiveListener.BaseDirectiveListener;
import com.gionee.voiceassist.coreservice.sdk.module.webbrowser.WebBrowserDeviceModule;

/**
 * Created by twf on 2017/8/16.
 */

public class WebBrowserListener extends BaseDirectiveListener implements WebBrowserDeviceModule.IWebBrowserDirectiveListener {
    public static final String TAG = WebBrowserListener.class.getSimpleName();
    private static final String APP_BROWSER_PACKAGE_NAME = "com.android.browser";

    public WebBrowserListener(IBaseFunction baseFunction) {
        super(baseFunction);
    }

    @Override
    public void webLauncherDirectiveReceived(String url, Directive directive) {
        String rawMessage = directive.rawMessage;
        LogUtil.d("DCSF-----", TAG + " url= " + url);
        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		mIntent.addCategory(Intent.CATEGORY_BROWSABLE);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.setPackage(APP_BROWSER_PACKAGE_NAME);

        GnVoiceAssistApplication.getInstance().startActivity(mIntent);
    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {

    }
}
