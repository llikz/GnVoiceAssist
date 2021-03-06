/*
 * *
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.gionee.voiceassist.coreservice.sdk.module.applauncher;

import android.content.Context;

import com.gionee.voiceassist.coreservice.sdk.module.applauncher.message.AppInfo;

import java.util.List;

/**
 * Created by caoyushu01 on 2017/7/20.
 * 打开应用接口
 */

public interface IAppLauncher {
    // 根据应用名打开
    void launchAppByName(Context context, String appname);
    // 根据应用包名打开
    void launchAppByPackageName(Context context, String packageName);
    // 根据deeplink打开应用指定功能
    void launchAppByDeepLink(Context context, String deepLink);
    // 异步更新app列表信息
    void updateAppList(Context context);
    // 获取缓存的应用列表
    List<AppInfo> getAppList();

}
