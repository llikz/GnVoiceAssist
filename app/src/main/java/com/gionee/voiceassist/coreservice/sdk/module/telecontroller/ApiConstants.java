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
package com.gionee.voiceassist.coreservice.sdk.module.telecontroller;

/**
 * Created by caoyushu01 on 2017/7/25.
 * 金立遥控器服务
 */

public class ApiConstants {
    // 自定义指令集 v3  修改了遥控器指令的命名空间
    public static final String NAMESPACE = "ai.dueros.device_interface.thirdparty.gionee.kookong";
    public static final String NAME = "TeleControllerInterface";

    public static final class Events {

    }

    public static final class Directives {
        public static final class OperateBluetooth {
            public static final String NAME = OperateBluetooth.class.getSimpleName();
        }
    }
}
